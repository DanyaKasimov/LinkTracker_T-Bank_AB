package backend.academy.scrapper.clients;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.UrlConfig;
import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.constants.GitHubEndpoints;
import backend.academy.scrapper.dto.GitHubUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubClient {

    private final ScrapperConfig config;
    private final UrlConfig urlConfig;
    private final RestAccessor restAccessor;

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("https://github\\.com/([^/]+)/([^/]+)");
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

    public Optional<GitHubUpdate> getLatestCommit(String path) {
        return buildApiUrl(path, GitHubEndpoints.COMMIT.getDescription())
            .flatMap(this::extractCommitInfo);
    }

    public Optional<GitHubUpdate> getLatestPRIssue(String path, GitHubEndpoints endpoint) {
        return buildApiUrl(path, endpoint.getDescription())
            .flatMap(this::extractItemInfo);
    }

    private Optional<GitHubUpdate> extractCommitInfo(String apiUrl) {
        return getFirstItem(apiUrl).map(commit -> {
            String sha = asString(commit.get(SHA_FIELD));
            Map<String, Object> commitData = asMap(commit.get(COMMIT_FIELD));
            Map<String, Object> author = asMap(commitData.get(AUTHOR_FIELD));

            String message = asString(commitData.get(MESSAGE_FIELD));
            String preview = trimPreview(message);
            String username = asString(author.get(NAME_FIELD));
            String createdAt = asString(author.get(DATE_FIELD));
            String title = message.split("\\n")[0];

            return new GitHubUpdate(sha, title, username, createdAt, preview);
        });
    }

    private Optional<GitHubUpdate> extractItemInfo(String apiUrl) {
        return getFirstItem(apiUrl).map(item -> {
            String title = asString(item.get(TITLE_FIELD));
            Map<String, Object> user = asMap(item.get(USER_FIELD));
            String username = asString(user.get(LOGIN_FIELD));
            String createdAt = asString(item.get(CREATED_AT_FIELD));
            String body = asString(item.getOrDefault(BODY_FIELD, ""));
            String preview = trimPreview(body);

            return new GitHubUpdate("", title, username, createdAt, preview);
        });
    }

    private Optional<Map<String, Object>> getFirstItem(String apiUrl) {
        ResponseEntity<List<Map<String, Object>>> response = sendRequest(apiUrl);
        if (response == null || CollectionUtils.isEmpty(response.getBody())) return Optional.empty();
        return Optional.ofNullable(response.getBody().getFirst());
    }

    private ResponseEntity<List<Map<String, Object>>> sendRequest(String apiUrl) {
        Map<String, String> headers = Map.of(
            "Authorization", "Bearer " + config.githubToken(),
            "Accept", "application/vnd.github.v3+json"
        );

        return restAccessor.getApiAccess(
            apiUrl,
            new ParameterizedTypeReference<>() {
            },
            Map.of("per_page", "1", "sort", "created", "direction", "desc"),
            headers
        );
    }

    public Boolean urlIsValid(String url) {
        Optional<String> apiUrl = Optional.empty();
        try {
            apiUrl = buildApiUrl(url, GitHubEndpoints.COMMIT.getDescription());

            if (apiUrl.isPresent()) {
                ResponseEntity<List<Map<String, Object>>> response = sendRequest(apiUrl.get());
                return response != null && !CollectionUtils.isEmpty(response.getBody());
            }

            return false;
        } catch (Exception e) {
            log.atDebug()
                .setMessage("Не удалось проверить GitHub URL")
                .addKeyValue("apiUrl", apiUrl.orElse("empty"))
                .addKeyValue("error", e.getMessage())
                .log();
            return false;
        }
    }


    private Optional<String> buildApiUrl(String originalUrl, String endpointSuffix) {
        Matcher matcher = GITHUB_URL_PATTERN.matcher(originalUrl);
        if (matcher.find()) {
            return Optional.of(String.format("%s/repos/%s/%s%s",
                urlConfig.githubUrl(), matcher.group(1), matcher.group(2), endpointSuffix));
        }
        return Optional.empty();
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
}
