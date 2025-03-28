package backend.academy.bot;

import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSender {

    private final BotCore bot;

    public void send(String chatId, String message) {
        bot.getBot().execute(new SendMessage(chatId, message));
    }
}
