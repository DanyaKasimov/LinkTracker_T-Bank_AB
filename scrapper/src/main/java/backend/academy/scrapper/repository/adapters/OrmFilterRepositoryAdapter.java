package backend.academy.scrapper.repository.adapters;

import backend.academy.scrapper.Model.Filter;
import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.jpa.FilterJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "ORM")
@RequiredArgsConstructor
public class OrmFilterRepositoryAdapter implements FilterRepository {

    private final FilterJpaRepository filterJpaRepository;

    @Override
    public Filter findByName(String name) {
        return filterJpaRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return filterJpaRepository.existsByName(name);
    }

    @Override
    public void saveAll(Collection<Filter> filters) {
        filterJpaRepository.saveAll(filters);
    }

    @Override
    public void delete(Filter filter) {
        filterJpaRepository.delete(filter);
    }

    @Override
    public void deleteAllByLink(Link link) {
        filterJpaRepository.deleteAllByLink(link);
    }

    @Override
    public List<Filter> findAllByLink(Link link) {
        return filterJpaRepository.findAllByLink(link);
    }

    @Override
    public void deleteAllByLinkIn(Collection<Link> links) {
        filterJpaRepository.deleteAllByLinkIn(links);
    }
}
