package backend.academy.scrapper.utils;

import backend.academy.scrapper.dto.GitHubUpdate;
import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.dto.UserMessage;

public class MessageGenerator {

    public static String generateDescription(UserMessage message) {
        if (message instanceof GitHubUpdate gitHubUpdate) {
            return String.format(
                "[GitHub] %s\nAuthor: %s\nCreated: %s\n%s",
                gitHubUpdate.title(),
                gitHubUpdate.username(),
                gitHubUpdate.createdAt(),
                gitHubUpdate.preview()
            );
        } else if (message instanceof StackOverflowAnswer answer) {
            return String.format(
                "**%s** ответил на вопрос \"%s\":\n%s",
                answer.username(),
                answer.questionTitle(),
                answer.preview()
            );
        }
        return "";
    }
}
