package backend.academy.scrapper.service;


import backend.academy.scrapper.dto.UserMessage;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface NotificationService {

    void sendNotification(String link, UserMessage message);

}
