package backend.academy.scrapper;

import backend.academy.scrapper.Model.Chat;
import backend.academy.scrapper.Model.Filter;
import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.Model.Tag;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
    "spring.liquibase.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "database.type=ORM"
})
public class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("link_tracker_db")
        .withUsername("postgres")
        .withPassword("root");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("database.type", () -> "ORM");
    }

    @Autowired
    ChatRepository chatRepo;
    @Autowired
    LinksRepository linksRepo;
    @Autowired
    FilterRepository filterRepo;
    @Autowired
    TagRepository tagRepo;

    Chat chat;
    Link link;


    @Test
    @Transactional
    @Rollback
    void testChatExistsAndDelete() {
        chat = Chat.builder().userId(123L).build();
        chatRepo.save(chat);
        assertThat(chatRepo.existsByUserId(123L)).isTrue();
        chatRepo.delete(chat);
        assertThat(chatRepo.existsByUserId(123L)).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void testSaveAndFindLink() {
        chat = Chat.builder().userId(123L).build();
        chatRepo.save(chat);

        link = Link.builder().name("link1").chat(chat).build();
        link = linksRepo.save(link);
        assertThat(linksRepo.findByName("link1")).isPresent();
        assertThat(linksRepo.existsByName("link1")).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void testFilterCrud() {
        chat = Chat.builder().userId(123L).build();
        chatRepo.save(chat);

        link = Link.builder().name("link1").chat(chat).build();
        link = linksRepo.save(link);

        Filter f = new Filter(null, "filter1", link);
        filterRepo.saveAll(List.of(f));
        assertThat(filterRepo.existsByName("filter1")).isTrue();

        Filter fetched = filterRepo.findByName("filter1");
        filterRepo.delete(fetched);
        assertThat(filterRepo.existsByName("filter1")).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void testTagCrud() {
        chat = Chat.builder().userId(123L).build();
        chatRepo.save(chat);

        link = Link.builder().name("link1").chat(chat).build();
        link = linksRepo.save(link);

        Tag tag = new Tag(null, "tag1", link);
        tagRepo.save(tag);
        assertThat(tagRepo.existsByName("tag1")).isTrue();

        Tag fetched = tagRepo.findByName("tag1");
        tagRepo.delete(fetched);
        assertThat(tagRepo.existsByName("tag1")).isFalse();
    }
}
