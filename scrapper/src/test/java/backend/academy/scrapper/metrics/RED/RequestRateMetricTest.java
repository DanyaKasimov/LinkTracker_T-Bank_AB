package backend.academy.scrapper.metrics.RED;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class RequestRateMetricTest {

    @Test
    void testRequestRateCounterIncrements() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Counter requestCounter = Counter.builder("http_server_requests_seconds_count")
                .tag("method", "GET")
                .tag("status", "200")
                .register(registry);

        assertThat(requestCounter.count()).isEqualTo(0.0);

        requestCounter.increment();
        assertThat(requestCounter.count()).isEqualTo(1.0);

        requestCounter.increment(2);
        assertThat(requestCounter.count()).isEqualTo(3.0);
    }
}
