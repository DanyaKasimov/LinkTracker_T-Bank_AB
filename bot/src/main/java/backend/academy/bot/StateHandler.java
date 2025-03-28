package backend.academy.bot;

import backend.academy.bot.constants.BotState;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.Subscription;
import backend.academy.bot.dto.SubscriptionRequestDto;
import backend.academy.bot.exceptions.ErrorResponseException;
import backend.academy.bot.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StateHandler {

    private final SubscriptionService subscriptionService;
    private final MessageSender messageSender;
    private final Map<String, BotState> chatStates = new HashMap<>();
    private final Map<String, SubscriptionRequestDto> chatData = new HashMap<>();
    private final Map<String, Boolean> chatActive = new HashMap<>();


    public void handleState(String chatId, String text) {
        if (!chatStates.containsKey(chatId)) return;

        switch (chatStates.get(chatId)) {
            case AWAITING_URL -> processUrl(chatId, text);
            case AWAITING_UNTRACK_URL -> processUntrackUrl(chatId, text);
            case AWAITING_TAGS -> processTags(chatId, text);
            case AWAITING_FILTERS -> processFilters(chatId, text);
            default -> messageSender.send(chatId, "Неизвестная команда. Введите /help.");
        }
    }

    private void processUrl(String chatId, String url) {
        chatData.put(chatId, new SubscriptionRequestDto().setLink(url));
        chatStates.put(chatId, BotState.AWAITING_TAGS);
        messageSender.send(chatId, "Введите теги или отправьте '-' для пропуска:");
    }

    private void processUntrackUrl(String chatId, String url) {
        try {
            LinkResponse response = subscriptionService.removeSubscription(chatId, url);
            messageSender.send(response.getId(), "Подписка снята с " + response.getLink());
        } catch (ErrorResponseException e) {
            messageSender.send(chatId, e.getMessage());
        }
        chatStates.remove(chatId);
    }

    private void processTags(String chatId, String text) {
        SubscriptionRequestDto data = chatData.get(chatId);
        if (!text.equalsIgnoreCase("-")) {
            data.setTags(Arrays.asList(text.split(" ")));
        }
        chatStates.put(chatId, BotState.AWAITING_FILTERS);
        messageSender.send(chatId, "Введите фильтры или отправьте '-' для пропуска:");
    }

    private void processFilters(String chatId, String text) {
        SubscriptionRequestDto data = chatData.remove(chatId);
        chatStates.remove(chatId);

        if (!text.equalsIgnoreCase("-")) {
            data.setFilters(Arrays.asList(text.split(" ")));
        }

        showData(chatId, data);
    }

    public void showData(String chatId, SubscriptionRequestDto data) {
        try {
            Subscription subscription = subscriptionService.addSubscription(chatId, data);
            messageSender.send(chatId, "Подписка оформлена на " + subscription.getLink());

            if (subscription.getTags() != null) {
                messageSender.send(chatId, "Теги: " + String.join(", ", subscription.getTags()));
            }
            if (subscription.getFilters() != null) {
                messageSender.send(chatId, "Фильтры: " + String.join(", ", subscription.getFilters()));
            }
        } catch (ErrorResponseException e) {
            messageSender.send(chatId, e.getMessage());
        }
    }

    public void setState(String chatId, BotState botState) {
        chatStates.put(chatId, botState);
    }

    public void setActive(String chatId) {
        chatActive.put(chatId, true);
    }

    public void deleteActive(String chatId) {
        chatActive.remove(chatId);
    }

    public boolean hasState(String chatId) {
        return chatStates.containsKey(chatId);
    }

    public boolean isActive(String chatId) {
        return chatActive.containsKey(chatId);
    }
}
