package com.cyg.demo;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> start) {

        vertx.deployVerticle((new HelloVerticle()));

        Router router = Router.router(vertx);

        // Middleware to check for an auth token
        // Can use it to check for a valid token before allowing access to the API
        // Can also add headers (to allow CORS, etc.)
        router.route().handler(ctx -> {
            // ctx.response()
            //         .putHeader("content-type", "application/json")
            //         .putHeader("Access-Control-Allow-Origin", "*")
            //         .putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            //         .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
            //         .putHeader("Access-Control-Allow-Credentials", "true");

            // String authToken = ctx.request().getHeader("Authorization");
            String authToken = ctx.request().getHeader("AUTH_TOKEN");
            if (authToken == null || !authToken.equals("mySuperSecretAuthToken")) {
                ctx.response().setStatusCode(401).end("Unauthorized");
                return;
            }
            ctx.next();
        });

        

        // Exposed endpoints
        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloName);

        doConfig(start, router);
    }

    private void doConfig(Promise<Void> start, Router router) {
        ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setConfig(new JsonObject().put("path", "config.json"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(defaultConfig);

        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, options);

        // Curried function to handle the results of the configuration retrieval
        Handler<AsyncResult<JsonObject>> handler = asyncResult -> this.HandleConfigResults(start, router, asyncResult);
        configRetriever.getConfig(handler);
    }

    void HandleConfigResults(Promise<Void> start, Router router, AsyncResult<JsonObject> asyncResult) {
        if (asyncResult.succeeded()) {
            JsonObject config = asyncResult.result();
            JsonObject http = config.getJsonObject("http");

            // Can check that this works by changing the port in the config.json file
            int httpPort = http.getInteger("port", 8080);

            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(httpPort);
            
            start.complete();

        } else {
            System.out.println("Failed to retrieve the configuration.");
            start.fail("Unable to load configuration.");
        }
    }

    void helloVertx(RoutingContext ctx) {
        // Send a message to the event bus and expect a reply (from HelloVerticle)
        vertx.eventBus().request("hello.vertx.addr", "", reply -> {
            // Replies back over http to the client (browser)
            ctx.request().response().end((String)reply.result().body());
        });
    }

    void helloName(RoutingContext ctx) {
        // Send a message to the event bus and expect a reply (with a name from HelloVerticle)
        String name = ctx.pathParam("name");
        vertx.eventBus().request("hello.name.addr", name, reply -> {
            // Replies back over http to the client (browser)
            ctx.request().response().end((String)reply.result().body());
        });
    }
}
