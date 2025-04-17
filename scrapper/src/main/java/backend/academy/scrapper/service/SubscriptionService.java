package backend.academy.scrapper.service;

import backend.academy.scrapper.Model.Subscription;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import java.util.Collection;

public interface SubscriptionService {

    Subscription save(Long chatId, SubscriptionRequestDto dto);

    LinkResponse delete(Long chatId, String link);

    ListLinksResponse getAllLinks(Long chatId);

    Collection<String> findAllLinksByLink(String link);

    Collection<Long> findAllChatIdsByLink(String link);
}
