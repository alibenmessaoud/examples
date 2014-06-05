The Paremus Fractal Demo
========================

This project contains the sources for a simple fractal viewer. The client is written in Angular JS, and the server uses JAX-RS and SSE to stream the image to the client.

The server-side rendering service has been deliberately written to add latency to calls, however the effects of this latency can be substantially reduced by making asynchronous calls to the service.

This application relies on Declarative Services, the Http Service, and the Async Service from OSGi Enterprise R6.

Building
========

The project can be built using Eclipse + bndtools, or by using ant -f cnf/build.xml release
