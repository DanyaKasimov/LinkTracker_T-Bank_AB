package backend.academy.scrapper.watcher;

import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.NotificationService;
import backend.academy.scrapper.utils.WatcherUtils;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Slf4j
public abstract class AbstractWatcher<T> {

    private final LinkService linkService;

    protected final NotificationService notificationService;

    private final ExecutorService executor;

    protected AbstractWatcher(LinkService linkService, NotificationService notificationService, int threadCount) {
        this.linkService = linkService;
        this.notificationService = notificationService;
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    protected abstract String getBaseUrl();

    protected abstract int getBatchSize();

    protected abstract int getThreadCount();

    protected abstract void fetchAndNotify(String link);

    protected void doCheck() {
        List<String> allLinks = linkService.findAllLinksByLink(getBaseUrl());
        if (CollectionUtils.isEmpty(allLinks)) {
            return;
        }
        List<List<String>> batches = WatcherUtils.partitionList(allLinks, getBatchSize());
        for (List<String> batch : batches) {
            WatcherUtils.processInThreads(batch, executor, getThreadCount(), this::fetchAndNotify);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10_000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                log.warn("ExecutorService did not terminate in time, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted during shutdown: {}", e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
