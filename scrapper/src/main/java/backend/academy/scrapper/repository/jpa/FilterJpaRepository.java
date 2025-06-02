package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import java.util.Collection;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "ORM")
public interface FilterJpaRepository extends JpaRepository<Filter, Long> {

    Filter findByName(String name);

    boolean existsByName(String name);

    void deleteAllByLink(Link link);

    List<Filter> findAllByLink(Link link);

    void deleteAllByLinkIn(Collection<Link> links);
}
