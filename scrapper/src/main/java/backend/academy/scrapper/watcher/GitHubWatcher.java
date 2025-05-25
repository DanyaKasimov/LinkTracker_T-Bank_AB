package backend.academy.scrapper.watcher;

import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.constants.GitHubEndpoints;
import backend.academy.scrapper.dto.GitHubUpdate;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.NotificationService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GitHubWatcher extends AbstractWatcher<GitHubUpdate> {

    private static final long FIXED_RATE = 10_000L;

    private final GitHubClient gitHubClient;
    private final ScrapperConfig config;
    private final Map<String, String> lastCommitHashes = new ConcurrentHashMap<>();

    @Autowired
    public GitHubWatcher(
            GitHubClient gitHubClient,
            LinkService linkService,
            NotificationService notificationService,
            ScrapperConfig scrapperConfig) {
        super(linkService, notificationService, scrapperConfig.multithreading().threadCount());
        this.gitHubClient = gitHubClient;
        this.config = scrapperConfig;
    }

    @Scheduled(fixedRate = FIXED_RATE)
    public void checkForUpdates() {
        doCheck();
    }

    @Override
    protected String getBaseUrl() {
        return config.github().baseUrl();
    }

    @Override
    protected int getBatchSize() {
        return config.multithreading().batchSize();
    }

    @Override
    protected int getThreadCount() {
        return config.multithreading().threadCount();
    }

    @Override
    protected void fetchAndNotify(String link) {
        try {
            if (link.contains("/pull/")) {
                gitHubClient
                        .getLatestPRIssue(link, GitHubEndpoints.PULL_REQUEST)
                        .ifPresent(update -> sendNotification(link, update));
            } else if (link.contains("/issues/")) {
                gitHubClient
                        .getLatestPRIssue(link, GitHubEndpoints.ISSUES)
                        .ifPresent(update -> sendNotification(link, update));
            } else {
                Optional<GitHubUpdate> update = gitHubClient.getLatestCommit(link);
                String previous = lastCommitHashes.get(link);
                update.ifPresentOrElse(
                        u -> handleNewCommit(link, u, previous), () -> lastCommitHashes.putIfAbsent(link, ""));
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке ссылки GitHub {}: {}", link, e.getMessage());
        }
    }

    private void handleNewCommit(String link, GitHubUpdate update, String previous) {
        String sha = update.sha();
        if (previous == null) {
            lastCommitHashes.put(link, sha);
            log.info("Хеш коммита {} установлен для ссылки {}", sha, link);
            return;
        }
        if (!constantTimeEquals(previous, sha)) {
            lastCommitHashes.put(link, sha);
            sendNotification(link, update);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return Objects.equals(a, b);
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }

    private void sendNotification(String link, GitHubUpdate update) {
        log.info("Новое обновление GitHub для {}: {}", link, update.title());
        notificationService.sendNotification(link, update);
    }
}
