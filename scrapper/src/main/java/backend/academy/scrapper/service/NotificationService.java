package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.dto.response.LinkUpdateDto;
import backend.academy.scrapper.model.Link;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;

public interface NotificationService {

    void sendNotification(String link, UserMessage message);

    default Optional<LinkUpdateDto> validateAndBuildUpdateDto(
            LinkService linkService, String linkName, UserMessage message, Logger log) {
        if (message == null) {
            log.warn("Попытка отправить пустое сообщение для ссылки: {}", linkName);
            return Optional.empty();
        }

        String description = message.toString();
        if (description.isEmpty()) {
            log.warn("Попытка отправить уведомление с пустым описанием для ссылки: {}", linkName);
            return Optional.empty();
        }

        Link link = linkService.findByLinkName(linkName);

        Collection<Long> chatIds = linkService.findAllChatIdsByLink(linkName);
        if (CollectionUtils.isEmpty(chatIds)) {
            log.warn("Нет получателей уведомления для ссылки: {}", linkName);
            return Optional.empty();
        }

        return Optional.of(LinkUpdateDto.builder()
                .id(link.getId())
                .url(linkName)
                .description(description)
                .tgChatIds(chatIds)
                .build());
    }
}
