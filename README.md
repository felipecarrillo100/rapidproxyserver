# rapidproxyserver
Server side implementation for rapidproxy in Java and Spring

## Description
Rapidproxy is an easy to use proxy implementation that allows browsers to connect to sites using a proxy. The objective is to solve CORS issues and http on https issues when consuming data from web application.

This is the server side implementation written in Java for rapidproxy.
[Rapidproxy](https://github.com/felipecarrillo100/rapidproxy) is the client side implemented in JavaScript.

The idea is that the site is not reachable directly, you can instead send your request to a proxy located in one of your own machines or built in inside your own Java application.

## To use
Get it from Maven, just add it to the maven dependencies of your main application

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
* ogc.proxy.baseurl: The url where the proxy is listening
* ogc.proxy.baseurl3d: This implementation is for OGC 3D Tiles
* ogc.proxy.cors.enabled: Set to true to enable CORS.  This is required if yur web application is served from a different domain

## To use

Simple install [rapidproxy](https://github.com/felipecarrillo100/rapidproxy) in your JavaScript project and create a proxy.
Refer to the [rapidproxy](https://github.com/felipecarrillo100/rapidproxy) documentation for details.
