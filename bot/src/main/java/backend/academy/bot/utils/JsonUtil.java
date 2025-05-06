package backend.academy.bot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ERROR_MESSAGE = "Ошибка при парсинге JSON";

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.atError()
                .setMessage(ERROR_MESSAGE)
                .addKeyValue("targetType", clazz.getSimpleName())
                .addKeyValue("json", json)
                .addKeyValue("error", e.getMessage())
                .log();
            throw new RuntimeException(ERROR_MESSAGE + ": " + clazz.getSimpleName(), e);
        }
    }
}
