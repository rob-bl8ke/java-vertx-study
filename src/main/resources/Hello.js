// Register a consumer on the event bus to listen for messages
vertx.eventBus().consumer("hello.vertx.addr", message => {
    message.reply("Hello Vert.x World! From JavaScript!");
});