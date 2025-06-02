package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Tag;
import java.util.Collection;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "ORM")
public interface TagJpaRepository extends JpaRepository<Tag, Long> {

    Tag findByName(String name);

    boolean existsByName(String name);

    void deleteAllByLink(Link link);

    List<Tag> findAllByLink(Link link);

    void deleteAllByLinkIn(Collection<Link> links);
}
