package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.LinkUpdateDto;
import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.service.NotificationService;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.utils.MessageGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHttpServiceImpl implements NotificationService {

    private final RestAccessor restAccessor;

    private final LinkService linkService;

    @Override
    public void sendNotification(String nameLink, UserMessage message) {

        String description = MessageGenerator.generateDescription(message);
        Link link = linkService.findByLinkName(nameLink);

        if (description.isEmpty()) {
            return;
        }

        LinkUpdateDto updateDto = LinkUpdateDto.builder()
            .id(link.getId())
            .url(nameLink)
            .description(description)
            .tgChatIds(linkService.findAllChatIdsByLink(nameLink))
            .build();

        restAccessor.postBot("/updates", updateDto, String.class);
    }
}
