package backend.academy.bot.controller;

import backend.academy.bot.api.BotApi;
import backend.academy.bot.dto.LinkUpdateDto;
import backend.academy.bot.services.ChatManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BotController implements BotApi {

    private final ChatManagementService chatManagementService;

    @Override
    public void updateMessage(final LinkUpdateDto dto) {
        log.atInfo()
                .setMessage("Поступил запрос на обновление сообщений.")
                .addKeyValue("url", dto.getUrl())
                .addKeyValue("chatsIds", dto.getTgChatIds())
                .addKeyValue("description", dto.getDescription())
                .log();

        chatManagementService.sendUpdates(dto);
    }
}
