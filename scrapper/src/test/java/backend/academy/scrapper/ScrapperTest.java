package backend.academy.scrapper;

import backend.academy.scrapper.Model.Subscription;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.SubscriptionRepository;
import backend.academy.scrapper.repository.impl.SubscriptionRepositoryImpl;
import backend.academy.scrapper.service.SubscriptionService;
import backend.academy.scrapper.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScrapperTest {

    @Test
    void testSaveDataToRepository() {
        SubscriptionRepository repo = new SubscriptionRepositoryImpl();
        SubscriptionRequestDto dto = new SubscriptionRequestDto("https://github.com/user/repo", List.of("tag1"), List.of("filter"));
        Subscription subscription = repo.save(1L, dto);
        assertNotNull(subscription);
    }

    @Test
    void testAddRemoveLinksHappyPath() {
        SubscriptionRepository repo = new SubscriptionRepositoryImpl();
        assertFalse(repo.existByLink("https://github.com/user/repo"));
        Subscription sub = repo.save(1L, new SubscriptionRequestDto("https://github.com/user/repo", List.of(), List.of()));
        assertNotNull(sub);
        assertTrue(repo.existByLink("https://github.com/user/repo"));
        repo.delete(1L, "https://github.com/user/repo");
        assertFalse(repo.existByLink("https://github.com/user/repo"));
    }

    @Test
    void testDuplicateLinkShouldThrowException() {
        SubscriptionRepository repo = mock(SubscriptionRepository.class);
        ChatRepository chatRepo = mock(ChatRepository.class);
        GitHubClient gitHubClient = mock(GitHubClient.class);
        StackOverflowClient stackOverflowClient = mock(StackOverflowClient.class);
        SubscriptionService service = new SubscriptionServiceImpl(repo, chatRepo, gitHubClient, stackOverflowClient);

        Long chatId = 1L;
        String duplicateLink = "https://github.com/user/repo";
        SubscriptionRequestDto dto = new SubscriptionRequestDto(duplicateLink, List.of(), List.of());

        when(repo.existChatByLink(chatId, duplicateLink)).thenReturn(true);

        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> {
            service.save(chatId, dto);
        });

        assertEquals("Ссылка " + duplicateLink + " уже существует", exception.getMessage());

        verify(repo, never()).save(anyLong(), any(SubscriptionRequestDto.class));
    }

}
