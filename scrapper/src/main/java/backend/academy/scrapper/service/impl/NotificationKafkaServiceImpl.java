package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.config.kafka.KafkaTopics;
import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.NotificationService;
import backend.academy.scrapper.dto.LinkUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationKafkaServiceImpl implements NotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final LinkService linkService;
    private final KafkaTopics topics;
    private final ObjectMapper objectMapper;

    @Override
    public void sendNotification(String nameLink, UserMessage message) {
        log.info("Поступил запрос на отправку уведомлений через Kafka.");

        String description = message.toString();
        if (description.isEmpty()) return;

        Link link = linkService.findByLinkName(nameLink);

        LinkUpdateDto updateDto = LinkUpdateDto.builder()
            .id(link.getId())
            .url(nameLink)
            .description(description)
            .tgChatIds(linkService.findAllChatIdsByLink(nameLink))
            .build();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(updateDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

        kafkaTemplate.send(topics.notification(), payload)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Ошибка при отправке Kafka-сообщения в основной топик", ex);
                } else {
                    log.info("Успешно отправлено сообщение: {}", result.getRecordMetadata());
                }
            });
    }
}
