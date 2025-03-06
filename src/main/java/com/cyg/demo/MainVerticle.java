package com.cyg.demo;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

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
        vertx.deployVerticle(new HelloVerticle());

        // The reason why we need to create this curried result is because handler by default
        // only accepts one parameter, so we need to create a new handler that accepts two
        Handler<AsyncResult<Void>> dbMigrationResultHandler = result -> this.handleMigrationResult(start, result);
        vertx.executeBlocking(this::doDatabaseMigrations, dbMigrationResultHandler);

        Router router = Router.router(vertx);

        router.get("/api/v1/hello").handler(this::helloHandler);
        router.get("/api/v1/hello/:name").handler(this::helloByNameHandler);

        doConfig(start, router);
    }

    void handleMigrationResult(Promise<Void> start, AsyncResult<Void> result) {
        if (result.failed()) {
            start.fail(result.cause());
        }
    }

    void doDatabaseMigrations(Promise<Void> promise) {
        Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://127.0.0.1:5532/todo", "admin", "admin").load();

        try {
            flyway.migrate();
            promise.complete();
        } catch (FlywayException fe) {
            promise.fail(fe);
        }
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

    void helloHandler(RoutingContext ctx) {
        // Send a message to the event bus and expect a reply
        vertx.eventBus().request("hello.vertx.addr", "", reply -> {
            ctx.request().response().end((String)reply.result().body());
        });
    }

    void helloByNameHandler(RoutingContext ctx) {
        // Send a message to the event bus and expect a reply (with a name)
        String name = ctx.pathParam("name");
        vertx.eventBus().request("hello.name.addr", name, reply -> {
            ctx.request().response().end((String)reply.result().body());
        });
    }
}
