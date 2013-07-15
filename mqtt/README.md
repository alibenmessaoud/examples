BUILD
-----

	ant -f build/build.xml release

RUN
---

The demo consists of two runtimes:

1. A server, which runs on a desktop or laptop computer cabable of running a full Service Fabric fibre.
2. A device runtime, which runs on a Raspberry Pi (model B, i.e. with ethernet).

Before starting either runtime, the laptop and the Raspberry Pi must be connected to a network on the same subnet. This can be achieved by connecting both devices to a router, or by running DHCP on either the laptop or the Pi and establishing a direct ethernet connection.

