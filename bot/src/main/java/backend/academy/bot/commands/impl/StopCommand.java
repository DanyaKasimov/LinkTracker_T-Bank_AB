package backend.academy.bot.commands.impl;

import backend.academy.bot.MessageSender;
import backend.academy.bot.StateHandler;
import backend.academy.bot.commands.Command;
import backend.academy.bot.constants.BotCommand;
import backend.academy.bot.exceptions.ErrorResponseException;
import backend.academy.bot.services.ChatManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StopCommand implements Command {
    private final StateHandler stateHandler;
    private final ChatManagementService chatManagementService;
    private final MessageSender messageSender;

    @Override
    public boolean supports(String command) {
        return BotCommand.STOP.getCommand().equalsIgnoreCase(command);
    }

    @Override
    public void execute(String chatId, String text) {
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
        messageSender.send(chatId, "Бот отключен. Введите " + BotCommand.START.getCommand() + " для повторного включения.");
    }
}
