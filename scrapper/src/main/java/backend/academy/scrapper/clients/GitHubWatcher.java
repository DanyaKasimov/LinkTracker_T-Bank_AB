package backend.academy.scrapper.clients;

import backend.academy.scrapper.constants.GitHubEndpoints;
import backend.academy.scrapper.dto.GitHubUpdate;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.NotificationService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubWatcher {

    private final GitHubClient gitHubClient;
    private final LinkService linkService;
    private final NotificationService notificationService;
    private final Map<String, String> lastCommitHashes = new ConcurrentHashMap<>();

    private static final String GH_API_URL = "https://github.com/";
    private static final String PULL_URL = "/pull/";
    private static final String ISSUES_URL = "/issues/";
    private static final int THREAD_COUNT = 4;
    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedRate = 10_000)
    public void checkForUpdates() {
        List<String> links = linkService.findAllLinksByLink(GH_API_URL);
        if (links.isEmpty()) return;

        List<String> batch = links.subList(0, Math.min(BATCH_SIZE, links.size()));
        processLinksInParallel(batch);
    }

    private void processLinksInParallel(List<String> links) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<List<String>> partitions = partitionList(links, THREAD_COUNT);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (List<String> chunk : partitions) {
            tasks.add(() -> {
                for (String link : chunk) {
                    processLink(link);
                }
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Потоки были прерваны: {}", e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private List<List<String>> partitionList(List<String> list, int partitions) {
        int chunkSize = (int) Math.ceil((double) list.size() / partitions);
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            result.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return result;
    }

    private void processLink(String link) {
        try {
            if (link.contains(PULL_URL)) {
                handlePullRequest(link);
            } else if (link.contains(ISSUES_URL)) {
                handleIssue(link);
            } else {
                handleCommit(link);
            }
        } catch (Exception e) {
            log.atError()
                    .setMessage("Ошибка при обработке ссылки GitHub")
                    .addKeyValue("link", link)
                    .addKeyValue("errorMessage", e.getMessage())
                    .log();
        }
    }

    private void handlePullRequest(String link) {
        Optional<GitHubUpdate> update = gitHubClient.getLatestPRIssue(link, GitHubEndpoints.PULL_REQUEST);
        update.ifPresent(updateData -> {
            log.atInfo()
                    .setMessage("Новый Pull Request")
                    .addKeyValue("link", link)
                    .log();
            notificationService.sendNotification(link, updateData);
        });
    }

    private void handleIssue(String link) {
        Optional<GitHubUpdate> update = gitHubClient.getLatestPRIssue(link, GitHubEndpoints.ISSUES);
        update.ifPresent(updateData -> {
            log.atInfo().setMessage("Новый Issue").addKeyValue("link", link).log();
            notificationService.sendNotification(link, updateData);
        });
    }

    private void handleCommit(String link) {
        Optional<GitHubUpdate> update = gitHubClient.getLatestCommit(link);
        String lastCommitHash = lastCommitHashes.get(link);

        update.ifPresentOrElse(message -> handleNewCommit(link, message, lastCommitHash), () -> handleNoCommit(link));
    }

    private void handleNoCommit(String link) {
        log.atInfo()
                .setMessage("Коммиты не обнаружены")
                .addKeyValue("link", link)
                .log();
        lastCommitHashes.put(link, "");
    }

    private void handleNewCommit(String link, GitHubUpdate message, String lastCommitHash) {
        if (lastCommitHash == null) {
            lastCommitHashes.put(link, message.sha());
            log.atInfo()
                    .setMessage("Первый запуск: установлен хеш коммита")
                    .addKeyValue("commitHash", message.sha())
                    .addKeyValue("link", link)
                    .log();
            return;
        }

        boolean hashesEqual = MessageDigest.isEqual(
                lastCommitHash.getBytes(StandardCharsets.UTF_8), message.sha().getBytes(StandardCharsets.UTF_8));

        if (!hashesEqual) {
            log.atInfo()
                    .setMessage("Обнаружен новый коммит")
                    .addKeyValue("commitHash", message.sha())
                    .addKeyValue("link", link)
                    .log();

            lastCommitHashes.put(link, message.sha());
            notificationService.sendNotification(link, message);
        }
    }
}
