
# Create a new Vert.x project

```bash
mvn io.reactiverse:vertx-maven-plugin::setup

git init
git add . && git commit -m "initial generated vert.x project"

mvn clean compile vertx:run
```
Set up a MainVehicle, build a simple server to handle a GET request on the specified port.

```java
package com.cyg.demo;

import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.createHttpServer()
                .requestHandler(req -> req.response().end("Hello Vert.x World!"))
                .listen(8080);
    }
}
```


You'll see that Vert.x has started running. Browse to "http://localhost:8080/".

# Adding additional Vert.x Dependencies

When the project is generated a dependency chain is set up..

```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.vertx</groupId>
                <artifactId>vertx-stack-depchain</artifactId>
                <version>${vertx.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

This means that Vert.x dependencies can be added one after another and will be compatible with the vert.x specified vert.x version.

Add teh `vertx-web` dependency to use the Router.

```xml
    <dependencies>
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-core</artifactId>
        </dependency>
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-web</artifactId>
        </dependency>
    </dependencies>
```


