package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.Model.Chat;
import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.exceptions.NotFoundDataException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.service.ChatService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    private final LinksRepository linksRepository;

    private final TagRepository tagRepository;

    private final FilterRepository filterRepository;

    @Override
    @Transactional
    public void registerChat(final Long id) {
        if (chatRepository.existsByUserId(id)) {
            throw new InvalidDataException("Пользователь уже зарегистрирован.");
        }

        Chat chat = new Chat(null, id);
        chatRepository.save(chat);
    }

    @Override
    @Transactional
    public void deleteChat(Long id) {
        Chat chat = findById(id);

        List<Link> links = linksRepository.findAllByChat(chat);

        if (!links.isEmpty()) {
            tagRepository.deleteAllByLinkIn(links);
            filterRepository.deleteAllByLinkIn(links);
            linksRepository.deleteAll(links);
        }

        chatRepository.delete(chat);
    }

    @Override
    public Chat findById(Long id) {
        return chatRepository.findByUserId(id).orElseThrow(() -> new NotFoundDataException("Чат не найден."));
    }
}
