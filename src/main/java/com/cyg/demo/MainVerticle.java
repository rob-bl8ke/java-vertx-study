package com.cyg.demo;

import org.infinispan.distribution.topologyaware.TopologyInfo.Cluster;

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
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.impl.CorsHandlerImpl;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> start) {

        vertx.deployVerticle((new HelloVerticle()));

        Router router = Router.router(vertx);

        // Enable clustered session storage
        // SessionStore clusteredSessionStore = ClusteredSessionStore.create(vertx);
        // router.route().handler(SessionHandler.create(clusteredSessionStore));
        // or
        // Enable local session storage
        // Store information about users... sets a Session Id header
        SessionStore localSessionStore = LocalSessionStore.create(vertx);
        router.route().handler(SessionHandler.create(localSessionStore));

        // CORS
        router.route().handler(CorsHandler.create("localhost"));
        // router.route().handler(CorsHandler.create("*.example.com"));
        // Careful using this (all) as you cannot allow credentials at the same time
        // router.route().handler(CorsHandler.create("*"));

        // Log every request coming in
        router.route().handler(LoggerHandler.create());

        // CSRF - Cross Site Request Forgery and using a generated secret
        router.route().handler(CSRFHandler.create(vertx, "wjO+ojoXMZUX8N0Vm8D1DXfzWJSznbcszYZM5pEOlTDPQTX4XLrENMePGbSBOBk9dagU1MwgVJR8"));
        // Generate a secret at the command line with "dd if=/dev/urandom bs=384 count=1 2>/dev/null | base64"
        // Copy a line and use it as your secret

        // Cookies are handled out-of-the-box by Vert.x session handling

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
