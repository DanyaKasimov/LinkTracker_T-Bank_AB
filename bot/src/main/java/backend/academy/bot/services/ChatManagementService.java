package backend.academy.bot.services;

import backend.academy.bot.dto.LinkUpdateDto;
import org.springframework.stereotype.Service;

public interface ChatManagementService {

    String registerChat(String id);

    String deleteChat(String id);

    void sendUpdates(LinkUpdateDto id);
}
