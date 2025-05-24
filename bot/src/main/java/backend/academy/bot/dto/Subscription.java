package backend.academy.bot.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {
    private String id;
    private String link;
    private List<String> tags;
    private List<String> filters;

    @Override
    public String toString() {
        return String.format(
                "Link: %s%nTags: %s%nFilters: %s",
                link,
                tags == null || tags.isEmpty() ? "-" : String.join(", ", tags),
                filters == null || filters.isEmpty() ? "-" : String.join(", ", filters));
    }
}
