package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.UserMessage;

public interface NotificationService {

    void sendNotification(String link, UserMessage message);
}
