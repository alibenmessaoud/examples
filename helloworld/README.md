Hello World Example
===================

See [Hello World Examples](https://docs.paremus.com/display/SF19/Tutorials)

Instructions for Blueprint Version
----------------------------------

1.	Build the bundles:

		$ ant -f build/build.xml build release

2.	Start the Blueprint service example:

		% nim:add osgi.active.bundle/helloworld.blueprint

3.	Start the CLI bundle and test the service:

		% nim:add osgi.active.bundle/helloworld.cli

4.	Lets test the service :)

		% sayHello Neil
                Hello Neil!
		% setpid com.example.hello language=de
		-------> Setting language to de
		% sayHello Neil
		Guten Tag, Neil.


Instructions for Declarative Service Version
----------------------------------

1.	Build the bundles:

                $ ant -f build/build.xml build release

2.	Start the Declarative Service example:

                % nim:add ms/com.example.hello.ds 

3.	Start the CLI bundle and test the service:

                % nim:add osgi.active.bundle/helloworld.cli

4.	Lets test the service :)

		% sayHello Neil
		Hello Neil!
		% 
		% setpid com.example.hello.ds language=fr
		-------> Setting language to fr
		% sayHello Neil
		Bonjour Neil.
		% pids
		com.example.hello.ds {language="fr"}


Deploy to Fibre
---------------

1.	Start a fibre and load the repository indexes

		% fibre -I
		...
		% fabric:connect

2.	Import, deploy and test the system:

		% fabric:import helloworld.scripts/helloworld-blueprint-system.xml
		% fabric:deploy helloworld-blueprint-system
		% sayHello Neil
		Hello Neil!

3.	Update configuration (using part:systemname/partname/pid):

		% fabric:config part:helloworld-blueprint-system/com.example.hello/com.example.hello language=de
		% sayHello Neil
		Guten Tag Neil!
