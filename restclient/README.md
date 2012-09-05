REST Fabric Admin Example Client Code
=====================================

This project contains a very simple 

Build
-----

1. Change to `com.paremus.example.restclient`
2. Execute `ant package`

The resulting output is `generated/restclient.jar`. This is a standalone executable JAR file containing an
embedded OSGi framework.

N.B.: an internet connection is required to build for the first time; after this the dependencies will be
cached in `${HOME/.bnd/cache`

Run
---

1. Execute `java -jar generated/restclient.jar`

Use
---

A simple Gogo shell is provided. Commands supported currently are:

* `repo:list`
* `system.list`
* `system:show`
* `system:install`

Use `help <command>` to get help for a specific command.

Note that the client is currently hard-coded to connect to a fabric on localhost, port 9000.
