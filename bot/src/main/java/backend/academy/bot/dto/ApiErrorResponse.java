package backend.academy.bot.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {
    private String description;
    private String code;
    private String exceptionName;
    private String exceptionMessage;
    private List<String> stacktrace;

    public static ApiErrorResponse fromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json.substring(json.indexOf("{")), ApiErrorResponse.class);
        } catch (Exception e) {
            log.atError()
                .setMessage("Ошибка при парсинге JSON в ApiErrorResponse")
                .addKeyValue("json", json)
                .addKeyValue("error", e.getMessage())
                .log();
            throw new RuntimeException("Ошибка при парсинге JSON в ApiErrorResponse", e);
        }
    }
}
