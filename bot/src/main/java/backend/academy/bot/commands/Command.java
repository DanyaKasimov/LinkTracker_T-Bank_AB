package backend.academy.bot.commands;

public interface Command {
    boolean supports(String command);

    void execute(String chatId, String text);
}
