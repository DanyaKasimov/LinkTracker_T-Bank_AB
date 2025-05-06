package backend.academy.scrapper.clients;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.CommitMessage;
import backend.academy.scrapper.dto.LinkUpdateDto;
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
public class GitHubWatcher {

    private final GitHubClient gitHubClient;
    private final SubscriptionService subscriptionService;
    private final RestAccessor restAccessor;

    private final Map<String, String> lastCommitHashes = new HashMap<>();

    @Scheduled(fixedRate = 10_000)
    public void checkForUpdates() {
        subscriptionService.findAllLinksByLink("https://github.com/")
            .forEach(this::processLink);
    }

    private void processLink(String link) {
        try {
            Optional<CommitMessage> messageOpt = gitHubClient.getTryLatestCommitHash(link);
            String lastCommitHash = lastCommitHashes.get(link);

            messageOpt.ifPresentOrElse(
                message -> handleNewCommit(link, message, lastCommitHash),
                () -> handleNoCommit(link)
            );
        } catch (Exception e) {
            log.atError()
                .setMessage("Ошибка при получении коммитов")
                .addKeyValue("link", link)
                .addKeyValue("errorMessage", e.getMessage())
                .log();
        }
    }

    private void handleNoCommit(String link) {
        log.atInfo()
            .setMessage("Коммиты не обнаружены")
            .addKeyValue("link", link)
            .log();
        lastCommitHashes.put(link, "");
    }

    private void handleNewCommit(String link, CommitMessage message, String lastCommitHash) {
        if (lastCommitHash == null) {
            lastCommitHashes.put(link, message.sha());
            log.atInfo()
                .setMessage("Первый запуск: установлен хеш коммита")
                .addKeyValue("commitHash", message.sha())
                .addKeyValue("link", link)
                .log();
            return;
        }

        if (!lastCommitHash.equals(message.sha())) {
            log.atInfo()
                .setMessage("Обнаружен новый коммит")
                .addKeyValue("commitHash", message.sha())
                .addKeyValue("link", link)
                .log();

            lastCommitHashes.put(link, message.sha());
            notifySubscribers(link, message);
        }
    }

    private void notifySubscribers(String link, CommitMessage message) {
        restAccessor.postBot("/updates", LinkUpdateDto.builder()
                .id("1")
                .url(link)
                .description(message.message())
                .tgChatIds(subscriptionService.findAllChatIdsByLink(link))
                .build(),
            String.class);
    }
}
