package backend.academy.scrapper.clients;

import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.constants.GitHubEndpoints;
import backend.academy.scrapper.dto.GitHubUpdate;
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
public class GitHubClient {

    private final ScrapperConfig config;
    private final RestAccessor restAccessor;

    private static final Pattern GITHUB_URL_PATTERN =
            Pattern.compile("^https://github\\.com/([^/]+)/([^/]+?)(?:\\.git)?(?:/.*)?$");
    private static final int PREVIEW_LIMIT = 200;

    private static final String COMMIT_FIELD = "commit";
    private static final String AUTHOR_FIELD = "author";
    private static final String MESSAGE_FIELD = "message";
    private static final String SHA_FIELD = "sha";
    private static final String NAME_FIELD = "name";
    private static final String DATE_FIELD = "date";
    private static final String TITLE_FIELD = "title";
    private static final String USER_FIELD = "user";
    private static final String LOGIN_FIELD = "login";
    private static final String CREATED_AT_FIELD = "created_at";
    private static final String BODY_FIELD = "body";

    private static final String QUERY_PER_PAGE = "per_page";
    private static final String QUERY_SORT = "sort";
    private static final String QUERY_DIRECTION = "direction";
    private static final String DEFAULT_PER_PAGE = "1";
    private static final String DEFAULT_SORT = "created";
    private static final String DEFAULT_DIRECTION = "desc";

    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String MEDIA_TYPE_GITHUB_V3_JSON = "application/vnd.github.v3+json";
    private static final String AUTH_BEARER_PREFIX = "Bearer ";

    public Optional<GitHubUpdate> getLatestCommit(String path) {
        return buildApiUrl(path, GitHubEndpoints.COMMIT.getDescription()).flatMap(this::extractCommitInfo);
    }

    public Optional<GitHubUpdate> getLatestPRIssue(String path, GitHubEndpoints endpoint) {
        return buildApiUrl(path, endpoint.getDescription()).flatMap(this::extractItemInfo);
    }

    public boolean urlIsValid(String url) {
        Optional<String> apiUrlOpt = buildApiUrl(url, GitHubEndpoints.COMMIT.getDescription());
        if (apiUrlOpt.isEmpty()) return false;
        try {
            ResponseEntity<List<Map<String, Object>>> response = sendRequest(apiUrlOpt.orElseThrow());
            return response != null && !CollectionUtils.isEmpty(response.getBody());
        } catch (Exception e) {
            log.debug(
                    "Не удалось проверить GitHub URL, apiUrl={}, error={}", apiUrlOpt.orElse("empty"), e.getMessage());
            return false;
        }
    }

    private Optional<GitHubUpdate> extractCommitInfo(String apiUrl) {
        return getFirstItem(apiUrl).map(item -> {
            String sha = asString(item, SHA_FIELD);
            Map<String, Object> commit = asMap(item.get(COMMIT_FIELD));
            Map<String, Object> author = asMap(commit.get(AUTHOR_FIELD));
            String message = asString(commit, MESSAGE_FIELD);
            String preview = trimPreview(message);
            String username = asString(author, NAME_FIELD);
            String createdAt = asString(author, DATE_FIELD);
            String title = extractTitle(message);

            return GitHubUpdate.builder()
                    .sha(sha)
                    .title(title)
                    .username(username)
                    .createdAt(createdAt)
                    .preview(preview)
                    .build();
        });
    }

    private Optional<GitHubUpdate> extractItemInfo(String apiUrl) {
        return getFirstItem(apiUrl).map(item -> {
            String title = asString(item, TITLE_FIELD);
            Map<String, Object> user = asMap(item.get(USER_FIELD));
            String username = asString(user, LOGIN_FIELD);
            String createdAt = asString(item, CREATED_AT_FIELD);
            String body = asString(item, BODY_FIELD);
            String preview = trimPreview(body);

            return GitHubUpdate.builder()
                    .sha("")
                    .title(title)
                    .username(username)
                    .createdAt(createdAt)
                    .preview(preview)
                    .build();
        });
    }

    private Optional<Map<String, Object>> getFirstItem(String apiUrl) {
        return Optional.ofNullable(sendRequest(apiUrl))
                .map(ResponseEntity::getBody)
                .filter(body -> !CollectionUtils.isEmpty(body))
                .map(List::getFirst);
    }

    private ResponseEntity<List<Map<String, Object>>> sendRequest(String apiUrl) {
        Map<String, String> headers = Map.of(
                HEADER_ACCEPT,
                MEDIA_TYPE_GITHUB_V3_JSON,
                HEADER_AUTHORIZATION,
                AUTH_BEARER_PREFIX + config.github().token());

        Map<String, String> queryParams = Map.of(
                QUERY_PER_PAGE, DEFAULT_PER_PAGE,
                QUERY_SORT, DEFAULT_SORT,
                QUERY_DIRECTION, DEFAULT_DIRECTION);
        return restAccessor.getApiAccess(apiUrl, new ParameterizedTypeReference<>() {}, queryParams, headers);
    }

    private Optional<String> buildApiUrl(String originalUrl, String endpointSuffix) {
        Matcher matcher = GITHUB_URL_PATTERN.matcher(originalUrl);
        if (matcher.matches()) {
            return Optional.of(String.format(
                    "%s/repos/%s/%s%s", config.github().apiUrl(), matcher.group(1), matcher.group(2), endpointSuffix));
        }
        return Optional.empty();
    }

    private static String trimPreview(String text) {
        if (text == null) return "";
        return text.length() > PREVIEW_LIMIT ? text.substring(0, PREVIEW_LIMIT) : text;
    }

    private static String extractTitle(String message) {
        if (message == null) return "";
        int idx = message.indexOf('\n');
        return idx == -1 ? message : message.substring(0, idx);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object obj) {
        return obj instanceof Map ? (Map<String, Object>) obj : Map.of();
    }

    private static String asString(Map<String, Object> map, String field) {
        Object val = map.get(field);
        return val == null ? "" : val.toString();
    }
}
