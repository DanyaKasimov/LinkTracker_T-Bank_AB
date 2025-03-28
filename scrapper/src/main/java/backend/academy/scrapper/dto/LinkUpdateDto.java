package backend.academy.scrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkUpdateDto {
    private String id;

    private String url;

    private String description;

    private List<Long> tgChatIds;

}
