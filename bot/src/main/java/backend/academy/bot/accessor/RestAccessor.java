package backend.academy.bot.accessor;

import backend.academy.bot.config.BotConfig;
import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.exceptions.ErrorResponseException;
import backend.academy.bot.exceptions.HttpConnectException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.net.URI;
import java.util.Collections;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAccessor {

    private final BotConfig config;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Retry retry;

    @CircuitBreaker(name = "botApiCircuitBreaker", fallbackMethod = "getClassFallback")
    public <T> ResponseEntity<T> get(String path, Class<T> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        try {
            return webClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(responseType)
                    .retryWhen(retry)
                    .block();
        } catch (WebClientResponseException e) {
            handleError(e);
            throw new HttpConnectException(e.getMessage());
        }
    }

    @CircuitBreaker(name = "botApiCircuitBreaker", fallbackMethod = "postClassFallback")
    public <T, R> ResponseEntity<R> post(String path, T body, Class<R> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        try {
            return webClient
                    .post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(responseType)
                    .retryWhen(retry)
                    .block();
        } catch (WebClientResponseException e) {
            handleError(e);
            throw new HttpConnectException(e.getMessage());
        }
    }

    @CircuitBreaker(name = "botApiCircuitBreaker", fallbackMethod = "postVoidFallback")
    public void post(String path) {
        URI uri = URI.create(config.scrapper().url() + path);
        try {
            webClient
                    .post()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class)
                    .retryWhen(retry)
                    .block();
        } catch (WebClientResponseException e) {
            handleError(e);
            throw new HttpConnectException(e.getMessage());
        }
    }

    @CircuitBreaker(name = "botApiCircuitBreaker", fallbackMethod = "deleteVoidFallback")
    public void delete(String path) {
        URI uri = URI.create(config.scrapper().url() + path);
        try {
            webClient
                    .delete()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class)
                    .retryWhen(retry)
                    .block();
        } catch (WebClientResponseException e) {
            handleError(e);
            throw new HttpConnectException(e.getMessage());
        }
    }

    @CircuitBreaker(name = "botApiCircuitBreaker", fallbackMethod = "deleteClassFallback")
    public <T, R> ResponseEntity<R> delete(
            String path, T body, Class<R> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        try {
            return webClient
                    .method(org.springframework.http.HttpMethod.DELETE)
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(responseType)
                    .retryWhen(retry)
                    .block();
        } catch (WebClientResponseException e) {
            handleError(e);
            throw new HttpConnectException(e.getMessage());
        }
    }

    public <T> ResponseEntity<T> getClassFallback(
            String path, Class<T> responseType, Map<String, String> queryParams, Throwable throwable) {
        log.warn("Circuit Breaker GET(Class) fallback: path={}, error={}", path, throwable.toString());
        throw new HttpConnectException("Circuit breaker сработал, внешний сервис недоступен.");
    }

    public <T, R> ResponseEntity<R> postClassFallback(
            String path, T body, Class<R> responseType, Map<String, String> queryParams, Throwable throwable) {
        log.warn("Circuit Breaker POST(Class) fallback: path={}, error={}", path, throwable.toString());
        throw new HttpConnectException("Circuit breaker сработал, внешний сервис недоступен.");
    }

    public <T, R> ResponseEntity<R> postFallback(
            String path,
            T body,
            ParameterizedTypeReference<R> responseType,
            Map<String, String> queryParams,
            Map<String, String> headers,
            Throwable throwable) {
        log.warn("Circuit Breaker POST(Ptr) fallback: path={}, error={}", path, throwable.toString());
        throw new HttpConnectException("Circuit breaker сработал, внешний сервис недоступен.");
    }

    public void postVoidFallback(String path, Throwable throwable) {
        log.warn("Circuit Breaker POST(void) fallback: path={}, error={}", path, throwable.toString());
        throw new HttpConnectException("Circuit breaker сработал, внешний сервис недоступен.");
    }

    public void deleteVoidFallback(String path, Throwable throwable) {
        log.warn("Circuit Breaker DELETE(void) fallback: path={}, error={}", path, throwable.toString());
        throw new HttpConnectException("Circuit breaker сработал, внешний сервис недоступен.");
    }

    public <T, R> ResponseEntity<R> deleteClassFallback(
            String path, T body, Class<R> responseType, Map<String, String> queryParams, Throwable throwable) {
        log.warn("Circuit Breaker DELETE(Class) fallback: path={}, error={}", path, throwable.toString());
        throw new HttpConnectException("Circuit breaker сработал, внешний сервис недоступен.");
    }

    private URI buildUri(String path, Map<String, String> queryParams) {
        return UriComponentsBuilder.fromUriString(config.scrapper().url() + path)
                .queryParams((queryParams == null ? Collections.<String, String>emptyMap() : queryParams)
                        .entrySet().stream()
                                .collect(
                                        LinkedMultiValueMap::new,
                                        (map, entry) -> map.add(entry.getKey(), entry.getValue()),
                                        LinkedMultiValueMap::putAll))
                .build()
                .toUri();
    }

    private void handleError(WebClientResponseException e) {
        if (e.getStatusCode().is4xxClientError()) {
            ApiErrorResponse response;
            try {
                response = objectMapper.readValue(e.getResponseBodyAsString(), ApiErrorResponse.class);
            } catch (Exception ex) {
                log.error("Error parsing error response: {}", ex.getMessage());
                throw new RuntimeException(ex.getMessage());
            }

            log.error("Error: {}", response.getExceptionName());
            if ("InvalidDataException".equalsIgnoreCase(response.getExceptionName())
                    || "NotFoundDataException".equalsIgnoreCase(response.getExceptionName())) {
                throw new ErrorResponseException(response.getExceptionMessage());
            }
        }
    }
}
