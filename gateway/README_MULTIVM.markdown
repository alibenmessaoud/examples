
## Nimble on Multi JVMs ##

This version of the example launches several nimble instances and installs the required services via 
posh scripts. 

    $0 $fabric/bin/posh
    $1 $fabric/bin/posh
    $2 $fabric/bin/posh

    %0 sh gateway.scripts/services/client.osh
    %1 sh gateway.scripts/services/impl.osh
    %2 sh gateway.scripts/services/pricer.osh

Now use the gateway cli to connect and request some quotes

    %0 gateway testbatch --batchSize=10000 --batchCount=10 --requestTimeout=60000
