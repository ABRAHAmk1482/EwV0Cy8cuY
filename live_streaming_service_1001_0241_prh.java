// 代码生成时间: 2025-10-01 02:41:25
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.serviceproxy.ServiceException;

public class LiveStreamingService extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        // Create HTTP server
        HttpServer server = vertx.createHttpServer();

        // Define the router
        Router router = Router.router(vertx);

        // Serve static files
        router.route().handler(StaticHandler.create());

        // Create a SockJS handler
        SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(5000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

        // Add a bridge to allow communication between the server and clients
        BridgeOptions bridgeOptions = new BridgeOptions().addInboundPermitted(new PermittedOptions().setAddress("liveStreamIn"))
                .addOutboundPermitted(new PermittedOptions().setAddress("liveStreamOut"));
        sockJSHandler.bridge(bridgeOptions, event -> {
            BridgeEvent be = event.body();
            if (be.type() == BridgeEventType.SOCKET_DATA) {
                // Handle incoming data from the client
                String data = be.getRawMessage().toString();
                vertx.eventBus().publish("liveStreamIn", data);
            } else if (be.type() == BridgeEventType.SOCKET_READY) {
                // Handle client ready
                String sessionID = be.session().id();
                System.out.println("Client ready: " + sessionID);
            }
        });

        // Route for SockJS endpoint
        router.route("/eventbus/*").handler(sockJSHandler);

        // Start the HTTP server
        server.requestHandler(router).listen(8080);
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        LiveStreamingService liveStreamingService = new LiveStreamingService();
        vertx.deployVerticle(liveStreamingService);
    }
}
