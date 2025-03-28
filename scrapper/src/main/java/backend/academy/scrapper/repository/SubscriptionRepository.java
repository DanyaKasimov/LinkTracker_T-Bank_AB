package backend.academy.scrapper.repository;

import backend.academy.scrapper.Model.Subscription;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository {

    Subscription save(Long chatId, SubscriptionRequestDto dto);

    Subscription delete(Long chatId, String link);

    boolean existChatByLink(Long chatId, String link);

    boolean existByLink(String link);

    boolean existByChatId(Long chatId);

    List<Subscription> findAllByChatId(Long chatId);

    List<String> findAllLinksGitHub();

    List<String> findAllLinksStackOverflow();

    List<Long> findAllChatIdsByLink(String link);
}
