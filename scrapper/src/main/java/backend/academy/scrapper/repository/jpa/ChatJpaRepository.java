package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.Model.Chat;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "ORM")
public interface ChatJpaRepository extends JpaRepository<Chat, Long> {

    Boolean existsByUserId(Long userId);

    Optional<Chat> findByUserId(Long userId);
}
