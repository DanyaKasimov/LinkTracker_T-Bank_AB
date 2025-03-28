package backend.academy.scrapper.service;

import backend.academy.scrapper.Model.Subscription;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface SubscriptionService {

    Subscription save(Long chatId, SubscriptionRequestDto dto);

    LinkResponse delete(Long chatId, String link);

    ListLinksResponse getAllLinks(Long chatId);

    List<String> findAllLinksGitHub();

    List<String> findAllLinksStackOverflow() ;

    List<Long> findAllChatIdsByLink(String link);
}
