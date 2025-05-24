package backend.academy.scrapper.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequestDto {
    private String link;
    private List<String> tags;
    private List<String> filters;
}
