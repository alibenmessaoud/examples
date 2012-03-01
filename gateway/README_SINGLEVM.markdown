
## Nimble on a Single JVM ##

This version of the example shows how to setup the components so that they run in a single framework with
no remote communication. 

First launch posh in the gateway example directory.

    $ $fabric/bin/posh

Now build the repositories that nimble will use to install the artifacts

    % sh gateway.scripts/repos/makeRepos.osh
    % sh gateway.scripts/repos/loadRepos.osh

Install the parts of the example, note that Nimble will download the dependencies from the repository.

    % nim add osgi.active.bundle/gateway.cli
    % nim add msf/com.example.gateway id=foo
    % nim add msf/com.example.pricer#f type=firm
    % nim add msf/com.example.pricer#i type=indicative

Now use the gateway cli to connect and request some quotes

    % gateway login demo
    % gateway request paremus ibm oracle microsoft yahoo
    % gateway testbatch --batchSize=10000 --batchCount=10 --requestTimeout=60000
