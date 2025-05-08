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

@Repository
@RequiredArgsConstructor
public class JdbcTagRepository implements TagRepository {
    private final JdbcTemplate jdbc;

    private static final String INSERT = "INSERT INTO tags (name, link_id) VALUES (?, ?)";
    private static final String EXISTS_BY_NAME = "SELECT COUNT(*) FROM tags WHERE name = ?";
    private static final String FIND_BY_NAME = "SELECT id, name, link_id FROM tags WHERE name = ?";
    private static final String DELETE = "DELETE FROM tags WHERE id = ?";
    private static final String DELETE_ALL_BY_LINK = "DELETE FROM tags WHERE link_id = ?";
    private static final String FIND_ALL_BY_LINK = "SELECT id, name, link_id FROM tags WHERE link_id = ?";

    @Override
    public void save(Tag tag) {
        jdbc.update(INSERT, tag.getName(), tag.getLink().getId());
    }

    @Override
    @Transactional
    public void saveAll(Collection<Tag> tags) {
        jdbc.batchUpdate(INSERT, tags, tags.size(), (ps, tag) -> {
            ps.setString(1, tag.getName());
            ps.setLong(2, tag.getLink().getId());
        });
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = jdbc.queryForObject(EXISTS_BY_NAME, Integer.class, name);
        return count != null && count > 0;
    }

    @Override
    public Tag findByName(String name) {
        try {
            return jdbc.queryForObject(FIND_BY_NAME, (rs, rowNum) ->
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
        jdbc.update(DELETE, tag.getId());
    }

    @Override
    public void deleteAllByLink(Link link) {
        jdbc.update(DELETE_ALL_BY_LINK, link.getId());
    }

    @Override
    public List<Tag> findAllByLink(Link link) {
        return jdbc.query(FIND_ALL_BY_LINK, (rs, rowNum) ->
            new Tag(
                rs.getLong("id"),
                rs.getString("name"),
                link
            ), link.getId());
    }
}
