package example.jdbc.movies;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @since 30.05.12
 */
public class MovieService {

    private final String uri;

    public MovieService(String uri) {
        this.uri = uri;
    }

    public Map<String, Object> findMovie(String title) {
        if (title == null) return Collections.emptyMap();
        try (Connection connection = connect(uri);
             PreparedStatement statement = connection.prepareStatement(
                     "MATCH (movie:Movie {title:$1})" +
                             " OPTIONAL MATCH (movie)<-[r]-(person:Person)\n" +
                             " RETURN movie.title as title, collect({name:person.name, job:head(split(toLower(type(r)),'_')), role:r.roles}) as cast LIMIT 1")) {
            statement.setString(1, title);
            Map<String, Object> result = new HashMap<>(2);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.put("title", resultSet.getString("title"));
                    result.put("cast", resultSet.getArray("cast").getArray());
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Could not find movie", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Iterable<Map<String, Object>> search(String query) {
        if (query == null || query.trim().isEmpty()) return Collections.emptyList();
        try (Connection connection = connect(uri);
             PreparedStatement statement = connection.prepareStatement(
                     "MATCH (movie:Movie)\n" +
                             " WHERE movie.title =~ $1\n" +
                             " RETURN movie")) {
            statement.setString(1, "(?i).*" + query + ".*");
            List<Map<String, Object>> results = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> movie = resultSet.getObject("movie", Map.class);
                    results.add(Map.of("movie", movie));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Could not find movie", e);
        }
    }

    public Map<String, Object> graph(int limit) {
        try (Connection connection = connect(uri);
             PreparedStatement statement = connection.prepareStatement(
                     "MATCH (m:Movie)<-[:ACTED_IN]-(a:Person) " +
                             " RETURN m.title as movie, collect(a.name) as cast " +
                             " LIMIT $1")) {

            statement.setInt(1, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> nodes = new ArrayList<>();
                List<Map<String, Object>> rels = new ArrayList<>();
                int i = 0;
                while (resultSet.next()) {
                    nodes.add(Map.of("title", resultSet.getString("movie"), "label", "movie"));
                    int target = i;
                    i++;
                    for (Object name : (Object[]) resultSet.getArray("cast").getArray()) {
                        Map<String, Object> actor = Map.of("title", name, "label", "actor");
                        int source = nodes.indexOf(actor);
                        if (source == -1) {
                            nodes.add(actor);
                            source = i++;
                        }
                        rels.add(Map.of("source", source, "target", target));
                    }
                }
                return Map.of("nodes", nodes, "links", rels);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find cast", e);
        }
    }

    private Connection connect(String uri) {
        try {
            return DriverManager.getConnection(uri);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to server", e);
        }
    }
}
