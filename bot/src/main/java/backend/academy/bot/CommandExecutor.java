package backend.academy.bot;

import backend.academy.bot.commands.Command;
import backend.academy.bot.constants.BotCommand;
import backend.academy.bot.exceptions.UnknownCommandException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandExecutor {
    private final List<Command> commands;

    private final StateHandler stateHandler;

    private final MessageSender messageSender;

    public void execute(String chatId, String text) {
        if (!stateHandler.isActive(chatId) && !text.equalsIgnoreCase(BotCommand.START.getCommand())) {
            messageSender.send(chatId, "Введите " + BotCommand.START.getCommand() + " для начала работы.");
            return;
        }

        String command = text.trim().split("\\s+")[0];
        try {
            commands.stream()
                    .filter(s -> s.supports(command))
                    .findFirst()
                    .orElseThrow(() -> new UnknownCommandException("Неизвестная команда. Введите /help."))
                    .execute(chatId, text);
        } catch (UnknownCommandException e) {
            log.atWarn()
                    .setMessage("Неизвестная команда")
                    .addKeyValue("warn", e.getMessage())
                    .log();
            messageSender.send(chatId, e.getMessage());
        }
    }
}
