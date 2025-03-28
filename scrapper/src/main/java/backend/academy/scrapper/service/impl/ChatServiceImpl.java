package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.exceptions.NotFoundDataException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    @Override
    public void registerChat(final Long id) {
        if (chatRepository.isActive(id)) {
            throw new InvalidDataException("Пользователь уже зарегистрирован.");
        }
        chatRepository.save(id);
    }

    @Override
    public void deleteChat(Long id) {
        if (!chatRepository.isActive(id)) {
            throw new NotFoundDataException("Пользователь не найден.");
        }
        chatRepository.delete(id);
    }
}
