package backend.academy.scrapper.accessor;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.exceptions.HttpConnectException;
import backend.academy.scrapper.exceptions.InvalidDataException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAccessor {

    private final WebClient client;
    private final Retry retry;
    private final ScrapperConfig config;

    @CircuitBreaker(name = "apiCircuitBreaker", fallbackMethod = "getApiFallback")
    public <T> ResponseEntity<T> getApiAccess(
            String path,
            ParameterizedTypeReference<T> responseType,
            Map<String, String> queryParams,
            Map<String, String> headers) {

        URI uri = buildUri(path, queryParams);

        try {
            return client.get()
                    .uri(uri)
                    .headers(h -> headers.forEach(h::set))
                    .retrieve()
                    .toEntity(responseType)
                    .retryWhen(retry)
                    .block();

        } catch (WebClientResponseException e) {
            log.warn("WebClientResponseException body: {}", e.getResponseBodyAsString());
            switch (e.getStatusCode()) {
                case NOT_FOUND -> throw new InvalidDataException("Ресурс не найден.");
                case CONFLICT -> throw new InvalidDataException("Пустой репозиторий.");
                default -> throw new HttpConnectException(
                        "Ошибка клиента (" + e.getRawStatusCode() + "): " + e.getResponseBodyAsString());
            }
        } catch (WebClientRequestException e) {
            log.warn("WebClientRequestException message: {}", e.getMessage());
            throw new HttpConnectException(e.getMessage());
        }
    }

    @CircuitBreaker(name = "apiCircuitBreaker", fallbackMethod = "postBotFallback")
    public <T, R> ResponseEntity<R> postBot(String path, T requestBody, ParameterizedTypeReference<R> responseType) {
        try {
            return client.post()
                    .uri(config.bot().url() + path)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .toEntity(responseType)
                    .retryWhen(retry)
                    .block();

        } catch (WebClientResponseException | WebClientRequestException e) {
            log.error("Ошибка запроса к API Bot. Path: {}. ErrorMessage: {}", path, e.getMessage());
            throw new HttpConnectException(e.getMessage());
        }
    }

    public <T> ResponseEntity<T> getApiFallback(
            String path,
            ParameterizedTypeReference<T> responseType,
            Map<String, String> queryParams,
            Map<String, String> headers,
            Throwable throwable) {
        log.warn("Circuit Breaker getApiAccess fallback: path={}, error={}", path, throwable.toString());
        throw new HttpConnectException("Circuit breaker сработал, внешний сервис недоступен.");
    }

    public <T, R> ResponseEntity<R> postBotFallback(
            String path, T requestBody, ParameterizedTypeReference<R> responseType, Throwable throwable) {
        log.warn("Circuit Breaker postBot fallback: path={}, error={}", path, throwable.toString());
        throw new HttpConnectException("Circuit breaker сработал, внешний сервис недоступен.");
    }

    private URI buildUri(String path, Map<String, String> queryParams) {
        return UriComponentsBuilder.fromUriString(path)
                .queryParams(queryParams.entrySet().stream()
                        .collect(
                                LinkedMultiValueMap::new,
                                (map, entry) -> map.add(entry.getKey(), entry.getValue()),
                                LinkedMultiValueMap::putAll))
                .build()
                .toUri();
    }
}
