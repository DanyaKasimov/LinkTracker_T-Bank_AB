package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.repository.ChatRepository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepositoryImpl implements ChatRepository {

    private final ConcurrentMap<Long, Boolean> dataBase = new ConcurrentHashMap<>();

    @Override
    public void save(final Long id) {
        dataBase.put(id, true);
    }

    @Override
    public void delete(final Long id) {
        dataBase.put(id, false);
    }

    @Override
    public boolean isActive(final Long id) {
        return dataBase.getOrDefault(id, false);
    }
}
