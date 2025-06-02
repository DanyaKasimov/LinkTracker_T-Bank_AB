package backend.academy.scrapper.dto.response;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListLinksResponse {
    private Collection<LinkResponse> links;
    private int size;
}
