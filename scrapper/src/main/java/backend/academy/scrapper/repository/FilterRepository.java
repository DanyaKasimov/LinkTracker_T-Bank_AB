package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import java.util.Collection;
import java.util.List;

public interface FilterRepository {
    Filter findByName(String name);

    boolean existsByName(String name);

    void saveAll(Collection<Filter> filters);

    void delete(Filter filter);

    void deleteAllByLink(Link link);

    List<Filter> findAllByLink(Link link);

    void deleteAllByLinkIn(Collection<Link> links);
}
