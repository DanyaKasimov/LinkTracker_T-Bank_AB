package backend.academy.scrapper.service.impl.notification;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.dto.response.LinkUpdateDto;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHttpServiceImpl implements NotificationService {

    private final RestAccessor restAccessor;
    private final LinkService linkService;

    @Override
    public void sendNotification(String linkName, UserMessage message) {
        LinkUpdateDto updateDto = validateAndBuildUpdateDto(linkService, linkName, message, log);
        if (updateDto == null) return;

        restAccessor.postBot("/updates", updateDto, new ParameterizedTypeReference<String>() {});
    }
}
