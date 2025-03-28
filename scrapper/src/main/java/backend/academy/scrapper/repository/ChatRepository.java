package backend.academy.scrapper.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository {

    void save(Long id);

    void delete(Long id);

    boolean isActive(Long id);
}
