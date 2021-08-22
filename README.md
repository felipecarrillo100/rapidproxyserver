[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.felipecarrillo100/rapidproxyserver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.felipecarrillo100/rapidproxyserver)

# rapidproxyserver
Server side implementation for rapidproxy in Java and Spring

## Description
Rapidproxy is an easy to use proxy implementation that allows browsers to connect to sites using a proxy. The objective is to solve CORS issues and http on https issues when consuming data from web application.

This is the server side implementation written in Java for rapidproxy client that is written in Javascript.
[Rapidproxy](https://github.com/felipecarrillo100/rapidproxy) is the client side implemented that runs in the browser.

The idea is that when the site is not reachable directly, you can instead, send your request to a proxy located in one of your own machines or built in inside your Java application.

## To install
Get it from Maven, just add it to the maven dependencies of your main application.

## To wire
There is very little wiring required. You just need to configure the proxy using the properties file:

Example:
```
ogc.proxy.enabled=true
ogc.proxy.baseurl=/api/user/proxy
ogc.proxy.baseurl3d=/api/user/proxy3d
ogc.proxy.cors.enabled=true
```

* ogc.proxy.enabled: Enable the proxy 
* ogc.proxy.baseurl: The url entry point where the generic proxy is listening
* ogc.proxy.baseurl3d: The urls entry point for OGC 3D Tiles proxy
* ogc.proxy.cors.enabled: Set to true to enable CORS.  This is required if your web application is served from a different domain

## To use

Simple install [rapidproxy](https://github.com/felipecarrillo100/rapidproxy) in your JavaScript project and create a proxy.
Refer to the [rapidproxy](https://github.com/felipecarrillo100/rapidproxy) documentation for details.

## Expected behaviour
The server will create a proxy at the specified entry point. Make sure your client is configured to use the same entry point.
Client will send a request to the server.  The server will act as a reverse proxy forwarding the request to the real target. Any data provided by the remote target will be passed back to the client.


## Advanced customization
You may require to set your own customization, for this you may overwrite the ProxyRequest. If you decide to apply this options then you can
Create your own class derived from Proxy request and then tell RapidExplorerServer that you will use this class instaead.

Example:
```Java
public class CustomProxyRequest extends ProxyRequest {
    public CustomProxyRequest(HttpServletRequest req, String index, Principal principal) {
        super(req, index, principal);
    };
    // Dos something fancier
}
```

Now create a configuration bean to tell set the ProxyRequestProvider to your new class
```Java
@Configuration
public class AppConfig {
    @Bean(name="initializeCustomProxy")
    public void helloWorld() {
        System.out.println("CustomProxyRequest yy");
        ProxyRequestProvider.setProxyRequest(CustomProxyRequest.class);
    }
}
```

Sourcecode is provided, therefore you could fork and make your own custom implementation.
