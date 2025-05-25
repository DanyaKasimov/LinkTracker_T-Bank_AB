package backend.academy.scrapper.watcher;

import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.NotificationService;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowWatcher extends AbstractWatcher<StackOverflowAnswer> {

    private static final long FIXED_RATE = 100_000L;

    private final ScrapperConfig config;
    private final StackOverflowClient stackOverflowClient;
    private final Map<String, String> lastAnswerIds = new ConcurrentHashMap<>();

    public StackOverflowWatcher(
            StackOverflowClient stackOverflowClient,
            LinkService linkService,
            NotificationService notificationService,
            ScrapperConfig scrapperConfig) {
        super(linkService, notificationService, scrapperConfig.multithreading().threadCount());
        this.stackOverflowClient = stackOverflowClient;
        this.config = scrapperConfig;
    }

    @Scheduled(fixedRate = FIXED_RATE)
    public void checkForUpdates() {
        doCheck();
    }

    @Override
    protected String getBaseUrl() {
        return config.stackOverflow().baseUrl();
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
            Optional<StackOverflowAnswer> answer = stackOverflowClient.getLatestAnswerOrComment(link);
            String previous = lastAnswerIds.get(link);
            answer.ifPresent(a -> {
                String idStr = String.valueOf(a.answerId());
                if (previous == null || !previous.equals(idStr)) {
                    lastAnswerIds.put(link, idStr);
                    if (previous != null) {
                        sendNotification(link, a);
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error processing SO link {}: {}", link, e.getMessage());
        }
    }

    private void sendNotification(String link, StackOverflowAnswer answer) {
        log.info("New StackOverflow answer for {}: {} by {}", link, answer.questionTitle(), answer.username());
        notificationService.sendNotification(link, answer);
    }
}
