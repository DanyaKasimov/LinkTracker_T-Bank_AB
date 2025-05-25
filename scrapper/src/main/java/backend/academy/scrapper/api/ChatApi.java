package backend.academy.scrapper.api;

import backend.academy.scrapper.dto.response.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Управление чатами", description = "Методы для управления чатами")
public interface ChatApi {

    @Operation(description = "Регистрация чата")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Чат зарегистрирован"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
            })
    @PostMapping("/tg-chat/{id}")
    @ResponseStatus(HttpStatus.OK)
    void registerChat(final @PathVariable @Valid Long id);

    @Operation(description = "Удаление чата")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Чат удален"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Чат не найден",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
            })
    @DeleteMapping("/tg-chat/{id}")
    @ResponseStatus(HttpStatus.OK)
    void deleteChat(final @PathVariable @Valid Long id);
}
