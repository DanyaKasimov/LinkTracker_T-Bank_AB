package backend.academy.scrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Collection;

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
