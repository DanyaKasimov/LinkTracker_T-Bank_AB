package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.Model.Filter;
import backend.academy.scrapper.Model.Link;
import backend.academy.scrapper.repository.FilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcFilterRepository implements FilterRepository {

    private final JdbcTemplate jdbc;

    private static final String SQL_INSERT = "INSERT INTO filters (name, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    private static final String SQL_EXISTS_BY_NAME = "SELECT COUNT(*) FROM filters WHERE name = ?";
    private static final String SQL_FIND_BY_NAME = "SELECT id, name, link_id FROM filters WHERE name = ?";
    private static final String SQL_DELETE = "DELETE FROM filters WHERE id = ?";
    private static final String SQL_DELETE_ALL_BY_LINK = "DELETE FROM filters WHERE link_id = ?";
    private static final String SQL_FIND_ALL_BY_LINK = "SELECT id, name, link_id FROM filters WHERE link_id = ?";
    private static final String SQL_DELETE_ALL_BY_LINKS = "DELETE FROM filters WHERE link_id IN (%s)";

    @Override
    @Transactional
    public void saveAll(Collection<Filter> filters) {
        jdbc.batchUpdate(SQL_INSERT, filters, filters.size(), (ps, filter) -> {
            ps.setString(1, filter.getName());
            ps.setLong(2, filter.getLink().getId());
        });
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = jdbc.queryForObject(SQL_EXISTS_BY_NAME, Integer.class, name);
        return count != null && count > 0;
    }

    @Override
    public Filter findByName(String name) {
        return jdbc.queryForObject(SQL_FIND_BY_NAME, (rs, rowNum) ->
            new Filter(rs.getLong("id"), rs.getString("name"), new Link(rs.getLong("link_id"), null, null)), name);
    }

    @Override
    public void delete(Filter filter) {
        jdbc.update(SQL_DELETE, filter.getId());
    }

    @Override
    public void deleteAllByLink(Link link) {
        jdbc.update(SQL_DELETE_ALL_BY_LINK, link.getId());
    }

    @Override
    public List<Filter> findAllByLink(Link link) {
        return jdbc.query(SQL_FIND_ALL_BY_LINK, (rs, rowNum) ->
            new Filter(rs.getLong("id"), rs.getString("name"), link), link.getId());
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
