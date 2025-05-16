package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import java.util.Collection;
import java.util.List;

public interface LinkService {

    LinkResponse save(Long chatId, SubscriptionRequestDto dto);

    LinkResponse delete(Long chatId, String link);

    ListLinksResponse getAllLinks(Long chatId);

    List<String> findAllLinksByLink(String link);

    Collection<Long> findAllChatIdsByLink(String link);
}
