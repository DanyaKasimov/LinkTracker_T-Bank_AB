package backend.academy.bot.config.kafka;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kafka")
public record KafkaProperties(@NotEmpty String bootstrapServers, @NotEmpty String group) {}
