package com.cyg.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);

        // GET without a path parameter
        router.get("/api/v1/hello").handler(this::helloVertx);

        // GET with a path parameter
        router.get("/api/v1/hello/:name").handler(this::helloName);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080);
    }

    void helloVertx(RoutingContext ctx) {
        ctx.request().response().end("Hello Vert.x World!");
    }

    void helloName(RoutingContext ctx) {
        String name = ctx.pathParam("name");
        ctx.request().response().end(String.format("Hello %s!", name));
    }
}
