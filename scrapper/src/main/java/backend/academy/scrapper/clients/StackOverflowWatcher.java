package backend.academy.scrapper.clients;

import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.service.NotificationService;
import backend.academy.scrapper.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowWatcher {

    private final StackOverflowClient stackOverflowClient;
    private final LinkService linkService;
    private final NotificationService notificationService;

    private final Map<String, Integer> lastAnswerIds = new HashMap<>();
    private static final String SO_API_URL = "https://stackoverflow.com/";
    private static final int THREAD_COUNT = 4;
    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedRate = 100_000)
    public void checkForUpdates() {
        List<String> allLinks = linkService.findAllLinksByLink(SO_API_URL);
        if (allLinks.isEmpty()) return;

        List<String> batch = allLinks.subList(0, Math.min(BATCH_SIZE, allLinks.size()));
        List<List<String>> partitions = partitionList(batch, THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (List<String> partition : partitions) {
            executor.submit(() -> partition.forEach(this::processLink));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(90, TimeUnit.SECONDS)) {
                log.warn("Timeout при завершении обработки StackOverflow ссылок");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private List<List<String>> partitionList(List<String> links, int parts) {
        int size = links.size();
        int partitionSize = (int) Math.ceil((double) size / parts);
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < size; i += partitionSize) {
            partitions.add(links.subList(i, Math.min(i + partitionSize, size)));
        }
        return partitions;
    }

    private void processLink(String link) {
        try {
            Optional<StackOverflowAnswer> response = stackOverflowClient.getLatestAnswerOrComment(link);
            Integer storedAnswerId = lastAnswerIds.get(link);

            response.ifPresentOrElse(
                answer -> handleAnswer(link, answer, storedAnswerId),
                () -> handleNoAnswer(link)
            );
        } catch (Exception e) {
            log.atError()
                .setMessage("Ошибка при обработке ссылки StackOverflow")
                .addKeyValue("link", link)
                .addKeyValue("error", e.getMessage())
                .log();
        }
    }

    private void handleNoAnswer(String link) {
        log.atInfo()
            .setMessage("Ответов пока нет")
            .addKeyValue("link", link)
            .log();
        lastAnswerIds.putIfAbsent(link, 0);
    }

    private void handleAnswer(String link, StackOverflowAnswer answer, Integer lastAnswerId) {
        if (lastAnswerId == null || !lastAnswerId.equals(answer.answerId())) {
            lastAnswerIds.put(link, answer.answerId());

            if (lastAnswerId == null) {
                logNewAnswer(link, answer, "Первый запуск — ID ответа сохранён");
                return;
            }

            logNewAnswer(link, answer, "Обнаружен новый ответ");
            notificationService.sendNotification(link, answer);
        }
    }

    private void logNewAnswer(String link, StackOverflowAnswer answer, String message) {
        log.atInfo()
            .setMessage(message)
            .addKeyValue("answerId", answer.answerId())
            .addKeyValue("title", answer.questionTitle())
            .addKeyValue("username", answer.username())
            .addKeyValue("link", link)
            .log();
    }
}
