package backend.academy.bot.commands.impl;

import backend.academy.bot.MessageSender;
import backend.academy.bot.StateHandler;
import backend.academy.bot.commands.Command;
import backend.academy.bot.constants.BotCommand;
import backend.academy.bot.constants.BotState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommand implements Command {
    private final StateHandler stateHandler;
    private final MessageSender messageSender;

    @Override
    public boolean supports(String command) {
        return BotCommand.TRACK.getCommand().equalsIgnoreCase(command);
    }

    @Override
    public void execute(String chatId, String text) {
        stateHandler.setState(chatId, BotState.AWAITING_URL);
        messageSender.send(chatId, "Введите ссылку для отслеживания:");
    }
}
