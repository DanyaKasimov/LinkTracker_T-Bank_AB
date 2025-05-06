package backend.academy.bot.constants;

public class UserMessage {
    public static final String WELCOME =
        """
        Добро пожаловать!
        Введите команду /track для отслеживания ресурса.
        Если есть вопросы, введите /help.
        """;

    public static final String HELP =
        """
        Доступные команды:
        "/track" - начать отслеживание ресурса.
        "/untrack" - отменить отслеживание ресурса.
        "/list" - просмотр отслеживаемых ресурсов.
        "/stop" - остановить работу бота.
        """;
}
