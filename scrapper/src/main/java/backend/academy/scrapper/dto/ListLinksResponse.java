package backend.academy.scrapper.dto;

import backend.academy.scrapper.Model.Subscription;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListLinksResponse {
    private List<Subscription> links;
    private int size;
}
