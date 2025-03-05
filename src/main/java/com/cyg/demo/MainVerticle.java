package com.cyg.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);

        // GET without a path parameter
        router.get("/api/v1/hello").handler(ctx -> {
            ctx.request().response().end("Hello Vert.x World!");
        });

        // GET with a path parameter
        router.get("/api/v1/hello/:name").handler(ctx -> {
            String name = ctx.pathParam("name");
            ctx.request().response().end(String.format("Hello %s!", name));
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080);
    }
}
