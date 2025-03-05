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
        // Send a message to the event bus and expect a reply
        vertx.eventBus().request("hello.vertx.addr", "", reply -> {
            ctx.request().response().end((String)reply.result().body());
        });
    }

    void helloName(RoutingContext ctx) {
        // Send a message to the event bus and expect a reply (with a name)
        String name = ctx.pathParam("name");
        vertx.eventBus().request("hello.name.addr", name, reply -> {
            ctx.request().response().end((String)reply.result().body());
        });
    }
}
