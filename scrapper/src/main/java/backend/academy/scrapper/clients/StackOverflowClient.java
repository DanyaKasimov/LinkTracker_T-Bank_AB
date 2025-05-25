package backend.academy.scrapper.clients;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.exceptions.InvalidDataException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowClient {

    private final RestAccessor restAccessor;
    private final ScrapperConfig config;

    private static final Pattern QUESTION_ID_PATTERN =
            Pattern.compile("^https?://(?:www\\.)?stackoverflow\\.com/questions/(\\d+)(?:/|$)");
    private static final int PREVIEW_LIMIT = 200;

    private static final String ITEMS_FIELD = "items";
    private static final String BODY_FIELD = "body";
    private static final String CREATION_DATE_FIELD = "creation_date";
    private static final String OWNER_FIELD = "owner";
    private static final String DISPLAY_NAME_FIELD = "display_name";
    private static final String TITLE_FIELD = "title";
    private static final String ANSWER_ID_FIELD = "answer_id";

    public Optional<StackOverflowAnswer> getLatestAnswerOrComment(String originalUrl) {
        return buildApiUrl(originalUrl).flatMap(this::getFirstItem).map(item -> {
            Integer answerId = asInt(item, ANSWER_ID_FIELD);
            String preview = trimPreview(asString(item, BODY_FIELD));
            String createdAt = asString(item, CREATION_DATE_FIELD);
            Map<String, Object> owner = asMap(item.get(OWNER_FIELD));
            String username = asString(owner, DISPLAY_NAME_FIELD);
            String questionTitle = asString(item, TITLE_FIELD);

            return StackOverflowAnswer.builder()
                    .answerId(answerId)
                    .questionTitle(questionTitle)
                    .username(username)
                    .createdAt(createdAt)
                    .preview(preview)
                    .build();
        });
    }

    public boolean urlIsValid(String url) {
        String apiUrl = buildApiUrl(url)
                .orElseThrow(() -> new InvalidDataException("Некорректная ссылка StackOverflow: " + url));

        ResponseEntity<Map<String, Object>> response;
        try {
            response = sendRequest(apiUrl);
        } catch (Exception e) {
            log.info("Не удалось проверить StackOverflow URL: {}; error: {}", apiUrl, e.getMessage());
            return false;
        }

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey(ITEMS_FIELD)) {
            return false;
        }

        Object rawItems = body.get(ITEMS_FIELD);
        if (!(rawItems instanceof List<?> rawList)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) rawList;

        return !items.isEmpty();
    }

    private Optional<Map<String, Object>> getFirstItem(String apiUrl) {
        return Optional.ofNullable(sendRequest(apiUrl))
                .map(ResponseEntity::getBody)
                .map(body -> asList(body.get(ITEMS_FIELD)))
                .filter(items -> !CollectionUtils.isEmpty(items))
                .map(List::getFirst);
    }

    private ResponseEntity<Map<String, Object>> sendRequest(String apiUrl) {
        var params = new HashMap<String, String>();
        params.put("order", "desc");
        params.put("sort", "creation");
        params.put("site", "stackoverflow");
        params.put("key", config.stackOverflow().key());
        params.put("access_token", config.stackOverflow().accessToken());

        return restAccessor.getApiAccess(apiUrl, new ParameterizedTypeReference<>() {}, params, Map.of());
    }

    private Optional<String> buildApiUrl(String url) {
        Matcher matcher = QUESTION_ID_PATTERN.matcher(url);
        if (!matcher.find()) {
            log.info("Некорректная ссылка StackOverflow: {}", url);
            return Optional.empty();
        }
        String questionId = matcher.group(1);
        return Optional.of(config.stackOverflow().apiUrl().replace("{id}", questionId));
    }

    private static String trimPreview(String text) {
        if (text == null) return "";
        return text.length() > PREVIEW_LIMIT ? text.substring(0, PREVIEW_LIMIT) : text;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object obj) {
        return obj instanceof Map ? (Map<String, Object>) obj : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object obj) {
        return obj instanceof List ? (List<Map<String, Object>>) obj : List.of();
    }

    private static String asString(Map<String, Object> map, String field) {
        Object val = map.get(field);
        return val == null ? "" : val.toString();
    }

    private static Integer asInt(Map<String, Object> map, String field) {
        Object val = map.get(field);
        if (val instanceof Number num) return num.intValue();
        try {
            return val != null ? Integer.parseInt(val.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
