package backend.academy.scrapper.metrics.RED;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class RequestDurationMetricTest {

    @Test
    void testRequestDurationTimerRecords() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Timer timer = Timer.builder("http_server_requests_seconds")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .tag("method", "GET")
                .tag("status", "200")
                .register(registry);

        timer.record(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        });

        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.SECONDS)).isGreaterThan(0.0);
        assertThat(timer.takeSnapshot().percentileValues()).isNotEmpty();
    }
}
