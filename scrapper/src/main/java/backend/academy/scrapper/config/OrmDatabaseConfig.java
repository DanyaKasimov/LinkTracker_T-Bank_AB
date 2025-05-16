package backend.academy.scrapper.config;

import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.adapters.OrmChatRepositoryAdapter;
import backend.academy.scrapper.repository.adapters.OrmFilterRepositoryAdapter;
import backend.academy.scrapper.repository.adapters.OrmLinksRepositoryAdapter;
import backend.academy.scrapper.repository.adapters.OrmTagRepositoryAdapter;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "database.type", havingValue = "ORM")
public class OrmDatabaseConfig {
    @Bean
    public static BeanFactoryPostProcessor hibernateDdlAutoConfigurer() {
        return bf -> System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
    }

    @Bean
    public static BeanFactoryPostProcessor liquibaseDisabler() {
        return bf -> System.setProperty("spring.liquibase.enabled", "false");
    }

    @Bean
    @Primary
    public ChatRepository chatRepository(OrmChatRepositoryAdapter ormAdapter) {
        return ormAdapter;
    }

    @Bean
    @Primary
    public FilterRepository filterRepository(OrmFilterRepositoryAdapter ormAdapter) {
        return ormAdapter;
    }

    @Bean
    @Primary
    public LinksRepository linkRepository(OrmLinksRepositoryAdapter ormAdapter) {
        return ormAdapter;
    }

    @Bean
    @Primary
    public TagRepository tagRepository(OrmTagRepositoryAdapter ormAdapter) {
        return ormAdapter;
    }
}
