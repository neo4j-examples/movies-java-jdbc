package example.jdbc.movies;

import example.jdbc.env.Environment;
import spark.Spark;

import static spark.Spark.externalStaticFileLocation;

/**
 * @author Michael Hunger @since 22.10.13
 */
public class MovieServer {

    public static void main(String[] args) {
        Spark.port(Environment.getWebPort());
        externalStaticFileLocation("src/main/webapp");
        final MovieService service = new MovieService(Environment.getNeo4jUrl());
        new MovieRoutes(service).init();
    }
}
