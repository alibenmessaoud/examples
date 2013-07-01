# Hello World #

[Hello World Examples](https://docs.paremus.com/display/SF19/Tutorials)

##Systems 

* Hello Fabric and Hello Remote examples uses:
    * helloworld-ds-system.xml - a simple declarative service based Greeting server. 
    * helloworld-blueprint-system.xml - a simple blueprint service based Greeting server.

* Hello Remote example uses:
    * hello-server-local.xml - Greeting server (Declarative) constrained to deploy to a 'local' fibre. 
    * hello-server-remote.xml - Greeting server (Declarative) constrained to deploy to a 'remote' fibre.
    * hello-cli.xml - a command line service.

* Hello Upgrade example uses: 
    * hello_v1.0.xml - a local Greeting Service (Declarative) & local CLI 
    * hello_v1.1.xml - a local Greeting Service (Blueprint) & local CLI 
    * hello_v1.2.xml - a remote Greeting Service (Declarative) service & local CLI

* Hello Abstract example uses:
    * cli.xml - an Abstract system specifying the CLI 
    * hellogs_v1.0.xml - a Greeting Service (Declarative) that extends Abstract system & constrains CLI to 'local'.

* Hello, hello, hello example uses:
    * hello-server-remote.xml - demonstration use of Replication Handler

