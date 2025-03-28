package backend.academy.bot;

import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.Subscription;
import backend.academy.bot.services.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;

class BotTest {
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private StateHandler stateHandler;
    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private CommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSendErrorMessageForUnknownCommand() {
        String chatId = "12345";
        String unknownCommand = "/unknown";
        when(stateHandler.isActive(chatId)).thenReturn(true);

        commandHandler.handleCommand(chatId, unknownCommand);

        verify(messageSender).send(chatId, "Неизвестная команда. Введите /help для списка команд.");
    }

    @Test
    void shouldSendSubscriptionsForListCommand() {
        String chatId = "12345";
        ListLinksResponse response = new ListLinksResponse(List.of(
            new Subscription("1234", "https://example.com", List.of("tag"), List.of("filter"))), 1);
        when(stateHandler.isActive(chatId)).thenReturn(true);
        when(subscriptionService.getSubscriptions(chatId)).thenReturn(response);

        commandHandler.handleCommand(chatId, "/list");

        verify(messageSender).send(chatId, "Link: https://example.com\nTags: tag\nFilters: filter\n");
    }

    @Test
    void shouldSendNoSubscriptionsMessageWhenEmpty() {
        String chatId = "12345";
        ListLinksResponse response = new ListLinksResponse(Collections.emptyList(), 0);
        when(stateHandler.isActive(chatId)).thenReturn(true);
        when(subscriptionService.getSubscriptions(chatId)).thenReturn(response);

        commandHandler.handleCommand(chatId, "/list");

        verify(messageSender).send(chatId, "У вас нет подписок.");
    }
}
