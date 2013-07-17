BUILD
-----

	ant -f build/build.xml release

RUN
---

The demo consists of two runtimes:

1. A server, which runs on a desktop or laptop computer cabable of running a full Service Fabric fibre.
2. A device runtime, which runs on a Raspberry Pi (model B, i.e. with ethernet).

Before starting either runtime, the laptop and the Raspberry Pi must be connected to a network on the same subnet. This can be achieved by connecting both devices to a router, or by running DHCP on either the laptop or the Pi and establishing a direct ethernet connection.

To start the server runtime, invoke the following command from the mqtt directory:

    posh -k load-system.osh

After startup, you will be able to open a web browser on the address `http://localhost:8000/` and view the Radiation Monitor application. The main data area will display the message "waiting for sensor data" because we have not yet started the geiger counter program.

To start the device runtime, copy the file `com.paremus.examples.geiger/generated/launch-geiger.jar` to the Raspberry Pi. Then launch it with:

    java -jar launch-geiger.jar

N.B.: A Java virtual machine for the Raspberry Pi can be downloaded from: `http://www.oracle.com/technetwork/java/embedded/downloads/javase/index.html`.