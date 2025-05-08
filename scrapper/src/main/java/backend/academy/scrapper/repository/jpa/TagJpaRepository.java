package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.Model.Chat;
import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.Model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TagJpaRepository extends JpaRepository<Tag, Long> {

    Tag findByName(String name);

    boolean existsByName(String name);

    void deleteAllByLink(Link link);

    List<Tag> findAllByLink(Link link);
}

