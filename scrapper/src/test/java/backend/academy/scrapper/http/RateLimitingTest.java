package backend.academy.scrapper.http;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitingTest {

    @LocalServerPort
    int port;

    @Test
    void shouldReturn429WhenRateLimitExceeded() {
        RestClient restClient = RestClient.create();
        int totalRequests = 20;
        int tooManyRequests = 0;

        for (int i = 0; i < totalRequests; i++) {
            long id = (i + 1) * 10000;
            try {
                restClient
                        .get()
                        .uri("http://localhost:" + port + "/tg-chat/" + id)
                        .retrieve()
                        .toEntity(String.class);
            } catch (RestClientResponseException ex) {
                if (ex.getRawStatusCode() == 429) {
                    tooManyRequests++;
                }
            }
        }

        assertTrue(tooManyRequests > 0, "Должен быть хотя бы один ответ 429");
    }
}
