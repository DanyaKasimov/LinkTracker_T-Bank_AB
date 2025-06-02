package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.repository.ChatRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "JDBC")
@RequiredArgsConstructor
public class JdbcChatRepository implements ChatRepository {

    private final JdbcTemplate jdbcTemplate;

    public static final String SQL_EXISTS_BY_USER_ID = "SELECT COUNT(*) FROM chats WHERE user_id = ?";

    public static final String SQL_SAVE = "INSERT INTO chats (user_id) VALUES (?)";

    public static final String SQL_DELETE_BY_USER_ID = "DELETE FROM chats WHERE user_id = ?";

    public static final String SQL_FIND_BY_USER_ID = "SELECT id, user_id FROM chats WHERE user_id = ?";

    @Override
    public boolean existsByUserId(Long userId) {
        Integer count = jdbcTemplate.queryForObject(SQL_EXISTS_BY_USER_ID, Integer.class, userId);
        return count != null && count > 0;
    }

    @Override
    public void save(Chat chat) {
        jdbcTemplate.update(SQL_SAVE, chat.getUserId());
    }

    @Transactional
    @Override
    public void delete(Chat chat) {
        jdbcTemplate.update(SQL_DELETE_BY_USER_ID, chat.getUserId());
    }

    @Override
    public Optional<Chat> findByUserId(Long userId) {
        List<Chat> results = jdbcTemplate.query(
                SQL_FIND_BY_USER_ID, (rs, rowNum) -> new Chat(rs.getLong("id"), rs.getLong("user_id")), userId);
        return results.stream().findFirst();
    }
}
