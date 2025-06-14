package backend.academy.scrapper.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.exceptions.HttpConnectException;
import backend.academy.scrapper.exceptions.InvalidDataException;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RestAccessorRetryTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private RestAccessor restAccessor;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(8181);
        wireMockServer.start();
        configureFor("localhost", 8181);
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void resetStubs() {
        wireMockServer.resetAll();
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
                    "http://localhost:8181/test",
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
                    "http://localhost:8181/test",
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    Map.of(),
                    Map.of());
        });

        assertTrue(
                ex instanceof InvalidDataException || ex instanceof HttpConnectException,
                "Exception type: " + ex.getClass() + ", message: " + ex.getMessage());

        verify(1, getRequestedFor(urlPathEqualTo("/test")));
    }
}
