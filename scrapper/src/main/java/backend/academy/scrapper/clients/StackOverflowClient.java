package backend.academy.scrapper.clients;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.UrlConfig;
import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.exceptions.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class StackOverflowClient {
    private final RestAccessor restAccessor;

    private final ScrapperConfig config;

    private final UrlConfig urlConfig;

    private final String ANSWER_ID = "answer_id";

    private final String ITEMS = "items";

    public Optional<StackOverflowAnswer> getTryLatestAnswer(String url) {
        ResponseEntity<Map<String, Object>> response = sendRequest(url);

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get(ITEMS);
        if (CollectionUtils.isEmpty(items)) {
            return Optional.empty();
        }

        Map<String, Object> latestAnswer = items.getFirst();
        return Optional.of(new StackOverflowAnswer((Integer) latestAnswer.get(ANSWER_ID), ""));
    }

    public ResponseEntity<Map<String, Object>> sendRequest(String url) {
        String path = convertToStackOverflowApiUrl(url);
        Map<String, String> params = Map.of(
            "order", "desc",
            "sort", "creation",
            "site", "stackoverflow",
            "key", config.stackOverflow().key(),
            "access_token", config.stackOverflow().accessToken()
        );
        return restAccessor.getApiAccess(
            path,
            new ParameterizedTypeReference<>() {},
            params,
            Map.of()
        );
    }

    public String convertToStackOverflowApiUrl(String url) {
        String defaultUrl = urlConfig.stackOverflowUrl();
        Pattern pattern = Pattern.compile("stackoverflow.com/questions/(\\d+)/");
        Matcher matcher = pattern.matcher(url);

        String questionId = matcher.find() ? matcher.group(1) : null;
        if (questionId == null) {
            throw new InvalidDataException("Некорректная ссылка StackOverflow: " + url);
        }

        return defaultUrl.replace("{id}", questionId);
    }
}
