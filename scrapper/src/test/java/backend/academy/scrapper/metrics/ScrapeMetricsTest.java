package backend.academy.scrapper.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import org.junit.jupiter.api.Test;

class ScrapeMetricsTest {

    @Test
    void testScrapeDurationMetricRecords() throws Exception {
        MeterRegistry registry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        ScrapeMetrics metrics = new ScrapeMetrics(registry);

        metrics.timeScrape("github", () -> {
            Thread.sleep(50);
            return null;
        });

        Timer timer =
                registry.find("scrape_duration_seconds").tag("type", "github").timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);

        HistogramSnapshot snap = timer.takeSnapshot();
        assertThat(snap.percentileValues()).isNotEmpty();
    }
}
