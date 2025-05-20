package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.LinkUpdateDto;
import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transport.type", havingValue = "HTTP", matchIfMissing = true)
public class NotificationHttpServiceImpl implements NotificationService {

    private final RestAccessor restAccessor;

    private final LinkService linkService;

    @Override
    public void sendNotification(String nameLink, UserMessage message) {
        String description = message.toString();
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
