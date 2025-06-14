package backend.academy.bot.services.impl;

import backend.academy.bot.MessageSender;
import backend.academy.bot.accessor.RestAccessor;
import backend.academy.bot.dto.LinkUpdateDto;
import backend.academy.bot.services.ChatManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatManagementServiceImpl implements ChatManagementService {

    private final RestAccessor accessor;

    private final MessageSender messageSender;

    @Override
    @CacheEvict(value = "subscriptions", key = "#id")
    public String registerChat(final String id) {
        var url = String.format("/tg-chat/%s", id);
        accessor.post(url);
        return "Чат зарегистрирован.";
    }

    @Override
    @CacheEvict(value = "subscriptions", key = "#id")
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
}
