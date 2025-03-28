package backend.academy.bot;

import backend.academy.bot.constants.BotState;
import backend.academy.bot.constants.UserMessage;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.Subscription;
import backend.academy.bot.exceptions.ErrorResponseException;
import backend.academy.bot.services.ChatManagementService;
import backend.academy.bot.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final SubscriptionService subscriptionService;
    private final ChatManagementService chatManagementService;
    private final StateHandler stateHandler;
    private final MessageSender messageSender;

    public void handleCommand(String chatId, String text) {
        if (!stateHandler.isActive(chatId) && !text.trim().equals("/start")) {
            messageSender.send(chatId, "Введите /start для начала работы.");
            return;
        }
        String command = text.split(" ")[0];
        switch (command.toLowerCase()) {
            case "/start" -> startChat(chatId);
            case "/stop" -> stopChat(chatId);
            case "/track" -> trackCommand(chatId);
            case "/untrack" -> untrackCommand(chatId, text);
            case "/list" -> listSubscriptions(chatId);
            case "/help" -> messageSender.send(chatId, UserMessage.HELP);
            default -> messageSender.send(chatId, "Неизвестная команда. Введите /help для списка команд.");
        }
    }

    private void startChat(String chatId) {
        if (stateHandler.isActive(chatId)) {
            messageSender.send(chatId, "Вы уже зарегистрированы.");
            return;
        }
        try {
            chatManagementService.registerChat(chatId);
        } catch (ErrorResponseException e) {
            messageSender.send(chatId, e.getMessage());
            return;
        }
        stateHandler.setActive(chatId);
        messageSender.send(chatId, UserMessage.WELCOME);
    }

    private void stopChat(String chatId) {
        if (!stateHandler.isActive(chatId)) {
            messageSender.send(chatId, "Вы не зарегистрированы.");
            return;
        }
        try {
            chatManagementService.deleteChat(chatId);
        } catch (ErrorResponseException e) {
            messageSender.send(chatId, e.getMessage());
            return;
        }
        stateHandler.deleteActive(chatId);
        messageSender.send(chatId, "Бот отключен. Введите /start для повторного включения.");
    }

    private void trackCommand(String chatId) {
        stateHandler.setState(chatId, BotState.AWAITING_URL);
        messageSender.send(chatId, "Введите ссылку для отслеживания:");
    }

    private void untrackCommand(String chatId, String text) {
        stateHandler.setState(chatId, BotState.AWAITING_UNTRACK_URL);
        messageSender.send(chatId, "Введите ссылку для отмены отслеживания:");
    }

    private void listSubscriptions(String chatId) {
        try {
            ListLinksResponse links = subscriptionService.getSubscriptions(chatId);
            messageSender.send(chatId,
                links.getLinks().isEmpty()
                    ? "У вас нет подписок."
                    : links.getLinks().stream()
                    .map(Subscription::toString)
                    .collect(Collectors.joining("\n"))

            );
        } catch (ErrorResponseException e) {
            messageSender.send(chatId, e.getMessage());
        }
    }
}
