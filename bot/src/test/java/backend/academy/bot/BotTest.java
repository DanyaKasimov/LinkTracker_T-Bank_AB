// File: BotTest.java
package backend.academy.bot;

import static org.mockito.Mockito.*;

import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.impl.*;
import backend.academy.bot.constants.BotCommand;
import backend.academy.bot.constants.UserMessage;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.Subscription;
import backend.academy.bot.exceptions.ErrorResponseException;
import backend.academy.bot.services.ChatManagementService;
import backend.academy.bot.services.SubscriptionService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BotTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private StateHandler stateHandler;

    @Mock
    private MessageSender messageSender;

    @Mock
    private ChatManagementService chatManagementService;

    private CommandExecutor commandExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        List<Command> commands = List.of(
                new ListCommand(subscriptionService, messageSender),
                new HelpCommand(messageSender),
                new TrackCommand(stateHandler, messageSender),
                new UntrackCommand(stateHandler, messageSender),
                new StartCommand(stateHandler, chatManagementService, messageSender),
                new StopCommand(stateHandler, chatManagementService, messageSender));
        commandExecutor = new CommandExecutor(commands, stateHandler, messageSender);
    }

    @Test
    void shouldSendErrorMessageForUnknownCommand() {
        String chatId = "12345";
        String unknownCommand = "/unknown";
        when(stateHandler.isActive(chatId)).thenReturn(true);

        commandExecutor.execute(chatId, unknownCommand);

        verify(messageSender).send(chatId, "Неизвестная команда. Введите /help.");
    }

    @Test
    void shouldSendSubscriptionsForListCommand() {
        String chatId = "12345";
        ListLinksResponse response = new ListLinksResponse(
                List.of(new Subscription("1234", "https://example.com", List.of("tag"), List.of("filter"))), 1);
        when(stateHandler.isActive(chatId)).thenReturn(true);
        when(subscriptionService.getSubscriptions(chatId)).thenReturn(response);

        commandExecutor.execute(chatId, BotCommand.LIST.getCommand());

        verify(messageSender).send(chatId, "Link: https://example.com\nTags: tag\nFilters: filter");
    }

    @Test
    void shouldSendNoSubscriptionsMessageWhenEmpty() {
        String chatId = "12345";
        ListLinksResponse response = new ListLinksResponse(Collections.emptyList(), 0);
        when(stateHandler.isActive(chatId)).thenReturn(true);
        when(subscriptionService.getSubscriptions(chatId)).thenReturn(response);

        commandExecutor.execute(chatId, BotCommand.LIST.getCommand());

        verify(messageSender).send(chatId, "У вас нет подписок.");
    }

    @Test
    void shouldPromptForTrackingUrl() {
        String chatId = "12345";
        when(stateHandler.isActive(chatId)).thenReturn(true);

        commandExecutor.execute(chatId, BotCommand.TRACK.getCommand());

        verify(stateHandler).setState(chatId, backend.academy.bot.constants.BotState.AWAITING_URL);
        verify(messageSender).send(chatId, "Введите ссылку для отслеживания:");
    }

    @Test
    void shouldPromptForUntrackingUrl() {
        String chatId = "12345";
        when(stateHandler.isActive(chatId)).thenReturn(true);

        commandExecutor.execute(chatId, BotCommand.UNTRACK.getCommand());

        verify(stateHandler).setState(chatId, backend.academy.bot.constants.BotState.AWAITING_UNTRACK_URL);
        verify(messageSender).send(chatId, "Введите ссылку для отмены отслеживания:");
    }

    @Test
    void shouldRegisterNewChat() throws ErrorResponseException {
        String chatId = "12345";
        when(stateHandler.isActive(chatId)).thenReturn(false);

        commandExecutor.execute(chatId, BotCommand.START.getCommand());

        verify(chatManagementService).registerChat(chatId);
        verify(stateHandler).setActive(chatId);
        verify(messageSender).send(chatId, UserMessage.WELCOME);
    }

    @Test
    void shouldNotRegisterIfAlreadyActive() {
        String chatId = "12345";
        when(stateHandler.isActive(chatId)).thenReturn(true);

        commandExecutor.execute(chatId, BotCommand.START.getCommand());

        verify(messageSender).send(chatId, "Вы уже зарегистрированы.");
        verifyNoInteractions(chatManagementService);
    }

    @Test
    void shouldStopActiveChat() throws ErrorResponseException {
        String chatId = "12345";
        when(stateHandler.isActive(chatId)).thenReturn(true);

        commandExecutor.execute(chatId, BotCommand.STOP.getCommand());

        verify(chatManagementService).deleteChat(chatId);
        verify(stateHandler).deleteActive(chatId);
        verify(messageSender).send(chatId, "Бот отключен. Введите /start для повторного включения.");
    }
}
