package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.exceptions.NotFoundDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

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
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Произошла непредвиденная ошибка", ex);
    }

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

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDataException(InvalidDataException ex) {
        log.atWarn()
            .setMessage("Некорректные данные")
            .addKeyValue("message", ex.getMessage())
            .log();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Некорректные данные.", ex);
    }

    @ExceptionHandler(NotFoundDataException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundDataException(NotFoundDataException ex) {
        log.atWarn()
            .setMessage("Данные не найдены")
            .addKeyValue("message", ex.getMessage())
            .log();
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Данные не найдены.", ex);
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
