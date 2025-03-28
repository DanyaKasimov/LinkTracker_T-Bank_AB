package backend.academy.scrapper.clients;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.CommitMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GitHubClient {
    
    @Autowired
    private ScrapperConfig config;

    @Autowired
    private RestAccessor restAccessor;

    private final String baseUrl;

    public GitHubClient() {
        this.baseUrl = "https://api.github.com";
    }

    public GitHubClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public CommitMessage getLatestCommitHash(String path) {
        Map<String, String> headers = Map.of(
            "Authorization", "Bearer " + config.githubToken(),
            "Accept", "application/vnd.github.v3+json"
        );

        ResponseEntity<List<Map<String, Object>>> response = restAccessor.getGitHub(
            convertToGitHubApiUrl(path),
            new ParameterizedTypeReference<>() {},
            Map.of("per_page", "1"),
            headers
        );

        if (response == null) {
            return null;
        }

        List<Map<String, Object>> commits = response.getBody();
        if (commits == null || commits.isEmpty()) {
            return null;
        }

        Map<String, Object> commit = commits.getFirst();
        String sha = (String) commit.get("sha");
        Map<String, Object> commitData = (Map<String, Object>) commit.get("commit");

        String message = "";
        if (commitData != null) {
            message = (String) commitData.get("message");
        }

        return new CommitMessage(sha, message);
    }


    public String convertToGitHubApiUrl(String url) {
        String githubPattern = "https://github\\.com/([^/]+)/([^/]+)";
        Pattern pattern = Pattern.compile(githubPattern);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return baseUrl + "/repos/" + matcher.group(1) + "/" + matcher.group(2) + "/commits";
        }
        return url;
    }
}
