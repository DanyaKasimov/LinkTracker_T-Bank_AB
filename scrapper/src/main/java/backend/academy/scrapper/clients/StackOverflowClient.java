package backend.academy.scrapper.clients;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.accessor.RestAccessor;
import backend.academy.scrapper.dto.StackOverflowAnswer;
import backend.academy.scrapper.exceptions.InvalidDataException;
import lombok.RequiredArgsConstructor;
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
public class StackOverflowClient {
    @Autowired
    private RestAccessor restAccessor;
    @Autowired
    private ScrapperConfig config;

    public StackOverflowAnswer getLatestAnswer(String url) {
        String path = convertToStackOverflowApiUrl(url);
        Map<String, String> params = Map.of(
            "order", "desc",
            "sort", "creation",
            "site", "stackoverflow",
            "key", config.stackOverflow().key(),
            "access_token", config.stackOverflow().accessToken()
        );

        ResponseEntity<Map<String, Object>> response = restAccessor.getStackOverflow(
            new ParameterizedTypeReference<>() {},
            params,
            Map.of(),
            path
        );

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        if (items == null || items.isEmpty()) {
            return null;
        }

        Map<String, Object> latestAnswer = items.getFirst();
        return new StackOverflowAnswer((Integer) latestAnswer.get("answer_id"), "");
    }

    public String convertToStackOverflowApiUrl(String url) {
        String defaultUrl = "https://api.stackexchange.com/2.3/questions/{id}/answers";
        Pattern pattern = Pattern.compile("stackoverflow.com/questions/(\\d+)/");
        Matcher matcher = pattern.matcher(url);

        String questionId = matcher.find() ? matcher.group(1) : null;
        if (questionId == null) {
            throw new InvalidDataException("Некорректная ссылка StackOverflow: " + url);
        }

        return defaultUrl.replace("{id}", questionId);
    }
}
