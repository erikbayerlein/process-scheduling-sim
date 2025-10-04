# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.api.process-scheduling' is invalid and this project uses 'com.api.process_scheduling' instead.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.6/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.6/maven-plugin/build-image.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.6/reference/using/devtools.html)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/3.5.6/specification/configuration-metadata/annotation-processor.html)
* [Docker Compose Support](https://docs.spring.io/spring-boot/3.5.6/reference/features/dev-services.html#features.dev-services.docker-compose)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.6/reference/web/servlet.html)
* [Spring Reactive Web](https://docs.spring.io/spring-boot/3.5.6/reference/web/reactive.html)
* [Spring Web Services](https://docs.spring.io/spring-boot/3.5.6/reference/io/webservices.html)
* [WebSocket](https://docs.spring.io/spring-boot/3.5.6/reference/messaging/websockets.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Building a Reactive RESTful Web Service](https://spring.io/guides/gs/reactive-rest-service/)
* [Producing a SOAP web service](https://spring.io/guides/gs/producing-web-service/)
* [Using WebSocket to build an interactive web application](https://spring.io/guides/gs/messaging-stomp-websocket/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

