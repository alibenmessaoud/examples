Hello World Example
===================

See [Hello World Examples](https://docs.paremus.com/display/SF19/Tutorials)

Instructions for Blueprint Version
----------------------------------

1.	Build the bundles:

		$ ant -f build/build.xml build release

2. 	Start a Posh / Nimble run time.

		$FABRIC_HOME/bin/posh -C

3. 	Set the following -- temporary work around for an Aries BluePrint bug (fix on the way)
	        system:setproperty org.apache.aries.proxy.weaving.disabled org.objectweb.asm.*,org.slf4j.*,org.
apache.log4j.*,javax.*,ch.qos.logback.* 

3.      Set the local repositories

		% repos -l ~/Desktop/examples-master/helloworld/cnf/releaserepo/index-nim.xml
                % repos -l ~/Desktop/examples-master/helloworld/helloworld.blueprint/index-nim-blueprint.xml
                % repos -l https://www.bundlerepo.org/repos/oss/index-nim.xml

4.	Start the Blueprint service example:

		% nim:add ms/com.example.hello.bp 

5.	Start the CLI bundle and test the service:

		% nim:add osgi.active.bundle/helloworld.cli

6.	Lets test the service :)

		% sayHello Neil
                Hello Neil!
		% setpid com.example.hello language=de
		-------> Setting language to de
		% sayHello Neil
		Guten Tag, Neil.

7.	And remove blueprint

	        % nim:remove ms/com.example.hello.bp


Instructions for Declarative Service Version
----------------------------------


1.	Start the Declarative Service example:

                % nim:add ms/com.example.hello.ds 

2.	Start the CLI bundle and test the service:

                % nim:add osgi.active.bundle/helloworld.cli

3.	Lets test the service :)

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

		% fabric:import cnf/system/ds.xml
		% fabric:deploy helloworld-ds-system
		% sayHello Neil
		Hello Neil!

3.	Update configuration (using part:systemname/partname/pid):

		% fabric:config part:helloworld-ds-system/com.example.hello.ds/com.example.hello language.ds=de
		% sayHello Neil
		Guten Tag Neil!
