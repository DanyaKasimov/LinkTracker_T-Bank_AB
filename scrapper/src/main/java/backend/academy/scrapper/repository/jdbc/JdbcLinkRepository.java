package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.Model.Chat;
import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.repository.LinksRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "JDBC")
@RequiredArgsConstructor
public class JdbcLinkRepository implements LinksRepository {
    private final JdbcTemplate jdbc;

    private static final String SQL_FIND_BY_NAME = "SELECT id, name, chat_id FROM links WHERE name = ?";
    private static final String SQL_FIND_BY_NAME_AND_CHAT = "SELECT id, name, chat_id FROM links WHERE name = ? AND chat_id = ?";
    private static final String SQL_FIND_ALL_BY_CHAT = "SELECT id, name, chat_id FROM links WHERE chat_id = ?";
    private static final String SQL_FIND_ALL_BY_NAME = "SELECT l.id AS link_id, l.name AS link_name, c.id AS chat_id, c.user_id FROM links l JOIN chats c ON l.chat_id = c.id WHERE l.name = ?";
    private static final String SQL_FIND_ALL_BY_NAME_PREFIX = "SELECT id, name, chat_id FROM links WHERE name LIKE ?";
    private static final String SQL_SAVE = "INSERT INTO links (name, chat_id) VALUES (?, ?) RETURNING id";
    private static final String SQL_DELETE = "DELETE FROM links WHERE id = ?";
    private static final String SQL_EXISTS_BY_NAME = "SELECT COUNT(*) FROM links WHERE name = ?";
    private static final String SQL_DELETE_ALL = "DELETE FROM links WHERE id IN (%s)";

    @Override
    public Optional<Link> findByName(String name) {
        return jdbc.query(SQL_FIND_BY_NAME, (rs, rowNum) -> new Link(
            rs.getLong("id"),
            rs.getString("name"),
            Chat.builder().id(rs.getLong("chat_id")).build()
        ), name).stream().findFirst();
    }

    @Override
    public Optional<Link> findByNameAndChat(String name, Chat chat) {
        return jdbc.query(SQL_FIND_BY_NAME_AND_CHAT, (rs, rowNum) -> new Link(
            rs.getLong("id"),
            rs.getString("name"),
            Chat.builder().id(rs.getLong("chat_id")).build()
        ), name, chat.getId()).stream().findFirst();
    }

    @Override
    public List<Link> findAllByChat(Chat chat) {
        return jdbc.query(SQL_FIND_ALL_BY_CHAT, (rs, rowNum) -> new Link(
            rs.getLong("id"),
            rs.getString("name"),
            Chat.builder().id(rs.getLong("chat_id")).build()
        ), chat.getId());
    }

    @Override
    public List<Link> findAllByName(String name) {
        return jdbc.query(SQL_FIND_ALL_BY_NAME, (rs, rowNum) -> new Link(
            rs.getLong("link_id"),
            rs.getString("link_name"),
            Chat.builder()
                .id(rs.getLong("chat_id"))
                .userId(rs.getLong("user_id"))
                .build()
        ), name);
    }

    @Override
    public List<Link> findAllByNameStartingWith(String prefix) {
        return jdbc.query(SQL_FIND_ALL_BY_NAME_PREFIX, (rs, rowNum) -> new Link(
            rs.getLong("id"),
            rs.getString("name"),
            Chat.builder().id(rs.getLong("chat_id")).build()
        ), prefix + "%");
    }

    @Override
    @Transactional
    public Link save(Link link) {
        Long id = jdbc.queryForObject(SQL_SAVE, Long.class, link.getName(), link.getChat().getId());
        return new Link(id, link.getName(), link.getChat());
    }

    @Override
    @Transactional
    public void delete(Link link) {
        jdbc.update(SQL_DELETE, link.getId());
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = jdbc.queryForObject(SQL_EXISTS_BY_NAME, Integer.class, name);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void deleteAll(Collection<Link> links) {
        if (links.isEmpty()) return;

        List<Long> ids = links.stream()
            .map(Link::getId)
            .toList();

        String placeholders = ids.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", "));

        String query = String.format(SQL_DELETE_ALL, placeholders);
        jdbc.update(query, ids.toArray());
    }
}
