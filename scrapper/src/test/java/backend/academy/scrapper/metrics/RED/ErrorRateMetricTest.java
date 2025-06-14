package backend.academy.scrapper.metrics.RED;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class ErrorRateMetricTest {

    @Test
    void testErrorRateCounterIncrements() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Counter errorCounter = Counter.builder("http_server_requests_seconds_count")
                .tag("method", "GET")
                .tag("status", "500")
                .register(registry);

        assertThat(errorCounter.count()).isZero();

        errorCounter.increment();
        assertThat(errorCounter.count()).isEqualTo(1.0);
    }
}
