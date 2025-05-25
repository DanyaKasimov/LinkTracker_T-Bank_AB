package backend.academy.scrapper.controller;

import backend.academy.scrapper.api.SubscriptionApi;
import backend.academy.scrapper.dto.request.SubscriptionRequestDto;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
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
    public LinkResponse addSubscription(final Long tgChatId, final SubscriptionRequestDto dto) {
        log.info("Поступил запрос на добавление ссылки на отслеживание. ID: {}, ChatID: {}", dto.getLink(), tgChatId);

        return linkService.save(tgChatId, dto);
    }

    @Override
    public ListLinksResponse getSubscription(final Long tgChatId) {
        log.info("Поступил запрос на получение списка ссылок. ChatID: {}", tgChatId);

        return linkService.getAllLinks(tgChatId);
    }

    @Override
    public LinkResponse deleteSubscription(final Long tgChatId, final String link) {
        log.info("Поступил запрос на удаление подписки. Link: {}, ChatID: {}", tgChatId, link);

        return linkService.delete(tgChatId, link);
    }
}
