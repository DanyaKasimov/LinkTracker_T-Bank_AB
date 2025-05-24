package backend.academy.bot.services.impl;

import backend.academy.bot.MessageSender;
import backend.academy.bot.accessor.RestAccessor;
import backend.academy.bot.dto.LinkUpdateDto;
import backend.academy.bot.services.ChatManagementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatManagementServiceImpl implements ChatManagementService {

    private final RestAccessor accessor;

    private final MessageSender messageSender;

    private final ObjectMapper objectMapper;

    @Override
    public String registerChat(final String id) {
        var url = String.format("/tg-chat/%s", id);
        accessor.post(url);
        return "Чат зарегистрирован.";
    }

    @Override
    public String deleteChat(final String id) {
        var url = String.format("/tg-chat/%s", id);
        accessor.delete(url);
        return "Чат удален.";
    }

    @Override
    public void sendUpdates(final LinkUpdateDto dto) {
        String message = String.format("Обновление по ссылке %s. Описание: %s", dto.getUrl(), dto.getDescription());
        dto.getTgChatIds().forEach(id -> {
            messageSender.send(String.valueOf(id), message);
        });
    }

    @KafkaListener(topics = "${kafka.topics.notification}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeUpdates(String dtoJson) {
        log.info("Сообщение получено из Kafka: DTO: {}", dtoJson);
        LinkUpdateDto dto;
        try {
            dto = objectMapper.readValue(dtoJson, LinkUpdateDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        sendUpdates(dto);
    }
}
