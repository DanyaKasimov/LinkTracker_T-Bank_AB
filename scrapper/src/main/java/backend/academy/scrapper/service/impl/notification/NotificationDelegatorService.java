package backend.academy.scrapper.service.impl.notification;

import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDelegatorService implements NotificationService {

    private final NotificationService primary;
    private final NotificationService secondary;

    @Override
    public void sendNotification(String linkName, UserMessage message) {
        try {
            log.info("Отправка через primary транспорт: {}", primary.getClass().getSimpleName());
            primary.sendNotification(linkName, message);
        } catch (Exception e) {
            log.warn(
                    "Primary notification transport failed, fallback to secondary: {}",
                    secondary.getClass().getSimpleName(),
                    e);
            try {
                secondary.sendNotification(linkName, message);
            } catch (Exception ex) {
                log.error("Both notification transports failed.", ex);
                throw new InvalidDataException(
                        "Не удалось отправить уведомление ни через один транспорт. Message: " + ex.getMessage());
            }
        }
    }
}
