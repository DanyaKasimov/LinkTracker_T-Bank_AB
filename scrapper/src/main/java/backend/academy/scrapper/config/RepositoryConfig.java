package backend.academy.scrapper.config;

import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.adapters.OrmChatRepositoryAdapter;
import backend.academy.scrapper.repository.adapters.OrmFilterRepositoryAdapter;
import backend.academy.scrapper.repository.adapters.OrmLinksRepositoryAdapter;
import backend.academy.scrapper.repository.adapters.OrmTagRepositoryAdapter;
import backend.academy.scrapper.repository.jdbc.JdbcChatRepository;
import backend.academy.scrapper.repository.jdbc.JdbcFilterRepository;
import backend.academy.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.repository.jdbc.JdbcTagRepository;
import backend.academy.scrapper.repository.jpa.ChatJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RepositoryConfig {

    @Value("${database.type}")
    private String accessType;

    private boolean isOrm() {
        return "ORM".equalsIgnoreCase(accessType);
    }


    @Bean
    @Primary
    public ChatRepository chatRepository(
        JdbcChatRepository jdbcRepo,
        OrmChatRepositoryAdapter ormAdapter
    ) {
        return isOrm() ? ormAdapter : jdbcRepo;
    }

    @Bean
    @Primary
    public FilterRepository filterRepository(
        JdbcFilterRepository jdbcRepo,
        OrmFilterRepositoryAdapter ormAdapter
    ) {
        return isOrm() ? ormAdapter : jdbcRepo;
    }

    @Bean
    @Primary
    public LinksRepository linkRepository(
        JdbcLinkRepository jdbcRepo,
        OrmLinksRepositoryAdapter ormAdapter
    ) {
        return isOrm() ? ormAdapter : jdbcRepo;
    }


    @Primary
    @Bean
    public TagRepository tagRepository(
        JdbcTagRepository jdbcRepo,
        OrmTagRepositoryAdapter ormAdapter
    ) {
        return isOrm() ? ormAdapter : jdbcRepo;
    }
}
