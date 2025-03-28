package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.Model.Subscription;
import backend.academy.scrapper.dto.SubscriptionRequestDto;
import backend.academy.scrapper.repository.SubscriptionRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

        if (subscriptions == null) {
            return null;
        }

        Iterator<Subscription> iterator = subscriptions.iterator();
        while (iterator.hasNext()) {
            Subscription subscription = iterator.next();
            if (Objects.equals(subscription.getLink(), link)) {
                iterator.remove();
                return subscription;
            }
        }

        return null;
    }

    @Override
    public boolean existChatByLink(final Long chatId, final String link) {
        return dataBase.getOrDefault(chatId, Collections.emptySet()).stream()
                .anyMatch(subscription -> Objects.equals(subscription.getLink(), link));
    }

    @Override
    public boolean existByLink(String link) {
        for (Set<Subscription> subscriptions : dataBase.values()) {
            for (Subscription subscription : subscriptions) {
                if (subscription.getLink().equals(link)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean existByChatId(final Long chatId) {
        return dataBase.containsKey(chatId);
    }

    @Override
    public List<Subscription> findAllByChatId(final Long chatId) {
        return dataBase.getOrDefault(chatId, Collections.emptySet()).stream().toList();
    }

    @Override
    public List<String> findAllLinksGitHub() {
        return dataBase.values().stream()
            .flatMap(Set::stream)
            .map(Subscription::getLink)
            .filter(link -> link.startsWith("https://github.com/"))
            .toList();
    }

    @Override
    public List<String> findAllLinksStackOverflow() {
        return dataBase.values().stream()
            .flatMap(Set::stream)
            .map(Subscription::getLink)
            .filter(link -> link.startsWith("https://stackoverflow.com/"))
            .toList();
    }

    @Override
    public List<Long> findAllChatIdsByLink(String link) {
        return dataBase.entrySet().stream()
            .filter(entry -> entry.getValue().stream()
                .anyMatch(subscription -> Objects.equals(subscription.getLink(), link)))
            .map(Map.Entry::getKey)
            .toList();
    }
}
