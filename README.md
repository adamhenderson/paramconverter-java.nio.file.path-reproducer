# ParamConverter for java.nio.file.Path Reproducer

## Reproducer

This project demonstrates an issue where a ParamConverter for a java.nio.file.Path @RestPath is not called.

Quarkus 3.1.2.Final

Based off a blank 'quarkus create' command.

There are 2 REST resource methods, one using a java.nio.file.Path as a parameter and another using a custom class PathWrapper.

``` java
@GET
@jakarta.ws.rs.Path("view/{path: .*}")
@Parameter(
    name = "path",
    example = "path/to/file",
    schema = @Schema(implementation = String.class)
)
@Produces(MediaType.TEXT_PLAIN)
public String view(@RestPath java.nio.file.Path path) {
    log.infof("GreetingResource.view(%s)", path.toString());

    return path.toString();
}
@GET
@jakarta.ws.rs.Path("viewwrapper/{path: .*}")
@Parameter(
    name = "path",
    example = "path/to/file",
    schema = @Schema(implementation = String.class)
)
@Produces(MediaType.TEXT_PLAIN)
public String viewWrapper(@RestPath PathWrapper path) {
    log.infof("GreetingResource.viewWrapper(%s)", path.toString());

    return path.toString();
}
```


The provider class (annotated with @Provider) gets called on startup but only for the PathWrapper case.

```java
@Provider
public class PathParamConverterProvider implements ParamConverterProvider {

    public static final Logger log = Logger.getLogger(PathParamConverterProvider.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        log.infof("getConverter(%s, %s, %s)", rawType.getName(), genericType.getTypeName(), annotations.length);

        if (rawType == java.nio.file.Path.class) {
            log.info("Path -> Looks like its assignable: " + rawType.getName());

            return (ParamConverter<T>) new ParamConverter<java.nio.file.Path>() {

                @Override
                public java.nio.file.Path fromString(String value) {
                    try {
                        java.nio.file.Path path = java.nio.file.Path.of(value);

                        return path;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                public String toString(java.nio.file.Path value) {

                    return value.toString();
                }

            };
        }

        if (rawType == PathWrapper.class) {
            log.info("PathWrapper -> Looks like its assignable: " + rawType.getName());
            return (ParamConverter<T>) new ParamConverter<PathWrapper>() {

                @Override
                public PathWrapper fromString(String value) {
                    try {
                        PathWrapper path = new PathWrapper(java.nio.file.Path.of(value));

                        return path;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                public String toString(PathWrapper value) {

                    return value.getPath().toString();
                }

            };
        }

        log.error("No ParamConverter for " + rawType.getName());
        return null;
    }

}
```


Also from looking at the logs appears to be called multiple times (4 times). NOTE: No references to java.nio.file.Path

```log
INFO  [org.acm.PathParamConverterProvider] (Quarkus Main Thread) getConverter(org.acme.PathWrapper, org.acme.PathWrapper, 1)
INFO  [org.acm.PathParamConverterProvider] (Quarkus Main Thread) Looks like its assignable: org.acme.PathWrapper
INFO  [org.acm.PathParamConverterProvider] (Quarkus Main Thread) getConverter(org.acmePathWrapper, org.acme.PathWrapper, 1)
INFO  [org.acm.PathParamConverterProvider] (Quarkus Main Thread) Looks like it sassignable: org.acme.PathWrapper
INFO  [org.acm.PathParamConverterProvider] (Quarkus Main Thread) getConverter(org.acme.PathWrapper, org.acme.PathWrapper, 1)
INFO  [org.acm.PathParamConverterProvider] (Quarkus Main Thread) Looks like its assignable: org.acme.PathWrapper
INFO  [org.acm.PathParamConverterProvider] (Quarkus Main Thread) getConverter(org.acme.PathWrapper, org.acme.PathWrapper, 1)
INFO  [org.acm.PathParamConverterProvider] (Quarkus Main Thread) Looks like its assignable: org.acme.PathWrapper
```


Invoking the endpoints using curl (or Swagger UI):

Version using a PathWrapper works!
```shell
curl -X 'GET' \
  'http://localhost:8080/viewwrapper/path%2Fto%2Ffile' \
  -H 'accept: text/plain'
```

But calling the version using java.nio.file.Path: 
```shell
curl -X 'GET' \
  'http://localhost:8080/view/path%2Fto%2Ffile' \
  -H 'accept: text/plain'
```
results in an exception:

```
2023-06-20 18:00:01,211 ERROR [io.qua.ver.htt.run.QuarkusErrorHandler] (executor-thread-1) HTTP Request to /view/path%2Fto%2Ffile failed, error id: c4dc5264-aaa4-4072-bd96-9004f237c606-1: java.lang.ClassCastException: class java.lang.String cannot be cast to class java.nio.file.Path (java.lang.String and java.nio.file.Path are in module java.base of loader 'bootstrap')
        at org.acme.GreetingResource$quarkusrestinvoker$view_8e89466ec36a527c8ce8264ef208573550b9777a.invoke(Unknown Source)
        at org.jboss.resteasy.reactive.server.handlers.InvocationHandler.handle(InvocationHandler.java:29)
        at io.quarkus.resteasy.reactive.server.runtime.QuarkusResteasyReactiveRequestContext.invokeHandler(QuarkusResteasyReactiveRequestContext.java:141)
        at org.jboss.resteasy.reactive.common.core.AbstractResteasyReactiveContext.run(AbstractResteasyReactiveContext.java:145)
        at io.quarkus.vertx.core.runtime.VertxCoreRecorder$14.runWith(VertxCoreRecorder.java:576)
        at org.jboss.threads.EnhancedQueueExecutor$Task.run(EnhancedQueueExecutor.java:2513)
        at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1538)
        at org.jboss.threads.DelegatingRunnable.run(DelegatingRunnable.java:29)
        at org.jboss.threads.ThreadLocalResettingRunnable.run(ThreadLocalResettingRunnable.java:29)
        at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
        at java.base/java.lang.Thread.run(Thread.java:1589)

```

