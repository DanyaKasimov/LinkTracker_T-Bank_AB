package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.Model.Chat;
import backend.academy.scrapper.Model.Filter;
import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.Model.Tag;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.exceptions.NotFoundDataException;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.LinkService;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final LinksRepository linksRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;
    private final ChatService chatService;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;

    private static final String GITHUB_PATTERN = "^https://github\\.com/[a-zA-Z0-9\\-]+/[a-zA-Z0-9\\-]+$";
    private static final String STACKOVERFLOW_PATTERN =
            "^https://stackoverflow\\.com/questions/\\d{1,10}(?:/[a-zA-Z0-9\\-]{1,50})?$";

    @Override
    @Transactional
    public LinkResponse save(Long chatId, SubscriptionRequestDto dto) {
        Chat chat = chatService.findById(chatId);

        Optional<Link> existingLink = linksRepository.findByNameAndChat(dto.getLink(), chat);
        if (existingLink.isPresent()) {
            throw new InvalidDataException("Ссылка " + dto.getLink() + " уже существует");
        }

        if (!linkIsCorrect(dto.getLink())) {
            throw new InvalidDataException("Некорректная ссылка.");
        }

        Link link = linksRepository.save(
                Link.builder().name(dto.getLink()).chat(chat).build());

        saveTags(dto.getTags(), link);
        saveFilters(dto.getFilters(), link);

        return LinkResponse.builder()
                .id(chat.getUserId().toString())
                .link(link.getName())
                .tags(tagsOf(link))
                .filters(filtersOf(link))
                .build();
    }

    @Override
    @Transactional
    public LinkResponse delete(Long chatId, String linkName) {
        Chat chat = chatService.findById(chatId);
        Link link = linksRepository
                .findByNameAndChat(linkName, chat)
                .orElseThrow(() -> new InvalidDataException("Ссылка не найдена."));

        List<String> removedTags = tagsOf(link);
        List<String> removedFilters = filtersOf(link);

        tagRepository.deleteAllByLink(link);
        filterRepository.deleteAllByLink(link);
        linksRepository.delete(link);

        return LinkResponse.builder()
                .id(chat.getUserId().toString())
                .link(link.getName())
                .tags(removedTags)
                .filters(removedFilters)
                .build();
    }

    @Override
    public ListLinksResponse getAllLinks(Long chatId) {
        Chat chat = chatService.findById(chatId);
        List<Link> links = linksRepository.findAllByChat(chat);

        List<LinkResponse> responses = links.stream()
                .map(link -> LinkResponse.builder()
                        .id(link.getId().toString())
                        .link(link.getName())
                        .tags(tagsOf(link))
                        .filters(filtersOf(link))
                        .build())
                .toList();

        return new ListLinksResponse(responses, responses.size());
    }

    @Override
    public List<String> findAllLinksByLink(String urlPrefix) {
        return linksRepository.findAllByNameStartingWith(urlPrefix).stream()
                .map(Link::getName)
                .toList();
    }

    @Override
    public Collection<Long> findAllChatIdsByLink(String linkName) {
        List<Link> links = linksRepository.findAllByName(linkName);
        if (links.isEmpty()) {
            throw new InvalidDataException("Ссылки - " + linkName + " не существует");
        }
        return links.stream().map(link -> link.getChat().getUserId()).collect(Collectors.toSet());
    }

    @Override
    public Link findByLinkName(String link) {
        return linksRepository.findByName(link).orElseThrow(() -> new NotFoundDataException("Ссылка не найдена"));
    }

    private void saveTags(List<String> tagNames, Link link) {
        if (CollectionUtils.isEmpty(tagNames)) return;

        List<Tag> tags = tagNames.stream()
                .distinct()
                .map(name -> Tag.builder().name(name).link(link).build())
                .toList();

        tagRepository.saveAll(tags);
    }

    private void saveFilters(List<String> filterNames, Link link) {
        if (filterNames == null || filterNames.isEmpty()) return;

        List<Filter> filters = filterNames.stream()
                .distinct()
                .map(name -> Filter.builder().name(name).link(link).build())
                .toList();

        filterRepository.saveAll(filters);
    }

    private List<String> tagsOf(Link link) {
        return tagRepository.findAllByLink(link).stream().map(Tag::getName).toList();
    }

    private List<String> filtersOf(Link link) {
        return filterRepository.findAllByLink(link).stream()
                .map(Filter::getName)
                .toList();
    }

    public boolean linkIsCorrect(String link) {
        if (link == null) return false;
        if (link.matches(GITHUB_PATTERN)) return gitHubClient.urlIsValid(link);
        if (link.matches(STACKOVERFLOW_PATTERN)) return stackOverflowClient.urlIsValid(link);
        return false;
    }
}
