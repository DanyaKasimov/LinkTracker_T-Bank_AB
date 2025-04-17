package backend.academy.bot.commands.impl;

import backend.academy.bot.MessageSender;
import backend.academy.bot.StateHandler;
import backend.academy.bot.commands.Command;
import backend.academy.bot.constants.BotCommand;
import backend.academy.bot.constants.BotState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UntrackCommand implements Command {
    private final StateHandler stateHandler;
    private final MessageSender messageSender;

    @Override
    public boolean supports(String command) {
        return BotCommand.UNTRACK.getCommand().equalsIgnoreCase(command);
    }

    @Override
    public void execute(String chatId, String text) {
        stateHandler.setState(chatId, BotState.AWAITING_UNTRACK_URL);
        messageSender.send(chatId, "Введите ссылку для отмены отслеживания:");
    }
}
