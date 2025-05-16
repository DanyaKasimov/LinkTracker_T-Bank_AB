package backend.academy.scrapper.repository;

import backend.academy.scrapper.Model.Chat;
import java.util.Optional;


public interface ChatRepository {
    boolean existsByUserId(Long userId);

    void save(Chat chat);

    void delete(Chat chat);

    Optional<Chat> findByUserId(Long userId);
}
