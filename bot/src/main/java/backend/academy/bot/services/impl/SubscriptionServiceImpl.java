package backend.academy.bot.services.impl;

import backend.academy.bot.accessor.RestAccessor;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.Subscription;
import backend.academy.bot.dto.SubscriptionRequestDto;
import backend.academy.bot.exceptions.ErrorResponseException;
import backend.academy.bot.services.SubscriptionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final RestAccessor accessor;

    @Override
    @CacheEvict(value = "subscriptions", key = "#chatId")
    public Subscription addSubscription(final String chatId, final SubscriptionRequestDto data) {
        Map<String, String> params = Map.of("tgChatId", chatId);
        ResponseEntity<Subscription> response = accessor.post("/links", data, Subscription.class, params);
        return response.getBody();
    }

    @Override
    @CacheEvict(value = "subscriptions", key = "#chatId")
    public LinkResponse removeSubscription(final String chatId, final String url) throws ErrorResponseException {
        Map<String, String> params = Map.of("tgChatId", chatId);
        ResponseEntity<LinkResponse> response = accessor.delete("/links", url, LinkResponse.class, params);
        return response.getBody();
    }

    @Override
    @Cacheable(value = "subscriptions", key = "#chatId")
    public ListLinksResponse getSubscriptions(final String chatId) throws ErrorResponseException {
        Map<String, String> params = Map.of("tgChatId", chatId);
        ResponseEntity<ListLinksResponse> response = accessor.get("/links", ListLinksResponse.class, params);
        return response.getBody();
    }
}
