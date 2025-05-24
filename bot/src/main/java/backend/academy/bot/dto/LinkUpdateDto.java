package backend.academy.bot.dto;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkUpdateDto {
    private Long id;

    private String url;

    private String description;

    private Collection<Long> tgChatIds;
}
