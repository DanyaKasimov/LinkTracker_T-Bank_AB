package backend.academy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

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
        return String.format("""
            Link: %s
            Tags: %s
            Filters: %s
            """,
            link,
            tags == null ? "-" : String.join(", ", tags),
            filters == null ? "-" : String.join(", ", filters));
    }
}
