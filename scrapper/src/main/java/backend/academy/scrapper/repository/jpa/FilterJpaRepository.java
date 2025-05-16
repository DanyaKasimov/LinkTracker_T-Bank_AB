package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.Model.Filter;
import backend.academy.scrapper.Model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface FilterJpaRepository extends JpaRepository<Filter, Long> {

    Filter findByName(String name);

    boolean existsByName(String name);

    void deleteAllByLink(Link link);

    List<Filter> findAllByLink(Link link);

    void deleteAllByLinkIn(Collection<Link> links);
}
