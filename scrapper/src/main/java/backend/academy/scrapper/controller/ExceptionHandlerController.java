package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.response.ApiErrorResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        log.atWarn()
                .setMessage("Ошибка валидации данных")
                .addKeyValue("errors", errors)
                .log();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка валидации данных: " + errors, ex);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeExceptions(RuntimeException ex) {
        HttpStatus status = resolveResponseStatus(ex);
        log.warn("Обработка исключения. Type: {}. Message: {}", ex.getClass().getSimpleName(), ex.getMessage());

        return buildErrorResponse(status, ex.getMessage(), ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex) {
        log.atError()
                .setMessage("Непредвиденная ошибка")
                .addKeyValue("exception", ex.getClass().getSimpleName())
                .addKeyValue("message", ex.getMessage())
                .log();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Произошла непредвиденная ошибка", ex);
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

    private HttpStatus resolveResponseStatus(Throwable ex) {
        ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
        return annotation != null ? annotation.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
