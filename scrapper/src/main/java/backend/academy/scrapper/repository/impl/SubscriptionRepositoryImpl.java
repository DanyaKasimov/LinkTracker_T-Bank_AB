package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.Model.Subscription;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import backend.academy.scrapper.repository.SubscriptionRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final ConcurrentMap<Long, Set<Subscription>> dataBase = new ConcurrentHashMap<>();

    @Override
    public Subscription save(final Long chatId, final SubscriptionRequestDto dto) {
        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .link(dto.getLink())
                .tags(dto.getTags())
                .filters(dto.getFilters())
                .build();
        dataBase.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(subscription);
        return subscription;
    }

    @Override
    public Subscription delete(Long chatId, String link) {
        Set<Subscription> subscriptions = dataBase.get(chatId);
        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }

        return subscriptions.stream()
            .filter(s -> Objects.equals(s.getLink(), link))
            .findFirst()
            .map(s -> {
                subscriptions.remove(s);
                return s;
            })
            .orElse(null);
    }

    @Override
    public boolean existChatByLink(final Long chatId, final String link) {
        return dataBase.getOrDefault(chatId, Collections.emptySet()).stream()
                .anyMatch(subscription -> Objects.equals(subscription.getLink(), link));
    }

    @Override
    public boolean existByLink(String link) {
        return dataBase.values().stream()
            .flatMap(Set::stream)
            .anyMatch(subscription -> subscription.getLink().equals(link));
    }

    @Override
    public boolean existByChatId(final Long chatId) {
        return dataBase.containsKey(chatId);
    }

    @Override
    public Collection<Subscription> findAllByChatId(final Long chatId) {
        return dataBase.getOrDefault(chatId, Collections.emptySet()).stream().toList();
    }

    @Override
    public Collection<String> findAllLinksByLink(String url) {
        return dataBase.values().stream()
            .flatMap(Set::stream)
            .map(Subscription::getLink)
            .filter(link -> link.startsWith(url))
            .toList();
    }


    @Override
    public Collection<Long> findAllChatIdsByLink(String link) {
        return dataBase.entrySet().stream()
            .filter(entry -> entry.getValue().stream()
                .anyMatch(subscription -> Objects.equals(subscription.getLink(), link)))
            .map(Map.Entry::getKey)
            .toList();
    }
}
