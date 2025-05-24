package backend.academy.bot.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkResponse {
    private String id;
    private String link;
    private List<String> tags;
    private List<String> filters;
}
