package backend.academy.bot.services;

import backend.academy.bot.dto.LinkUpdateDto;

public interface ChatManagementService {

    String registerChat(String id);

    String deleteChat(String id);

    void sendUpdates(LinkUpdateDto id);
}
