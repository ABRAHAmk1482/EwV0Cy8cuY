// 代码生成时间: 2025-09-24 01:07:07
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

// 定义数学计算服务接口
public interface MathCalculationService {
    String ADD = "math.add";
    String SUBTRACT = "math.subtract";
    String MULTIPLY = "math.multiply";
    String DIVIDE = "math.divide";
    
    void add(int a, int b, Handler<AsyncResult<Integer>> resultHandler);
    void subtract(int a, int b, Handler<AsyncResult<Integer>> resultHandler);
    void multiply(int a, int b, Handler<AsyncResult<Integer>> resultHandler);
    void divide(int a, int b, Handler<AsyncResult<Double>> resultHandler);
}

// 实现数学计算服务接口
public class MathCalculationServiceImpl implements MathCalculationService {
    @Override
    public void add(int a, int b, Handler<AsyncResult<Integer>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(a + b));
    }

    @Override
    public void subtract(int a, int b, Handler<AsyncResult<Integer>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(a - b));
    }

    @Override
    public void multiply(int a, int b, Handler<AsyncResult<Integer>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(a * b));
    }

    @Override
    public void divide(int a, int b, Handler<AsyncResult<Double>> resultHandler) {
        if (b == 0) {
            resultHandler.handle(Future.failedFuture("Cannot divide by zero"));
        } else {
            resultHandler.handle(Future.succeededFuture((double) a / b));
        }
    }
}

// 创建一个Verticle来启动服务并处理HTTP请求
public class MathCalculationTool extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        // 创建一个服务代理并绑定到端口
        ServiceBinder binder = new ServiceBinder(vertx);
        MathCalculationService service = new MathCalculationServiceImpl();
        binder.setAddress(MathCalculationService.ADD).register(MathCalculationService.class, service);
        binder.setAddress(MathCalculationService.SUBTRACT).register(MathCalculationService.class, service);
        binder.setAddress(MathCalculationService.MULTIPLY).register(MathCalculationService.class, service);
        binder.setAddress(MathCalculationService.DIVIDE).register(MathCalculationService.class, service);
        
        // 创建一个路由器并配置路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/add").handler(this::handleAdd);
        router.post("/subtract").handler(this::handleSubtract);
        router.post="/multiply").handler(this::handleMultiply);
        router.post("/divide").handler(this::handleDivide);
        
        // 创建一个HTTP服务器并启动服务
        vertx.createHttpServer().requestHandler(router).listen(8080, result -> {
            if (result.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(result.cause());
            }
        });
    }

    // 处理加法请求
    private void handleAdd(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        int a = body.getInteger("a");
        int b = body.getInteger("b");
        vertx.eventBus().send(MathCalculationService.ADD, new JsonObject().put("a", a).put("b", b), reply -> {
            if (reply.succeeded()) {
                context.response().setStatusCode(200).end(reply.result().body().toString());
            } else {
                context.response().setStatusCode(400).end(reply.cause().getMessage());
            }
        });
    }

    // 处理减法请求
    private void handleSubtract(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        int a = body.getInteger("a");
        int b = body.getInteger("b");
        vertx.eventBus().send(MathCalculationService.SUBTRACT, new JsonObject().put("a", a).put("b", b), reply -> {
            if (reply.succeeded()) {
                context.response().setStatusCode(200).end(reply.result().body().toString());
            } else {
                context.response().setStatusCode(400).end(reply.cause().getMessage());
            }
        });
    }

    // 处理乘法请求
    private void handleMultiply(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        int a = body.getInteger("a");
        int b = body.getInteger("b");
        vertx.eventBus().send(MathCalculationService.MULTIPLY, new JsonObject().put("a", a).put("b", b), reply -> {
            if (reply.succeeded()) {
                context.response().setStatusCode(200).end(reply.result().body().toString());
            } else {
                context.response().setStatusCode(400).end(reply.cause().getMessage());
            }
        });
    }

    // 处理除法请求
    private void handleDivide(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        int a = body.getInteger("a");
        int b = body.getInteger("b");
        vertx.eventBus().send(MathCalculationService.DIVIDE, new JsonObject().put("a", a).put("b", b), reply -> {
            if (reply.succeeded()) {
                context.response().setStatusCode(200).end(reply.result().body().toString());
            } else {
                context.response().setStatusCode(400).end(reply.cause().getMessage());
            }
        });
    }
}
