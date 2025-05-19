package backend.academy.bot.accessor;

import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.exceptions.ErrorResponseException;
import backend.academy.bot.exceptions.HttpConnectException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Component
public class RestAccessor {
    private final String USER_EXCEPTION = "InvalidDataException";

    private final String NOT_FOUND_EXCEPTION = "NotFoundDataException";

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "http://localhost:8100";

    @Autowired
    public RestAccessor(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public <T> ResponseEntity<T> get(String path, Class<T> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        return execute(() -> restClient.get()
            .uri(uri)
            .retrieve()
            .toEntity(responseType));
    }

    public <T, R> ResponseEntity<R> post(String path, T body, Class<R> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        return execute(() -> restClient.post()
            .uri(uri)
            .body(body)
            .retrieve()
            .toEntity(responseType));
    }

    public void post(String path) {
        URI uri = URI.create(BASE_URL + path);
        execute(() -> restClient.post()
            .uri(uri)
            .retrieve()
            .toEntity(String.class));
    }

    public void delete(String path) {
        URI uri = URI.create(BASE_URL + path);
        execute(() -> restClient.delete()
            .uri(uri)
            .retrieve()
            .toEntity(String.class));
    }

    public <T, R> ResponseEntity<R> delete(String path, T body, Class<R> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        return execute(() -> restClient.method(HttpMethod.DELETE)
            .uri(uri)
            .body(body)
            .retrieve()
            .toEntity(responseType));
    }

    private URI buildUri(String path, Map<String, String> queryParams) {
        return UriComponentsBuilder.fromUriString(BASE_URL + path)
            .queryParams(queryParams.entrySet().stream()
                .collect(LinkedMultiValueMap::new,
                    (map, entry) -> map.add(entry.getKey(), entry.getValue()),
                    LinkedMultiValueMap::putAll))
            .build()
            .toUri();
    }

    private <T> ResponseEntity<T> execute(Supplier<ResponseEntity<T>> request) {
        try {
            return request.get();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                handleClientError(e);
            }
            throw new HttpConnectException(e.getResponseBodyAsString());
        }
    }

    private void handleClientError(RestClientResponseException e) {
        ApiErrorResponse response;
        try {
            response = objectMapper.readValue(e.getResponseBodyAsString(), ApiErrorResponse.class);
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
        log.atError()
            .setMessage(response.getExceptionMessage())
            .addKeyValue("code", e.getStatusCode())
            .log();
        if (USER_EXCEPTION.equalsIgnoreCase(response.getExceptionName()) ||
            NOT_FOUND_EXCEPTION.equalsIgnoreCase(response.getExceptionName())) {
            throw new ErrorResponseException(response.getExceptionMessage());
        }
    }
}
