// 代码生成时间: 2025-09-23 21:26:52
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

public class ShoppingCartService extends AbstractVerticle {

    // Define a JsonObject to store the shopping cart data
    private JsonObject cart;

    @Override
    public void start(Future<Void> startFuture) {
        cart = new JsonObject();
        ServiceBinder binder = new ServiceBinder(vertx);
        // Bind the service to an address so it can be accessed remotely
        MessageConsumer<JsonObject> consumer = binder
            .setAddress("shopping.cart")
            .register(ShoppingCartService.class, this::handle);

        startFuture.complete();
    }

    // Handle incoming requests
    private void handle(JsonObject message) {
        switch (message.getString("action")) {
            case "add":
                addItem(message);
                break;
            case "remove":
                removeItem(message);
                break;
            case "get":
                getCart(message);
                break;
            default:
                // Error handling for unsupported actions
                replyWithError(message, "Unsupported action");
                break;
        }
    }

    // Add an item to the cart
    private void addItem(JsonObject message) {
        String itemId = message.getString("itemId");
        int quantity = message.getInteger("quantity");
        // Check if item already exists in the cart
        if (cart.containsKey(itemId)) {
            cart.put(itemId, cart.getInteger(itemId) + quantity);
        } else {
            cart.put(itemId, quantity);
        }
        // Reply with success message
        replyWithSuccess(message, "Item added to cart");
    }

    // Remove an item from the cart
    private void removeItem(JsonObject message) {
        String itemId = message.getString("itemId");
        if (cart.containsKey(itemId)) {
            cart.remove(itemId);
            // Reply with success message
            replyWithSuccess(message, "Item removed from cart");
        } else {
            // Reply with error message if item not found
            replyWithError(message, "Item not found in cart");
        }
    }

    // Retrieve the cart items
    private void getCart(JsonObject message) {
        // Reply with the cart data
        replyWithSuccess(message, cart);
    }

    // Helper method to reply with success message
    private void replyWithSuccess(JsonObject message, Object result) {
        JsonObject reply = new JsonObject();
        reply.put("status", "success");
        reply.put("result", result);
        vertx.eventBus().send(message.getString("replyAddress"), reply);
    }

    // Helper method to reply with error message
    private void replyWithError(JsonObject message, String errorMessage) {
        JsonObject reply = new JsonObject();
        reply.put("status", "error");
        reply.put("message", errorMessage);
        vertx.eventBus().send(message.getString("replyAddress"), reply);
    }
}
