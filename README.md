# Flame
Flame is a wrapper for mapping endpoints with [Spark](https://github.com/perwendel/spark) that aims to take care of boilerplate code while maintaining low-level freedom.

## What this offers
 * Request/response body parsing
 * Reflection-based endpoint mapping

## Example
```java
package io.github.flakey5.flametest;

// @Controller defines that a 
@Controller(prefix = "/v1")
public class DogController {
    @AllArgsConstructor
    public class DogResponse {
        private String url;
    }
    
    @Endpoint(path = "/dog")
    private DogResponse getDog(Request request) {
        return new DogResponse("https://dog.ceo/api/breeds/image/random");
    }
}

public class Main {
    public static void main(String[] args) {
        // Still use Spark's api however you like
        Spark.port(8080);
        
        // Map all the contollers (denoted by the @Controller annotation) in a package
        Flame.mapControllerPackage("io.github.flakey5.flametest");
        // Or, explicitly give it a class
        // Flame.mapClass(DogController.class);
        
        Spark.init();
    }
}
```