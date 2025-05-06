package backend.academy.scrapper.clients;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.UrlConfig;
import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.CommitMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


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

    private final String SHA = "sha";

    private final String COMMIT = "commit";

    private final String MESSAGE = "message";

    private final String PATTERN = "https://github\\.com/([^/]+)/([^/]+)";

    public Optional<CommitMessage> getTryLatestCommitHash(String path) {

        ResponseEntity<List<Map<String, Object>>> response = sendRequest(path);

        if (response == null) {
            return Optional.empty();
        }

        List<Map<String, Object>> commits = response.getBody();
        if (CollectionUtils.isEmpty(commits)) {
            return Optional.empty();
        }

        Map<String, Object> commit = commits.getFirst();
        String sha = (String) commit.get(SHA);
        Map<String, Object> commitData = (Map<String, Object>) commit.get(COMMIT);

        String message = commitData != null ? commitData.get(MESSAGE).toString() : "";
        return Optional.of(new CommitMessage(sha, message));
    }

    public ResponseEntity<List<Map<String, Object>>> sendRequest(String path) {
        Map<String, String> headers = Map.of(
            "Authorization", "Bearer " + config.githubToken(),
            "Accept", "application/vnd.github.v3+json"
        );

        return restAccessor.getApiAccess(
            convertToGitHubApiUrl(path),
            new ParameterizedTypeReference<>() {},
            Map.of("per_page", "1"),
            headers
        );
    }

    private String convertToGitHubApiUrl(String url) {
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return urlConfig.githubUrl() + "/repos/" + matcher.group(1) + "/" + matcher.group(2) + "/commits";
        }
        return url;
    }
}
