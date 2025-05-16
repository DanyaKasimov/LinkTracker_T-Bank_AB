package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.GitHubUpdate;
import backend.academy.scrapper.dto.LinkUpdateDto;
import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.service.NotificationService;
import backend.academy.scrapper.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHttpServiceImpl implements NotificationService {

    private final RestAccessor restAccessor;

    private final LinkService linkService;

    @Override
    public void sendNotification(String link, UserMessage message) {
        String description = generateDescription(message);

        if (description.isEmpty()) {
            return;
        }

        LinkUpdateDto updateDto = LinkUpdateDto.builder()
            .id("1")
            .url(link)
            .description(description)
            .tgChatIds(linkService.findAllChatIdsByLink(link))
            .build();

        restAccessor.postBot("/updates", updateDto, String.class);
    }

    private String generateDescription(UserMessage message) {
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
