package backend.academy.bot.commands.impl;

import backend.academy.bot.MessageSender;
import backend.academy.bot.commands.Command;
import backend.academy.bot.constants.BotCommand;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.Subscription;
import backend.academy.bot.exceptions.ErrorResponseException;
import backend.academy.bot.services.SubscriptionService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCommand implements Command {
    private final SubscriptionService subscriptionService;
    private final MessageSender messageSender;

    @Override
    public boolean supports(String command) {
        return BotCommand.LIST.getCommand().equalsIgnoreCase(command);
    }

    @Override
    public void execute(String chatId, String text) {
        try {
            ListLinksResponse links = subscriptionService.getSubscriptions(chatId);
            messageSender.send(
                    chatId,
                    links.getLinks().isEmpty()
                            ? "У вас нет подписок."
                            : links.getLinks().stream()
                                    .map(Subscription::toString)
                                    .collect(Collectors.joining("\n")));
        } catch (ErrorResponseException e) {
            messageSender.send(chatId, e.getMessage());
        }
    }
}
