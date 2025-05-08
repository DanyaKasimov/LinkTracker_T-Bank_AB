package backend.academy.scrapper.api;

import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Управление подписками", description = "Методы для управления подписками")
public interface SubscriptionApi {

    @Operation(description = "Добавление подписки")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Ссылка успешно добавлена"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
            })
    @PostMapping("/links")
    @ResponseStatus(HttpStatus.OK)
    LinkResponse addSubscription(final @RequestParam @Valid Long tgChatId, final @Valid @RequestBody SubscriptionRequestDto dto);

    @Operation(description = "Получение списка подписок")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Ссылки успешно получены",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ListLinksResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
            })
    @GetMapping("/links")
    @ResponseStatus(HttpStatus.OK)
    ListLinksResponse getSubscription(final @RequestParam @Valid Long tgChatId);


    @Operation(description = "Убрать отслеживание ссылки")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Ссылка успешно убрана",
                content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ListLinksResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные параметры запроса",
                content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Ссылка не найдена",
                content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiErrorResponse.class))),
        })
    @DeleteMapping("/links")
    @ResponseStatus(HttpStatus.OK)
    LinkResponse deleteSubscription(final @RequestParam @Valid Long tgChatId, final @RequestBody String link);
}
