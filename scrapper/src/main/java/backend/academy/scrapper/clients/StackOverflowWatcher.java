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
import java.util.Optional;


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
        subscriptionService.findAllLinksByLink("https://stackoverflow.com/").forEach(this::processLink);
    }

    private void processLink(String link) {
        try {
            Optional<StackOverflowAnswer> answer = stackOverflowClient.getTryLatestAnswer(link);
            Integer lastAnswerId = lastAnswerIds.get(link);


            answer.ifPresentOrElse(
                message -> handleNewAnswer(link, message, lastAnswerId),
                () -> handleNoAnswer(link)
            );
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

    private void handleNewAnswer(String link, StackOverflowAnswer answer, Integer lastAnswerId) {
        if (lastAnswerId == null) {
            lastAnswerIds.put(link, answer.answerId());
            log.atInfo()
                .setMessage("Первый запуск: установлен ID ответа")
                .addKeyValue("answerId", answer.answerId())
                .addKeyValue("link", link)
                .log();
            return;
        }

        if (!lastAnswerId.equals(answer.answerId())) {
            log.atInfo()
                .setMessage("Новый ответ")
                .addKeyValue("answerId", answer.answerId())
                .addKeyValue("link", link)
                .log();
            lastAnswerIds.put(link, answer.answerId());
            notifySubscribers(link, answer);
        }
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
