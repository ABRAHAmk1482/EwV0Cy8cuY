// 代码生成时间: 2025-10-11 19:25:53
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class CDNService extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(CDNService.class);
    private static final int MAX_CACHE_TIME = 3600; // Cache files for 1 hour

    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(vertx);

        // Serve static files from the 'webroot' directory
        StaticHandler staticHandler = StaticHandler.create()
            .setCachingEnabled(false)
            .setAllowRootFileSystemAccess(false);

        router.route(HttpMethod.GET, "/*").handler(staticHandler);

        // Start the HTTP server and listen on port 8080
        HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080));
        server.requestHandler(router).listen(res -> {
            if (res.succeeded()) {
                logger.info("CDN Service is running on port 8080");
                startFuture.complete();
            } else {
                logger.error("Failed to start CDN Service", res.cause());
                startFuture.fail(res.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        // Implement any necessary cleanup here
        stopFuture.complete();
    }

    /**
     * Handles caching of files. Vert.x's StaticHandler does not handle caching,
     * so we implement a simple caching mechanism here.
     *
     * @param context The routing context
     */
    private void cacheFile(RoutingContext context) {
        try {
            String requestPath = context.request().path();
            Path filePath = Paths.get("webroot", requestPath.substring(1)); // Remove leading slash

            // Check if the file exists and is within the webroot directory
            if (Files.exists(filePath) && filePath.normalize().startsWith(Paths.get("webroot").toAbsolutePath().normalize())) {
                Buffer cachedContent = vertx.fileSystem().readFileBlocking(filePath);

                // Set cache headers
                context.response().headers().add("Cache-Control", "max-age=" + MAX_CACHE_TIME);

                // Send the cached file content
                context.response().end(cachedContent);
            } else {
                // If the file is not found or outside the webroot, send a 404
                context.response().setStatusCode(404).end();
            }
        } catch (IOException e) {
            logger.error("Error caching file", e);
            context.response().setStatusCode(500).end("Internal Server Error");
        }
    }
}
