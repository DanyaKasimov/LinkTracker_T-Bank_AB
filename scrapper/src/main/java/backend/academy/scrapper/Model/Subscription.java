package backend.academy.scrapper.Model;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    private UUID id;

    private String link;

    private List<String> tags;

    private List<String> filters;
}
