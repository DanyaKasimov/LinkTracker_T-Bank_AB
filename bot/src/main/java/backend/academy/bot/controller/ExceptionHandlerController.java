package backend.academy.bot.controller;

import backend.academy.bot.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex) {
        log.atError()
                .setMessage("Непредвиденная ошибка")
                .addKeyValue("exception", ex.getClass().getSimpleName())
                .addKeyValue("message", ex.getMessage())
                .log();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Некорректные параметры запроса", ex);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String description, Exception ex) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.builder()
                        .description(description)
                        .code(status.getReasonPhrase())
                        .exceptionName(ex.getClass().getSimpleName())
                        .exceptionMessage(ex.getMessage())
                        .build());
    }
}
