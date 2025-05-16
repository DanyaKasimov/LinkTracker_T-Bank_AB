package backend.academy.scrapper.clients;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.UrlConfig;
import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.exceptions.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowClient {

    private final RestAccessor restAccessor;
    private final ScrapperConfig config;
    private final UrlConfig urlConfig;

    private static final Pattern QUESTION_ID_PATTERN = Pattern.compile("stackoverflow\\.com/questions/(\\d+)");
    private static final int PREVIEW_LIMIT = 200;

    private static final String ITEMS_FIELD = "items";
    private static final String BODY_FIELD = "body";
    private static final String CREATION_DATE_FIELD = "creation_date";
    private static final String OWNER_FIELD = "owner";
    private static final String DISPLAY_NAME_FIELD = "display_name";
    private static final String TITLE_FIELD = "title";
    private static final String ANSWER_ID_FIELD = "answer_id";

    public Optional<StackOverflowAnswer> getLatestAnswerOrComment(String originalUrl) {
        return buildApiUrl(originalUrl)
            .flatMap(this::getFirstItem)
            .map(item -> {
                Integer answerId = (Integer) item.get(ANSWER_ID_FIELD);
                String preview = trimPreview(asString(item.get(BODY_FIELD)));
                String createdAt = asString(item.get(CREATION_DATE_FIELD));
                Map<String, Object> owner = asMap(item.get(OWNER_FIELD));
                String username = asString(owner.get(DISPLAY_NAME_FIELD));
                String title = asString(item.get(TITLE_FIELD));
                return new StackOverflowAnswer(
                    answerId,
                    title,
                    username,
                    createdAt,
                    preview
                );
            });
    }


    private Optional<Map<String, Object>> getFirstItem(String apiUrl) {
        ResponseEntity<Map<String, Object>> response = sendRequest(apiUrl);
        if (response == null || CollectionUtils.isEmpty((List<?>) Objects.requireNonNull(response.getBody()).get(ITEMS_FIELD))) {
            return Optional.empty();
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get(ITEMS_FIELD);
        return Optional.ofNullable(items.getFirst());
    }


    private ResponseEntity<Map<String, Object>> sendRequest(String apiUrl) {
        Map<String, String> params = Map.of(
            "order", "desc",
            "sort", "creation",
            "site", "stackoverflow",
            "key", config.stackOverflow().key(),
            "access_token", config.stackOverflow().accessToken()
        );

        return restAccessor.getApiAccess(
            apiUrl,
            new ParameterizedTypeReference<>() {
            },
            params,
            Map.of()
        );

    }

    private Optional<String> buildApiUrl(String url) {
        Matcher matcher = QUESTION_ID_PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new InvalidDataException("Некорректная ссылка StackOverflow: " + url);
        }
        String questionId = matcher.group(1);
        return Optional.of(urlConfig.stackOverflowUrl().replace("{id}", questionId));
    }

    private String trimPreview(String text) {
        return text.length() > PREVIEW_LIMIT ? text.substring(0, PREVIEW_LIMIT) : text;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object obj) {
        return obj instanceof Map ? (Map<String, Object>) obj : Map.of();
    }

    private String asString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    public Boolean urlIsValid(String url) {
        Optional<String> apiUrl = Optional.empty();
        try {
            apiUrl = buildApiUrl(url);

            if (apiUrl.isPresent()) {
                ResponseEntity<Map<String, Object>> response = sendRequest(apiUrl.get());
                return response != null && !CollectionUtils.isEmpty(response.getBody());
            }

            return false;
        } catch (Exception e) {
            log.atDebug()
                .setMessage("Не удалось проверить StackOverflow URL")
                .addKeyValue("apiUrl", apiUrl.orElse("empty"))
                .addKeyValue("error", e.getMessage())
                .log();
            return false;
        }
    }
}
