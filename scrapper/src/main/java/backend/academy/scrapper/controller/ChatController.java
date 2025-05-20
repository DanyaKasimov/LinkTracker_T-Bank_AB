package backend.academy.scrapper.controller;

import backend.academy.scrapper.api.ChatApi;
import backend.academy.scrapper.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController implements ChatApi {

    private final ChatService chatService;

    @Override
    public void registerChat(final Long id) {
        log.atInfo()
                .setMessage("Поступил запрос на регистрацию чата")
                .addKeyValue("chatId", id)
                .log();

        chatService.registerChat(id);
    }

    @Override
    public void deleteChat(Long id) {
        log.atInfo()
                .setMessage("Поступил запрос на удаление чата")
                .addKeyValue("chatId", id)
                .log();

        chatService.deleteChat(id);
    }
}
