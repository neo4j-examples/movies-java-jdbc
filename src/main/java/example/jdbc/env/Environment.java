package example.jdbc.env;

/**
 * @author Michael Hunger @since 22.10.13
 */
public class Environment {

    public static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_URL = "jdbc:neo4j:bolt://demo.neo4jlabs.com/movies?user=movies&password=movies&database=movies";

    public static int getWebPort() {
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(webPort, 10);
    }

    public static String getNeo4jUrl() {
        String url = System.getenv("NEO4J_URL");
        if (url == null || url.isEmpty()) {
            return DEFAULT_URL;
        }
        return url;
    }
}
