package backend.academy.scrapper.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.service.LinkService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LinkTypeMetricsTest {

    @Autowired
    private ScrapperConfig config;

    @Test
    void testActiveLinksMetricExistsAndUpdates() {
        MeterRegistry registry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        LinkService linkService = Mockito.mock(LinkService.class);

        String githubBaseUrl = config.github().baseUrl();
        String stackOverflowBaseUrl = config.stackOverflow().baseUrl();

        Mockito.when(linkService.findAllLinksByLink(githubBaseUrl))
                .thenReturn(List.of("https://github.com/test1", "https://github.com/test2"));
        Mockito.when(linkService.findAllLinksByLink(stackOverflowBaseUrl))
                .thenReturn(List.of("https://stackoverflow.com/questions/1"));

        new LinkTypeMetrics(registry, linkService, config);

        Gauge githubGauge = registry.find("active_links").tag("type", "github").gauge();
        Gauge stackoverflowGauge =
                registry.find("active_links").tag("type", "stackoverflow").gauge();

        assertThat(githubGauge).isNotNull();
        assertThat(githubGauge.value()).isEqualTo(2.0);

        assertThat(stackoverflowGauge).isNotNull();
        assertThat(stackoverflowGauge.value()).isEqualTo(1.0);

        Mockito.when(linkService.findAllLinksByLink(githubBaseUrl)).thenReturn(List.of("https://github.com/test1"));
        assertThat(githubGauge.value()).isEqualTo(1.0);
    }
}
