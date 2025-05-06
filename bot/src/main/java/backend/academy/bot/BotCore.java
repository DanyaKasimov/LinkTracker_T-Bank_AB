package backend.academy.bot;

import backend.academy.bot.config.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class BotCore {

    private final TelegramBot bot;

    public BotCore(BotConfig config) {
        this.bot = new TelegramBot(config.telegramToken());
    }

    @PostConstruct
    public void registerCommands() {
        BotCommand[] commands = {
            new BotCommand("/start", "Запустить бота"),
            new BotCommand("/help", "Показать список всех команд"),
            new BotCommand("/track", "Подписаться на обновление ссылки"),
            new BotCommand("/untrack", "Отписаться от обновления ссылки"),
            new BotCommand("/list", "Получить все отслеживаемые ссылки"),
            new BotCommand("/stop", "Завершить работу бота"),
        };
        bot.execute(new SetMyCommands(commands));
    }
}
