package example.jdbc.movies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.servlet.SparkApplication;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static spark.Spark.get;

public class MovieRoutes implements SparkApplication {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private final MovieService service;

    public MovieRoutes(MovieService service) {
        this.service = service;
    }

    public void init() {
        get("/movie/:title", (request, response) ->
                gson.toJson(service.findMovie(URLDecoder.decode(request.params("title"), StandardCharsets.UTF_8))));
        get("/search", (request, response) ->
                gson.toJson(service.search(request.queryParams("q"))));
        get("/graph", (request, response) -> {
            int limit = request.queryParams("limit") != null ? Integer.parseInt(request.queryParams("limit"), 10) : 100;
            return gson.toJson(service.graph(limit));
        });
    }
}
