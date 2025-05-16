package backend.academy.scrapper.config;

import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.jdbc.JdbcChatRepository;
import backend.academy.scrapper.repository.jdbc.JdbcFilterRepository;
import backend.academy.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.repository.jdbc.JdbcTagRepository;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "database.type", havingValue = "JDBC")
public class JdbcDatabaseConfig {
    @Bean
    public static BeanFactoryPostProcessor liquibaseEnabler() {
        return bf -> System.setProperty("spring.liquibase.enabled", "true");
    }

    @Bean
    public static BeanFactoryPostProcessor hibernateDisabler() {
        return bf -> System.setProperty("spring.jpa.hibernate.ddl-auto", "none");
    }

    @Bean
    @Primary
    public ChatRepository chatRepository(JdbcChatRepository jdbcRepo) {
        return jdbcRepo;
    }

    @Bean
    @Primary
    public FilterRepository filterRepository(JdbcFilterRepository jdbcRepo) {
        return jdbcRepo;
    }

    @Bean
    @Primary
    public LinksRepository linkRepository(JdbcLinkRepository jdbcRepo) {
        return jdbcRepo;
    }

    @Bean
    @Primary
    public TagRepository tagRepository(JdbcTagRepository jdbcRepo) {
        return jdbcRepo;
    }
}
