package backend.academy.scrapper.repository.adapters;

import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.Model.Tag;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.jpa.TagJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrmTagRepositoryAdapter implements TagRepository {

    private final TagJpaRepository jpaRepository;

    @Override
    public Tag findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public void save(Tag tag) {
        jpaRepository.save(tag);
    }

    @Override
    public void saveAll(Collection<Tag> tags) {
        jpaRepository.saveAll(tags);
    }

    @Override
    public void delete(Tag tag) {
        jpaRepository.delete(tag);
    }

    @Override
    public void deleteAllByLink(Link link) {
        jpaRepository.deleteAllByLink(link);
    }

    @Override
    public List<Tag> findAllByLink(Link link) {
        return jpaRepository.findAllByLink(link);
    }

    @Override
    public void deleteAllByLinkIn(Collection<Link> links) {
        jpaRepository.deleteAllByLinkIn(links);
    }

}
