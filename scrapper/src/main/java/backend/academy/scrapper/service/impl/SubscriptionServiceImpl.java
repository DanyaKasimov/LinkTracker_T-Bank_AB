package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.Model.Subscription;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.exceptions.NotFoundDataException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.SubscriptionRepository;
import backend.academy.scrapper.service.SubscriptionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private final ChatRepository chatRepository;

    private final GitHubClient gitHubClient;

    private final StackOverflowClient stackOverflowClient;

    @Override
    public Subscription save(final Long chatId, final SubscriptionRequestDto dto) {
        if (subscriptionRepository.existChatByLink(chatId, dto.getLink())) {
            throw new InvalidDataException("Ссылка " + dto.getLink() + " уже существует");
        }

        if (!linkIsCorrect(dto.getLink())) {
            throw new InvalidDataException("Некорректная ссылка.");
        }

        return subscriptionRepository.save(chatId, dto);
    }

    @Override
    public LinkResponse delete(final Long chatId, final String link) {
        if (!chatRepository.isActive(chatId)) {
            throw new InvalidDataException("Чата не существует.");
        }

        if (!subscriptionRepository.existChatByLink(chatId, link)) {
            throw new NotFoundDataException("Подписки не существует.");
        }

        Subscription subscription = subscriptionRepository.delete(chatId, link);

        return LinkResponse.builder()
            .id(chatId.toString())
            .link(subscription.getLink())
            .tags(subscription.getTags())
            .filters(subscription.getFilters())
            .build();
    }

    @Override
    public ListLinksResponse getAllLinks(final Long chatId) {
        if (!chatRepository.isActive(chatId)) {
            throw new NotFoundDataException("Чата не существует.");
        }

        List<Subscription> subscriptionList = subscriptionRepository.findAllByChatId(chatId);
        return ListLinksResponse.builder()
                .links(subscriptionList)
                .size(subscriptionList.size())
                .build();
    }

    @Override
    public List<String> findAllLinksGitHub() {
        return subscriptionRepository.findAllLinksGitHub();
    }

    @Override
    public List<String> findAllLinksStackOverflow() {
        return subscriptionRepository.findAllLinksStackOverflow();
    }

    @Override
    public List<Long> findAllChatIdsByLink(String link) {
        if (!subscriptionRepository.existByLink(link)) {
            throw new InvalidDataException("Ссылки - " + link + " не существует");
        }

        return subscriptionRepository.findAllChatIdsByLink(link);
    }

    public boolean linkIsCorrect(String link) {
        final String GITHUB_PATTERN = "^https://github\\.com/[a-zA-Z0-9\\-]+/[a-zA-Z0-9\\-]+$";
        final String STACKOVERFLOW_PATTERN = "^https://stackoverflow\\.com/questions/\\d+(?:/[a-zA-Z0-9\\-]+)?$";

        if (link == null) {
            return false;
        }
        if (link.matches(GITHUB_PATTERN)) {
            gitHubClient.getLatestCommitHash(link);
            return true;
        }
        if (link.matches(STACKOVERFLOW_PATTERN)) {
            stackOverflowClient.getLatestAnswer(link);
            return true;
        }
        return false;
    }
}
