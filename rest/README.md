REST Endpoint Publishing Example
================================

This example shows how the Remote Services Admin (RSA) infrastructure can be used to advertise the existence of a resource that is not necessarily an RPC-style invocation endpoint. In this case, a RESTful HTTP endpoint is advertised. A web-based client for the RESTful service is also included.

Building
--------

	ant -f build/build.xml release

Running
-------

The supplied script `build/load-system.osh` can be used to load a fibre, load the required repositories, and import and deploy the system document:

	posh -k build/load-system.osh

Once this is started, visit the following URL in your browser: `http://localhost:8000/`.

Scaling
-------

The system is configured to replicate across as many fibres as it can find. To start another instance, simply start another fibre using Atlas or with `posh -k -c fibre`. You should see a second REST endpoint appear in the dropdown in the web page (it will be necessary to reload the page).
