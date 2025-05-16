package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.Model.Tag;
import backend.academy.scrapper.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcTagRepository implements TagRepository {
    private final JdbcTemplate jdbc;

    private static final String SQL_INSERT = "INSERT INTO tags (name, link_id) VALUES (?, ?)";
    private static final String SQL_EXISTS_BY_NAME = "SELECT COUNT(*) FROM tags WHERE name = ?";
    private static final String SQL_FIND_BY_NAME = "SELECT id, name, link_id FROM tags WHERE name = ?";
    private static final String SQL_DELETE = "DELETE FROM tags WHERE id = ?";
    private static final String SQL_DELETE_ALL_BY_LINK = "DELETE FROM tags WHERE link_id = ?";
    private static final String SQL_FIND_ALL_BY_LINK = "SELECT id, name, link_id FROM tags WHERE link_id = ?";
    private static final String SQL_DELETE_ALL_BY_LINKS = "DELETE FROM tags WHERE link_id IN (%s)";

    @Override
    public void save(Tag tag) {
        jdbc.update(SQL_INSERT, tag.getName(), tag.getLink().getId());
    }

    @Override
    @Transactional
    public void saveAll(Collection<Tag> tags) {
        jdbc.batchUpdate(SQL_INSERT, tags, tags.size(), (ps, tag) -> {
            ps.setString(1, tag.getName());
            ps.setLong(2, tag.getLink().getId());
        });
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = jdbc.queryForObject(SQL_EXISTS_BY_NAME, Integer.class, name);
        return count != null && count > 0;
    }

    @Override
    public Tag findByName(String name) {
        try {
            return jdbc.queryForObject(SQL_FIND_BY_NAME, (rs, rowNum) ->
                new Tag(
                    rs.getLong("id"),
                    rs.getString("name"),
                    new Link(rs.getLong("link_id"), null, null)
                ), name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void delete(Tag tag) {
        jdbc.update(SQL_DELETE, tag.getId());
    }

    @Override
    public void deleteAllByLink(Link link) {
        jdbc.update(SQL_DELETE_ALL_BY_LINK, link.getId());
    }

    @Override
    public List<Tag> findAllByLink(Link link) {
        return jdbc.query(SQL_FIND_ALL_BY_LINK, (rs, rowNum) ->
            new Tag(
                rs.getLong("id"),
                rs.getString("name"),
                link
            ), link.getId());
    }

    @Override
    public void deleteAllByLinkIn(Collection<Link> links) {
        if (links.isEmpty()) return;

        List<Long> ids = links.stream()
            .map(Link::getId)
            .toList();

        String placeholders = ids.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", "));

        String query = String.format(SQL_DELETE_ALL_BY_LINKS, placeholders);
        jdbc.update(query, ids.toArray());
    }
}
