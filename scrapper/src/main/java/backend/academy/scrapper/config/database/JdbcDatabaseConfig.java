package backend.academy.scrapper.config.database;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
