package backend.academy.bot.commands.impl;

import backend.academy.bot.MessageSender;
import backend.academy.bot.commands.Command;
import backend.academy.bot.constants.BotCommand;
import backend.academy.bot.constants.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements Command {
    private final MessageSender messageSender;

    @Override
    public boolean supports(String command) {
        return BotCommand.HELP.getCommand().equalsIgnoreCase(command);
    }

    @Override
    public void execute(String chatId, String text) {
        messageSender.send(chatId, UserMessage.HELP);
    }
}
