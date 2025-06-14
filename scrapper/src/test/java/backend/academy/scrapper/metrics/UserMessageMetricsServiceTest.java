package backend.academy.scrapper.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class UserMessageMetricsServiceTest {

    @Test
    void testUserMessageCounterIncrements() {
        MeterRegistry registry = new SimpleMeterRegistry();
        UserMessageMetricsService metricsService = new UserMessageMetricsService(registry);

        assertThat(Objects.requireNonNull(registry.find("user_messages_total").counter())
                        .count())
                .isEqualTo(0.0);

        metricsService.increment();
        assertThat(Objects.requireNonNull(registry.find("user_messages_total").counter())
                        .count())
                .isEqualTo(1.0);

        metricsService.increment();
        assertThat(Objects.requireNonNull(registry.find("user_messages_total").counter())
                        .count())
                .isEqualTo(2.0);
    }
}
