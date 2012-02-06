# Paremus Service Fabric - 'System' examples #
## A distributed OSGi PaaS solution for the Enterprise ##

[Terminlogy and Concepts](https://docs.paremus.com/display/SF18/Terminology+and+Concepts)


## Gateway Example ##

### Overview ###

This example shows a simple pricing gateway set up that demonstrates the following:

* A trivial implementation of a gateway with command line client,
backend pricing engine and asynchronous gateway listeners.
* The gateway and the pricing engine are written in Scala for its
async behaviours and use OSGi declarative services as the component
model.
* There is a simple blueprint persistence gateway listener written in
Java that takes quotes/requests and writes them to disk
* The command line is a Java OSGi bundle
* The remote communications are pluggable via RSA but the demo willdefault to using SLP for discovery and EssenceRMI for the transport.
* Systems and repositories used to deploy the components on the fabric.

### Source ###

The source is split up into the following directories:

* gateway.api - shared Java api for components
* gateway.persistence - blueprint persistence component
* gateway.cli - simple osgi shell
* gateway.pricer - scala pricing engine using declarative services
* gateway.scripts - scripts to launch components in nimble
* gateway.system - systems to deploy components in fabric

### Build ###

All the dependencies are packaged with this example so just run ant from the top level gateway directory

    $ cd $examples
    $ ant -f gateway.build/build.xml

### Fabric ###


The final example is to deploy these components via the service fabric using a system document.

First launch a number of fibres with one providing the infrastructure services.

    $0 cd examples/gateway
    $0 $fabric/bin/posh
    $1 $fabric/bin/posh -kc fibre --type=infra
    $n $fabric/bin/posh -kc fibre

Now connect to the infrastructure fibre and import/deploy the gateway system.

    %0 fabric connect localhost:9101
    %0 sh gateway.scripts/systems/gateway.osh
    %0 fabric status -S gateway.system

Now use the gateway cli to connect and request some quotes

    %0 sh gateway.scripts/services/client.osh
    %0 gateway testbatch --batchSize=10000 --batchCount=10 --requestTimeout=60000


### Other deployment methods ####

See [README.singlevm] and [README.multivm]