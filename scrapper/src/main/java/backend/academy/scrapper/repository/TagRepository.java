package backend.academy.scrapper.repository;

import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.Model.Tag;
import java.util.Collection;
import java.util.List;

public interface TagRepository {
    Tag findByName(String name);

    boolean existsByName(String name);

    void save(Tag tag);

    void saveAll(Collection<Tag> tags);

    void delete(Tag tag);

    void deleteAllByLink(Link link);

    List<Tag> findAllByLink(Link link);

    void deleteAllByLinkIn(Collection<Link> links);
}
