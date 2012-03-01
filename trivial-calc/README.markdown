# OSGi Remote Services - Trivial Calculator Example

The 3 calculator bundles (api/cmd/service) implement a trivial but fully "live"
example of a remote service. The example is split into a remote service that
can add two numbers, and a command line client that can be used to invoke the
service.

## Build

All the dependencies are packaged with this example so just run ant from the top level directory
(where $CALC is the directory in which this file resides).

    $ cd $CALC/calc
    $ ant 

Now we create the required repository indexes. 

    $pwd 
      $CALC/calc
    $
    $ $FABRIC/bin/posh -c index calc build/lib build/lib/index-nim.xml

## Running the example

The posh script 'loadcalc.osh' encapsulates the commands necessary to run the
example (it is annotated to describe what eachcommand does):

start JVM #0

	$ $FABRIC/bin/posh
	[host.0]% sh loadcalc.osh client
	+ nim:add com.paremus.dosgi.topologymanager@active
	+ nim:add com.paremus.dosgi.dsw.essencermi@active
	+ nim:add com.paremus.dosgi.discovery.slp@active
	+ nim:add com.paremus.dosgi.examples.calculator.cmd@active
	+ calc:status
	Calculator service is currently not available.

Oops, no server running. Let's start it in a new JVM
start JVM #1

	$ $FABRIC/bin/posh
	[host.1]% sh loadcalc.osh server
	+ nim:add com.paremus.dosgi.topologymanager@active
	+ nim:add com.paremus.dosgi.dsw.essencermi@active
	+nim:add com.paremus.dosgi.discovery.slp@active
	+ nim:add com.paremus.dosgi.examples.calculator.service@active

back to JVM#0

	[host.0]% calc:status
	Calculator service is ready.
	[host.0]% calc:add 1 2
	-> 3

That's all! On-the-fly removal/addition of the transport provider is handled
correctly, as is removal/re-addition of the calculator service.
