Hello World Example
===================

Instructions for Blueprint Version
----------------------------------

1.	Build the bundles:

		$ ant -f helloworld.build/build.xml build release

2.	Build the repository indexes:

		$ posh helloworld.scripts/repos/makeRepos.osh

3.	Start posh and load repositories:

		$ posh
		% sh helloworld.scripts/repos/loadRepos.osh

4.	Start the Blueprint service example:

		% nim:add osgi.active.bundle/helloworld.blueprint

5.	Start the CLI bundle and test the service:

		% nim:add osgi.active.bundle/helloworld.cli

6.	Lets test the service :)

		% sayHello Neil
                Hello Neil!
		% setpid com.example.hello language=de
		-------> Setting language to de
		% sayHello Neil
		Guten Tag, Neil.


Instructions for Declarative Service Version
----------------------------------

1.	Build the bundles:

                $ ant -f helloworld.build/build.xml build release

2.	Build the repository indexes:

                $ posh helloworld.scripts/repos/makeRepos.osh

3.	Start posh and load repositories:

                $ posh
                % sh helloworld.scripts/repos/loadRepos.osh

4.	Start the Declarative Service example:

                % nim:add ms/com.example.hello.ds 

5.	Start the CLI bundle and test the service:

                % nim:add osgi.active.bundle/helloworld.cli

6.	Lets test the service :)

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

1.	Start a repository server:

		$ posh
		Welcome to the Paremus Service Fabric!
		% poshx:sfs 9085 helloworld .
		% poshx:sfs -l
		http://192.168.0.157:9086/helloworld    /path/to/examples/helloworld

2.	Start a fibre and load the repository indexes

		% fibre -I
		...
		% fabric:connect
		...
		% fabric:repos -lmc http://192.168.0.157:9086/helloworld/repos.properties

3.	Deploy and test the system:

		% fabric:import helloworld.scripts/helloworld-blueprint-system.xml
		% fabric:deploy helloworld-blueprint-system
		% sayHello Neil
		Hello Neil!

4.	Update configuration (using part:systemname/partname/pid):

		% fabric:config part:helloworld-blueprint-system/com.example.hello/com.example.hello language=de
		% sayHello Neil
		Guten Tag Neil!
