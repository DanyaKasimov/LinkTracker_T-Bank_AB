package backend.academy.bot.accessor;

import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.exceptions.ErrorResponseException;
import java.net.URI;
import java.util.Map;
import backend.academy.bot.exceptions.HttpConnectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class RestAccessor {

    private final RestClient restClient;

    private final String BASE_URL = "http://localhost:8100";

    public RestAccessor() {
        this.restClient = RestClient.create();
    }

    public <T> ResponseEntity<T> get(String path, Class<T> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        try {
            return restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(responseType);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                ApiErrorResponse response = ApiErrorResponse.fromJson(e.getResponseBodyAsString());
                throw new ErrorResponseException(response.getExceptionMessage());
            }
            throw new HttpConnectException(e.getResponseBodyAsString());
        }
    }

    public <T, R> ResponseEntity<R> post(String path, T requestBody, Class<R> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        try {
            return restClient.post()
                .uri(uri)
                .body(requestBody)
                .retrieve()
                .toEntity(responseType);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                ApiErrorResponse response = ApiErrorResponse.fromJson(e.getResponseBodyAsString());
                throw new ErrorResponseException(response.getExceptionMessage());
            }
            throw new HttpConnectException(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> post(String path) {
        try {
            return restClient.post()
                .uri(BASE_URL + path)
                .retrieve()
                .toEntity(String.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                ApiErrorResponse response = ApiErrorResponse.fromJson(e.getResponseBodyAsString());
                throw new ErrorResponseException(response.getExceptionMessage());
            }
            throw new HttpConnectException(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> delete(String path) {
        try {
            return restClient.delete()
                .uri(BASE_URL + path)
                .retrieve()
                .toEntity(String.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                ApiErrorResponse response = ApiErrorResponse.fromJson(e.getResponseBodyAsString());
                throw new ErrorResponseException(response.getExceptionMessage());
            }
            throw new HttpConnectException(e.getResponseBodyAsString());
        }
    }


    public <T, R> ResponseEntity<R> delete(String path, T requestBody, Class<R> responseType, Map<String, String> queryParams) {
        URI uri = buildUri(path, queryParams);
        try {
            return restClient.method(HttpMethod.DELETE)
                .uri(uri)
                .body(requestBody)
                .retrieve()
                .toEntity(responseType);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                ApiErrorResponse response = ApiErrorResponse.fromJson(e.getResponseBodyAsString());
                throw new ErrorResponseException(response.getExceptionMessage());
            }
            throw new HttpConnectException(e.getResponseBodyAsString());
        }
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
}
