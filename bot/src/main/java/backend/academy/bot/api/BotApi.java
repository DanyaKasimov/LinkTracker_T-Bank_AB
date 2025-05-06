package backend.academy.bot.api;

import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.dto.LinkUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Методы для взаимодействия с ботом", description = "Методы для взаимодействия с ботом")
public interface BotApi {

    @Operation(description = "Отправить сообщение")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Обновление отработано"),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные параметры запроса",
                content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiErrorResponse.class))),
        })
    @PostMapping("/updates")
    @ResponseStatus(HttpStatus.OK)
    void updateMessage(final @RequestBody LinkUpdateDto dto);
}
