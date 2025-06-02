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
        log.info("Поступил запрос на обновление сообщений. URL: {}, ChatIDs: {}", dto.getUrl(), dto.getTgChatIds());

        chatManagementService.sendUpdates(dto);
    }
}
