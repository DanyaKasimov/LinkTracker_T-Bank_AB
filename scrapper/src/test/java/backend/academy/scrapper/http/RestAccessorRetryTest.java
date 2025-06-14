package backend.academy.scrapper.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.exceptions.HttpConnectException;
import backend.academy.scrapper.exceptions.InvalidDataException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

class RestAccessorRetryTest {

    private static WireMockServer wireMockServer;
    private RestAccessor restAccessor;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(Options.DYNAMIC_PORT);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        ScrapperConfig config = mock(ScrapperConfig.class);
        WebClient webClient = WebClient.builder().build();
        Retry retry = Retry.fixedDelay(3, Duration.ofMillis(50)).filter(throwable -> {
            if (throwable instanceof WebClientResponseException e) {
                return e.getStatusCode().is5xxServerError();
            }
            return throwable instanceof WebClientRequestException;
        });
        restAccessor = new RestAccessor(webClient, retry, config);
    }

    @Test
    void shouldRetryOnInternalServerError() {
        stubFor(get(urlPathEqualTo("/test"))
                .inScenario("retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("second-failure"));

        stubFor(get(urlPathEqualTo("/test"))
                .inScenario("retry")
                .whenScenarioStateIs("second-failure")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("success"));

        stubFor(get(urlPathEqualTo("/test"))
                .inScenario("retry")
                .whenScenarioStateIs("success")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ok\"}")));

        assertDoesNotThrow(() -> {
            restAccessor.getApiAccess(
                    "http://localhost:" + wireMockServer.port() + "/test",
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    Map.of(),
                    Map.of());
        });

        verify(3, getRequestedFor(urlPathEqualTo("/test")));
    }

    @Test
    void shouldNotRetryOnBadRequest() {
        stubFor(get(urlPathEqualTo("/test")).willReturn(aResponse().withStatus(404)));

        Exception ex = assertThrows(Exception.class, () -> {
            restAccessor.getApiAccess(
                    "http://localhost:" + wireMockServer.port() + "/test",
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    Map.of(),
                    Map.of());
        });

        assertTrue(ex instanceof InvalidDataException || ex instanceof HttpConnectException);

        verify(1, getRequestedFor(urlPathEqualTo("/test")));
    }
}
