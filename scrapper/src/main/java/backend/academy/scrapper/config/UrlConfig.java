package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "url", ignoreUnknownFields = false)
public record UrlConfig(@NotEmpty String githubUrl, @NotEmpty String stackOverflowUrl) {}
