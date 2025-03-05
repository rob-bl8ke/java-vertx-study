package com.cyg.demo;

import java.util.UUID;

import io.vertx.core.AbstractVerticle;

public class HelloVerticle extends AbstractVerticle {

    // Identity of the verticle
    String verticleId = UUID.randomUUID().toString();

    @Override
    public void start() {

        // Register a consumer on the event bus to listen for messages
        vertx.eventBus().consumer("hello.vertx.addr", message -> {
            message.reply("Hello Vert.x World!");
        });

        // Register a consumer on the event bus to listen for messages
        // with a specific address and reply with a message that includes
        // the name from the message body
        vertx.eventBus().consumer("hello.name.addr", message -> {
            String name = (String) message.body();
            message.reply(String.format("Hello %s from %s!", name, verticleId));
        });
    }
    
}
