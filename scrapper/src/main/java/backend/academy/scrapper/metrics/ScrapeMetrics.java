package backend.academy.scrapper.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.Callable;
import org.springframework.stereotype.Component;

@Component
public class ScrapeMetrics {
    private final MeterRegistry registry;

    public ScrapeMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public <T> void timeScrape(String type, Callable<T> callable) throws Exception {
        Timer timer = Timer.builder("scrape_duration_seconds")
                .description("Duration of scrape by type")
                .tag("type", type)
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        timer.recordCallable(callable);
    }
}
