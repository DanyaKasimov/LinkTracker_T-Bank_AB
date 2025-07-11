package backend.academy.bot.services;

import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.Subscription;
import backend.academy.bot.dto.SubscriptionRequestDto;

public interface SubscriptionService {

    Subscription addSubscription(final String chatId, final SubscriptionRequestDto data);

    LinkResponse removeSubscription(String chatId, String url);

    ListLinksResponse getSubscriptions(String chatId);
}
