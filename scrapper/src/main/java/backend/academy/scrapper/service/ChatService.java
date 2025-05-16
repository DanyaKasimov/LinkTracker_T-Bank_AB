package backend.academy.scrapper.service;

import backend.academy.scrapper.Model.Chat;

public interface ChatService {

    void registerChat(Long id);

    void deleteChat(Long id);

    Chat findById(Long id);
}
