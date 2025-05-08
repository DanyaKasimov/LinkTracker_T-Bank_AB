package backend.academy.scrapper.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
