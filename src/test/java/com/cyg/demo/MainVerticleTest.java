package com.cyg.demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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

        // Override the configuration to restrict the number of instances to 1 for testing
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject().put("helloVerticle.instances", 1));

        vertx.deployVerticle(MainVerticle.class.getName(), options, res -> {
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
                        String response = ar.result().bodyAsString();
                        // Use a regular expression to match the expected pattern
                        String expectedPattern = "Hello John from [a-fA-F0-9\\-]{36}!";
                        context.assertTrue(response.matches(expectedPattern), "Response does not match the expected pattern");
    
                        async.complete();
                    } else {
                        context.fail(ar.cause());
                    }
                });

        // Wait up to 5 seconds for the test to complete
        async.awaitSuccess(5000);
    }
}