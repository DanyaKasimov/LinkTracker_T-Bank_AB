package backend.academy.scrapper.controller;

import backend.academy.scrapper.api.SubscriptionApi;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import backend.academy.scrapper.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionApi {

    private final LinkService linkService;

    @Override
    public LinkResponse addSubscription(Long tgChatId, SubscriptionRequestDto dto) {
        log.atInfo()
                .setMessage("Поступил запрос на добавление ссылки на отслеживание")
                .addKeyValue("Link", dto.getLink())
                .addKeyValue("ChatID", tgChatId)
                .log();

        return linkService.save(tgChatId, dto);
    }

    @Override
    public ListLinksResponse getSubscription(Long tgChatId) {
        log.atInfo()
                .setMessage("Поступил запрос на получение списка ссылок")
                .addKeyValue("ChatID", tgChatId)
                .log();

        return linkService.getAllLinks(tgChatId);
    }

    @Override
    public LinkResponse deleteSubscription(Long tgChatId, String link) {
        log.atInfo()
                .setMessage("Поступил запрос на удаление подписки")
                .addKeyValue("ChatID", tgChatId)
                .addKeyValue("Link", link)
                .log();

        return linkService.delete(tgChatId, link);
    }
}
