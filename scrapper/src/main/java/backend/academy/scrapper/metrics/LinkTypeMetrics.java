package backend.academy.scrapper.metrics;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.service.LinkService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LinkTypeMetrics {

    public LinkTypeMetrics(MeterRegistry registry, LinkService linkService, ScrapperConfig config) {
        Gauge.builder("active_links", linkService, s -> s.findAllLinksByLink(
                                config.github().baseUrl())
                        .size())
                .description("Active GitHub links in DB")
                .tag("type", "github")
                .register(registry);
        Gauge.builder("active_links", linkService, s -> s.findAllLinksByLink(
                                config.stackOverflow().baseUrl())
                        .size())
                .description("Active StackOverflow links in DB")
                .tag("type", "stackoverflow")
                .register(registry);
    }
}
