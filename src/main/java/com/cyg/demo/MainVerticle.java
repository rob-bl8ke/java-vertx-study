package com.cyg.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {

        vertx.deployVerticle((new HelloVerticle()));

        Router router = Router.router(vertx);
        // Exposed endpoints
        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloName);

        int httpPort;
        try {
            httpPort = Integer.parseInt(System.getProperty("http.port", "8080"));
        } catch (NumberFormatException e) {
            httpPort = 8080;
        }

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(httpPort);
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
