package backend.academy.bot;


import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TelegramBotService {

    private final CommandExecutor commandHandler;

    private final StateHandler stateHandler;

    private final MessageSender messageSender;

    public TelegramBotService(BotCore botCore, CommandExecutor commandExecutor, StateHandler stateHandler, MessageSender messageSender) {
        this.commandHandler = commandExecutor;
        this.messageSender = messageSender;
        this.stateHandler = stateHandler;

        botCore.getBot().setUpdatesListener(updates -> {
            updates.forEach(this::processUpdate);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void processUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) return;

        String chatId = update.message().chat().id().toString();
        String text = update.message().text();

        try {
            if (stateHandler.hasState(chatId)) {
                stateHandler.handleState(chatId, text);
            } else {
                commandHandler.execute(chatId, text);
            }
        } catch (Exception e) {
            log.atError()
                .setMessage("Непредвиденная ошибка")
                .addKeyValue("error", e.getMessage())
                .log();
            messageSender.send(chatId, "Произошла ошибка. Попробуйте снова.");
        }
    }

}
