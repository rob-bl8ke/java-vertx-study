package com.cyg.demo;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        Async async = context.async();
        vertx.deployVerticle(MainVerticle.class.getName(), res -> {
            if (res.succeeded()) {
                // Consider adding a small delay to ensure the server is fully started
                // if you run into flakey test issues.
                // vertx.setTimer(1000, id -> async.complete());
                async.complete();
            } else {
                context.fail(res.cause());
            }
        });

        // Wait up to 5 seconds for the deployment to complete
        async.awaitSuccess(10000);
    }


    @After
    public void tearDown(TestContext context) {
        // Closes the Vert.x instance after each test.
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testHelloVertx(TestContext context) {
        WebClient client = WebClient.create(vertx);
        Async async = context.async();

        client.get(8080, "localhost", "/api/v1/hello")
                .send(ar -> {
                    if (ar.succeeded()) {
                        context.assertEquals("Hello Vert.x World!", ar.result().bodyAsString());
                        async.complete();
                    } else {
                        context.fail(ar.cause());
                    }
                });

        // Wait up to 5 seconds for the test to complete
        async.awaitSuccess(5000);
    }

    @Test
    public void testHelloName(TestContext context) {
        WebClient client = WebClient.create(vertx);
        Async async = context.async();

        client.get(8080, "localhost", "/api/v1/hello/John")
                .send(ar -> {
                    if (ar.succeeded()) {
                        context.assertEquals("Hello John!", ar.result().bodyAsString());
                        async.complete();
                    } else {
                        context.fail(ar.cause());
                    }
                });

        // Wait up to 5 seconds for the test to complete
        async.awaitSuccess(5000);
    }
}