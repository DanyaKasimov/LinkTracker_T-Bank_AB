package backend.academy.bot.config;

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
public record BotConfig(
        @Valid TelegramProperties telegram,
        @Valid KafkaProperties kafka,
        @Valid RedisProperties redis,
        @Valid ScrapperProperties scrapper,
        @Valid HttpProperties http) {

    public record TelegramProperties(@NotEmpty String token) {}

    public record KafkaProperties(@NotEmpty String bootstrapServers, @NotEmpty String group, @Valid Topics topics) {}

    public record Topics(@NotEmpty String notification) {}

    public record RedisProperties(@Min(1) Long duration) {}

    public record ScrapperProperties(@NotEmpty String url) {}

    public record HttpProperties(@Valid Timeout timeout, @Valid Retry retry, @Valid RateLimiting rateLimiting) {}

    public record Retry(@Min(1) int maxAttempts, @Min(0) long backoffMillis, @NotEmpty List<HttpStatus> statusCodes) {}

    public record Timeout(@NotNull Duration connect, @NotNull Duration response) {}

    public record RateLimiting(@NotNull boolean enabled, @Min(1) int requests, @Min(1) int seconds) {}
}
