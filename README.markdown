# Paremus Service Fabric - Example Applications #
### A distributed OSGi PaaS solution for the Enterprise ###

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

Where $EXAMPLES is the directory in which this file resides.

    $ cd $EXAMPLES/gateway
    $ ant -f gateway.build/build.xml

### Fabric ###

To deploy these components via the service fabric using a system document, first launch a number of fibres (runtime instances) with one providing the infrastructure services.

Where $FABRIC is the location of your Paremus Service Fabric installation and $EXAMPLES is the directory in which this file resides.

    $0 cd $EXAMPLES/gateway
    $0 $FABRIC/bin/posh
    $1 $FABRIC/bin/posh -kc fibre --type=infra
    $n $FABRIC/bin/posh -kc fibre

Now build the repositories that nimble will use to install the artifacts

    $0 sh gateway.scripts/repos/makeRepos.osh
    $0 sh gateway.scripts/repos/loadRepos.osh

Now connect to the infrastructure fibre and import/deploy the gateway system:

    %0 fabric:connect localhost:9101
    %0 sh gateway.scripts/systems/gateway.osh
    %0 fabric status -S gateway.system

Now use the gateway cli to connect and request some quotes:

    %0 sh gateway.scripts/services/client.osh
    %0 gateway testbatch --batchSize=10000 --batchCount=10 --requestTimeout=60000


### Other deployment methods ####

See [README_SINGLEVM.markdown](https://github.com/paremus/examples/blob/master/README_SINGLEVM.markdown) and [README_MULTIVM.markdown](https://github.com/paremus/examples/blob/master/README_MULTIVM.markdown)