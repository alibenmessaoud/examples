Hello World Example
===================

Instructions
------------

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
		% sayHello Neil
		Hello Neil!


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

		% fabric:import helloworld.scripts/helloworld-system.xml
		% fabric:deploy helloworld-system
		% sayHello Neil
		Hello Neil!

4.	Update configuration:

		% fabric:config pid:com.example.hello language=de
		% sayHello Neil
		Guten Tag Neil!
