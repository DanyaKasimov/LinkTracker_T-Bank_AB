package backend.academy.bot.constants;

import lombok.Getter;

@Getter
public enum BotCommand {
    START("/start"),
    STOP("/stop"),
    TRACK("/track"),
    UNTRACK("/untrack"),
    LIST("/list"),
    HELP("/help");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }
}
