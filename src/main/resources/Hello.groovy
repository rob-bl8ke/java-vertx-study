//  The Groovy verticle is a simple consumer that listens on the address  hello.named.addr  and replies with a greeting message. 
//  The Java verticle is a simple sender that sends a message to the address  hello.named.addr  and prints the reply.

vertx.eventBus().consumer("hello.named.addr").handler({ msg -> 
  def name = msg.body()
  msg.reply("Hello, ${msg.body()}! From Groovy")
})
 