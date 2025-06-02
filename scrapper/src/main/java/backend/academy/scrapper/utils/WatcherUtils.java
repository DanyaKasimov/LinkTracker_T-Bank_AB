package backend.academy.scrapper.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WatcherUtils {
    private WatcherUtils() {}

    public static <T> List<List<T>> partitionList(List<T> items, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < items.size(); i += size) {
            partitions.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return partitions;
    }

    public static <T> void processInThreads(
            List<T> items, ExecutorService executor, int threadCount, Consumer<T> processor) {
        if (items.isEmpty()) {
            return;
        }
        List<List<T>> partitions = partitionList(items, threadCount);
        List<Callable<Void>> tasks = partitions.stream()
                .map(chunk -> (Callable<Void>) () -> {
                    chunk.forEach(processor);
                    return null;
                })
                .toList();
        try {
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("Watcher task error: {}", e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Watcher threads interrupted: {}", e.getMessage());
        }
    }
}
