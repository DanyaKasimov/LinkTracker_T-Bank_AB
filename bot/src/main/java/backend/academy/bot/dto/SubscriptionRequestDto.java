package backend.academy.bot.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubscriptionRequestDto {
    private String link;
    private List<String> tags;
    private List<String> filters;
}
