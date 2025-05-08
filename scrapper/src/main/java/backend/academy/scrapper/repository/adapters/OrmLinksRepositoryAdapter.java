package backend.academy.scrapper.repository.adapters;

import backend.academy.scrapper.Model.Chat;
import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.jpa.LinksJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrmLinksRepositoryAdapter implements LinksRepository {

    private final LinksJpaRepository jpaRepository;

    @Override
    public Optional<Link> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public Link save(Link link) {
        return jpaRepository.save(link);
    }

    @Override
    public void delete(Link link) {
        jpaRepository.delete(link);
    }

    @Override
    public Optional<Link> findByNameAndChat(String name, Chat chat) {
        return jpaRepository.findByNameAndChat(name, chat);
    }

    @Override
    public List<Link> findAllByChat(Chat chat) {
        return jpaRepository.findAllByChat(chat);
    }

    @Override
    public List<Link> findAllByNameStartingWith(String prefix) {
        return jpaRepository.findAllByNameStartingWith(prefix);
    }

    @Override
    public List<Link> findAllByName(String name) {
        return jpaRepository.findAllByName(name);
    }
}
