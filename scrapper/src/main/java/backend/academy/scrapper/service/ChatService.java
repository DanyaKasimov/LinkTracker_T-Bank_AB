package backend.academy.scrapper.service;

import org.springframework.stereotype.Service;

@Service
public interface ChatService {

    void registerChat(Long id);

    void deleteChat(Long id);
}
