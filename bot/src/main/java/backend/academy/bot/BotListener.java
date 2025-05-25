package backend.academy.bot;

import backend.academy.bot.dto.LinkUpdateDto;
import backend.academy.bot.services.ChatManagementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotListener {

    private final ObjectMapper objectMapper;

    private final ChatManagementService ChatManagementService;

    @KafkaListener(topics = "${app.kafka.topics.notification}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeUpdates(String dtoJson) {
        log.info("Сообщение получено из Kafka: DTO: {}", dtoJson);
        LinkUpdateDto dto;
        try {
            dto = objectMapper.readValue(dtoJson, LinkUpdateDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        ChatManagementService.sendUpdates(dto);
    }
}
