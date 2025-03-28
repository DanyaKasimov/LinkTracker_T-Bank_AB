package backend.academy.scrapper.clients;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.LinkUpdateDto;
import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowWatcher {
    private final StackOverflowClient stackOverflowClient;
    private final SubscriptionService subscriptionService;
    private final RestAccessor restAccessor;

    private final Map<String, Integer> lastAnswerIds = new HashMap<>();

    @Scheduled(fixedRate = 100_000)
    public void checkForUpdates() {
        subscriptionService.findAllLinksStackOverflow().forEach(this::processLink);
    }

    private void processLink(String link) {
        try {
            StackOverflowAnswer answer = stackOverflowClient.getLatestAnswer(link);
            Integer lastAnswerId = lastAnswerIds.get(link);

            if (answer == null) {
                handleNoAnswer(link);
                return;
            }

            if (lastAnswerId == null) {
                handleFirstAnswer(link, answer);
            } else if (!lastAnswerId.equals(answer.answerId())) {
                handleNewAnswer(link, answer);
            } else {
                log.atInfo()
                    .setMessage("Новых ответов нет")
                    .addKeyValue("link", link)
                    .log();
            }
        } catch (Exception e) {
            log.atError()
                .setMessage("Ошибка при получении ответов")
                .addKeyValue("link", link)
                .addKeyValue("errorMessage", e.getMessage())
                .log();
        }
    }

    private void handleNoAnswer(String link) {
        log.atInfo()
            .setMessage("Ответов пока нет")
            .addKeyValue("link", link)
            .log();
        lastAnswerIds.put(link, 0);
    }

    private void handleFirstAnswer(String link, StackOverflowAnswer answer) {
        lastAnswerIds.put(link, answer.answerId());
        log.atInfo()
            .setMessage("Первый запуск: установлен ID ответа")
            .addKeyValue("answerId", answer.answerId())
            .addKeyValue("link", link)
            .log();
    }

    private void handleNewAnswer(String link, StackOverflowAnswer answer) {
        log.atInfo()
            .setMessage("Новый ответ")
            .addKeyValue("answerId", answer.answerId())
            .addKeyValue("link", link)
            .log();
        lastAnswerIds.put(link, answer.answerId());
        notifySubscribers(link, answer);
    }

    private void notifySubscribers(String link, StackOverflowAnswer answer) {
        restAccessor.postBot("/updates", LinkUpdateDto.builder()
                .id("1")
                .url(link)
                .description(answer.body())
                .tgChatIds(subscriptionService.findAllChatIdsByLink(link))
                .build(),
            String.class);
    }
}
