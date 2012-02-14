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
		Hello Neil

