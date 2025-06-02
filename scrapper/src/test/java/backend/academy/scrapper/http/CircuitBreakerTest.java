package backend.academy.scrapper.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.exceptions.HttpConnectException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WireMockTest(httpPort = 8089)
class CircuitBreakerTest {

    @Autowired
    private RestAccessor restAccessor;

    @Test
    void circuitBreakerShouldOpenAndReturnFallbackQuickly() {
        stubFor(get(urlPathEqualTo("/cb"))
                .willReturn(aResponse().withStatus(500).withFixedDelay(4000)));

        long start = System.currentTimeMillis();
        assertThrows(HttpConnectException.class, () -> {
            restAccessor.getApiAccess(
                    "http://localhost:8089/cb",
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    Map.of(),
                    Map.of());
        });
        long firstDuration = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        assertThrows(HttpConnectException.class, () -> {
            restAccessor.getApiAccess(
                    "http://localhost:8089/cb",
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    Map.of(),
                    Map.of());
        });
        long secondDuration = System.currentTimeMillis() - start;

        assertTrue(firstDuration > 3500, "Первый вызов должен иметь задержку");
        assertTrue(secondDuration < 500, "Ответ должен быть мгновенным");
    }
}
