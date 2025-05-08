package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "liquibase", ignoreUnknownFields = false)
public record LiquibaseConfig(@NotEmpty String url, @NotEmpty String username, @NotEmpty String password, @NotEmpty String file) {
}
