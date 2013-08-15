# Hello World #

[Hello World Examples](https://docs.paremus.com/display/SF19/Tutorials)

##Systems 

* Hello Fabric and Hello Remote examples uses:
    * ds.xml - a simple declarative service based Greeting server. 
    * blueprint.xml - a simple blueprint service based Greeting server.

* Hello Remote example uses:
    * greeting-local.xml - Greeting server (Declarative) constrained to deploy to a 'local' fibre. 
    * greeting-remote.xml - Greeting server (Declarative) constrained to deploy to a 'remote' fibre.
    * cli.xml - a command line service.

* Hello Upgrade example uses: 
    * hello_v1.0.xml - a local Greeting Service (Declarative) & local CLI 
    * hello_v1.1.xml - a local Greeting Service (Blueprint) & local CLI 
    * hello_v1.2.xml - a remote Greeting Service (Declarative) service & local CLI

* Hello Abstract example uses:
    * cli-abstract.xml - an Abstract system specifying the CLI 
    * greeting-includes.xml - a Greeting Service (Declarative) that includes the CLI template and constrains CLI to 'Local'.

* Hello, hello, hello example uses:
    * greeting-remote.xml - demonstration use of Replication Handler

