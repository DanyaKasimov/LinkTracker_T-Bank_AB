package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.request.SubscriptionRequestDto;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.model.Link;
import java.util.Collection;
import java.util.List;

public interface LinkService {

    LinkResponse save(Long chatId, SubscriptionRequestDto dto);

    LinkResponse delete(Long chatId, String link);

    ListLinksResponse getAllLinks(Long chatId);

    List<String> findAllLinksByLink(String link);

    Collection<Long> findAllChatIdsByLink(String link);

    Link findByLinkName(String link);
}
