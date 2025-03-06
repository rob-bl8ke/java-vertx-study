# Invoke endpoints

Use the [Rest Client extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) or simply use one of the terminal commands below:

```
curl -H "AUTH_TOKEN: mySuperSecretAuthToken" http://localhost:8089/api/v1/hello
```
or 
```
wget --header="AUTH_TOKEN: mySuperSecretAuthToken" http://localhost:8089/api/v1/hello
```
or
```
Invoke-RestMethod -Uri http://localhost:8089/api/v1/hello -Headers @{"AUTH_TOKEN"="mySuperSecretAuthToken"}
```

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

Add the `vertx-web` dependency to use the Router.

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

# Run the tests

```bash
  mvn clean install
  mvn test
```

# Issues

Following [this tutorial on clustering](https://www.youtube.com/watch?v=R3Iofu5byPk&list=PLkeCJDaCC2ZsnySdg04Aq9D9FpAZY6K5D&index=5), I currently cannot get it to work. The code is here and the tests run, but the following command simply does not work:

```bash
java -Djava.net.preferIPv4Stack=true -Dhttp.port=8090 -jar target/vertx-demo-1.0-SNAPSHOT.jar -cluster
```