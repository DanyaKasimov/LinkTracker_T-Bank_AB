package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.Model.Chat;
import backend.academy.scrapper.Model.Link;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "ORM")
public interface LinksJpaRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByName(String name);

    boolean existsByName(String name);

    Optional<Link> findByNameAndChat(String name, Chat chat);

    List<Link> findAllByChat(Chat chat);

    List<Link> findAllByNameStartingWith(String prefix);

    List<Link> findAllByName(String name);
}
