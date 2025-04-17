package backend.academy.scrapper.accessor;

import java.net.URI;
import java.util.Map;
import backend.academy.scrapper.exceptions.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAccessor {

    private final RestClient restClient;

    private final String BOT_URL = "http://localhost:8081";

    public RestAccessor() {
        this.restClient = RestClient.create();
    }

    public <T> ResponseEntity<T> getApiAccess(String path, ParameterizedTypeReference<T> responseType,
                                           Map<String, String> queryParams,
                                           Map<String, String> headers) {
        try {
            URI uri = buildUri(path, queryParams);

            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);

            return restClient.get()
                .uri(uri)
                .headers(h -> h.addAll(httpHeaders))
                .retrieve()
                .toEntity(responseType);
        } catch (RestClientResponseException e) {
            log.atError()
                .setMessage("Ошибка запроса к API")
                .addKeyValue("path", path)
                .addKeyValue("status", e.getStatusCode())
                .addKeyValue("errorMessage", e.getMessage())
                .log();
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new InvalidDataException("Ресурс не найден.");
            }
            if (e.getStatusCode().equals(HttpStatus.CONFLICT)) {
                return null;
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public <T, R> ResponseEntity<R> postBot(String path, T requestBody, Class<R> responseType) {
        try {
            return restClient
                .post()
                .uri(BOT_URL + path)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(requestBody)
                .retrieve()
                .toEntity(responseType);
        } catch (Exception e) {
            log.atError()
                .setMessage("Ошибка отправки POST-запроса")
                .addKeyValue("url", BOT_URL + path)
                .addKeyValue("errorMessage", e.getMessage())
                .log();
            throw new RuntimeException(e.getMessage());
        }
    }

    private URI buildUri(String path, Map<String, String> queryParams) {
        return UriComponentsBuilder.fromUriString(path)
            .queryParams(queryParams.entrySet().stream()
                .collect(LinkedMultiValueMap::new,
                    (map, entry) -> map.add(entry.getKey(), entry.getValue()),
                    LinkedMultiValueMap::putAll))
            .build()
            .toUri();
    }
}
