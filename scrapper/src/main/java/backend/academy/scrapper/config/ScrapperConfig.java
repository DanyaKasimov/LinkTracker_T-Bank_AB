package backend.academy.scrapper.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @Valid GitHubProperties github,
        @Valid StackOverflowProperties stackOverflow,
        @Valid HttpProperties http,
        @Valid KafkaProperties kafka,
        @Valid BotProperties bot,
        @Valid MultithreadingProperties multithreading) {

    public record GitHubProperties(@NotEmpty String apiUrl, @NotEmpty String baseUrl, @NotEmpty String token) {}

    public record StackOverflowProperties(
            @NotEmpty String key, @NotEmpty String accessToken, @NotEmpty String apiUrl, @NotEmpty String baseUrl) {}

    public record HttpProperties(@Valid Timeout timeout, @Valid Retry retry, @Valid RateLimiting rateLimiting) {}

    public record Retry(@Min(1) int maxAttempts, @Min(0) long backoffMillis, @NotEmpty List<HttpStatus> statusCodes) {}

    public record Timeout(@NotNull Duration connect, @NotNull Duration response) {}

    public record RateLimiting(boolean enabled, @Min(1) int requests, @Min(1) int seconds) {}

    public record KafkaProperties(@NotEmpty String bootstrapServers, @Valid Topics topics) {}

    public record Topics(@NotEmpty String notification) {}

    public record BotProperties(@NotEmpty String url) {}

    public record MultithreadingProperties(@Min(1) int threadCount, @Min(1) int batchSize) {}
}
