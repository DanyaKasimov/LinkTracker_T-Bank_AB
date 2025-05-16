package backend.academy.scrapper.repository;

import backend.academy.scrapper.Model.Chat;
import backend.academy.scrapper.Model.Link;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LinksRepository {
    Optional<Link> findByName(String name);

    boolean existsByName(String name);

    Link save(Link link);

    void delete(Link link);

    Optional<Link> findByNameAndChat(String name, Chat chat);

    List<Link> findAllByChat(Chat chat);

    List<Link> findAllByNameStartingWith(String prefix);

    List<Link> findAllByName(String name);

    void deleteAll(Collection<Link> links);

}
