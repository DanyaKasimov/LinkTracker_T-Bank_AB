package backend.academy.scrapper.repository.adapters;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.ChatJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "ORM")
@RequiredArgsConstructor
public class OrmChatRepositoryAdapter implements ChatRepository {

    private final ChatJpaRepository chatJpaRepository;

    @Override
    public boolean existsByUserId(Long userId) {
        return chatJpaRepository.existsByUserId(userId);
    }

    @Override
    public void save(Chat chat) {
        chatJpaRepository.save(chat);
    }

    @Override
    public void delete(Chat chat) {
        chatJpaRepository.delete(chat);
    }

    @Override
    public Optional<Chat> findByUserId(Long userId) {
        return chatJpaRepository.findByUserId(userId);
    }
}
