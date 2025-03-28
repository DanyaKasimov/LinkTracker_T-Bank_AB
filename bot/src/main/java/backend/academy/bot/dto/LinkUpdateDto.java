package backend.academy.bot.dto;

import lombok.Data;
import java.util.List;

@Data
public class LinkUpdateDto {
    private String id;

    private String url;

    private String description;

    private List<String> tgChatIds;

}
