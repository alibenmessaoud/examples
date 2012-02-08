# Paremus Service Fabric - Example Applications #
### A distributed OSGi PaaS solution for the Enterprise ###

[Terminlogy and Concepts](https://docs.paremus.com/display/SF18/Terminology+and+Concepts)


## Gateway Example ##

### Overview ###

This example shows a simple pricing gateway set up that demonstrates the following:

* A simple implementation of a Financial Services Pricing service with command line client,
backend pricing engine and asynchronous gateway listeners.
* The gateway and the pricing engine are written in Scala for its
async behaviours and use OSGi declarative services as the component
model.
* There is a simple blueprint persistence gateway listener written in
Java that takes quotes/requests and writes them to disk
* The command line is a Java OSGi bundle
* The remote communications are pluggable via RSA but the demo will default to using DDS for discovery and EssenceRMI for the transport.
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

Now we create the required repository indexes. Start an instance of posh in the $EXAMPLES directory and run the makeRepos.osh script. 

    $0 cd $EXAMPLES/gateway
    $0 $FABRIC/bin/posh
    %0 sh gateway.scripts/repos/makeRepos.osh

Where $EXAMPLES is the gateway example directory. 
You have now created the required repository indexes. 

### Fabric ###

To deploy these components via the service fabric using a system document, first launch a number of fibres (runtime instances) with one providing the infrastructure services. Please note that in this example they are all running on the same physical machine.

Where $FABRIC is the location of your Paremus Service Fabric installation.

    $0 cd $EXAMPLES/gateway
    $0 $FABRIC/bin/posh
    $1 $FABRIC/bin/posh -kc fibre --type=infra
    $n $FABRIC/bin/posh -kc fibre

Now connect to the infrastructure fibre and import/deploy the gateway system:

    %0 fabric:connect localhost:9101
    %0 sh gateway.scripts/systems/gateway.osh
    %0 fabric status -S gateway.dds.system

NOTE: Check the HTTP port announced when starting the infrastructure fibre. Depending on sequence the fibres were started in - you may need to connect to localhost:9000 or localhost:9001

Now use the gateway cli to connect and request some quotes:

    %0 sh gateway.scripts/services/client.osh
    %0 gateway login demo
    %0 gateway request paremus ibm oracle microsoft yahoo
    %0 gateway testbatch --batchSize=10000 --batchCount=10 --requestTimeout=60000


### Other deployment methods ####

See [README_SINGLEVM.markdown](https://github.com/paremus/examples/blob/master/README_SINGLEVM.markdown) and [README_MULTIVM.markdown](https://github.com/paremus/examples/blob/master/README_MULTIVM.markdown)
