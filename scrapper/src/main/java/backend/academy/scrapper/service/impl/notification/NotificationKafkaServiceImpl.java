package backend.academy.scrapper.service.impl.notification;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.dto.response.LinkUpdateDto;
import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.exceptions.JsonExceptions;
import backend.academy.scrapper.exceptions.KafkaException;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.NotificationService;
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
    private final ScrapperConfig scrapperConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void sendNotification(String linkName, UserMessage message) {
        log.info("Поступил запрос на отправку уведомлений через Kafka.");

        LinkUpdateDto updateDto = validateAndBuildUpdateDto(linkService, linkName, message, log);
        if (updateDto == null) return;

        String payload = createPayload(updateDto);
        String topic = scrapperConfig.kafka().topics().notification();
        try {
            kafkaTemplate.send(topic, payload).get();
        } catch (Exception e) {
            throw new KafkaException("Ошибка Kafka: " + e.getMessage());
        }
    }

    private String createPayload(LinkUpdateDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new JsonExceptions(e.getMessage());
        }
    }
}
