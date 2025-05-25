package backend.academy.scrapper.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.exceptions.HttpConnectException;
import backend.academy.scrapper.exceptions.InvalidDataException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@WireMockTest(httpPort = 8181)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RestAccessorRetryTest {

    @Autowired
    private RestAccessor restAccessor;

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
