JBoss Application Server Packager example
=============

These components provide a basic example for deploying Java EE applications into a packaged JBoss AS.

Prerequisites
--------------

* [Eclipse](http://www.eclipse.org)
* [Bndtools 2.0 or higher](http://bndtools.org)

Full Tutorial
--------------

[Available here](https://docs.paremus.com/display/SF19/Using+the+JBoss+AS+package+type)

Quick start
------------

# Import the folders as "existing Eclipse projects" into your Eclipse Workspace
# Open the *runJavaEE.bndrun* file in the *com.paremus.packager.test.javaee* project, and click *Run OSGi*.
# Go to [http://localhost:8081/system/console/configManager](http://localhost:8081/system/console/configManager) and enter the following in the JBoss ASProperties: *com.paremus.packager.test*, *0*, *javaee*, *jboss-as-helloworld*, *jboss-as-ejb-in-war* (you'll need to click the plus to add the extra context root).
# Save the config
# JBoss will start, and the applications will be available at [http://localhost:8080/jboss-as-helloworld](http://localhost:8080/jboss-as-helloworld) and [http://localhost:8080/jboss-as-ejb-in-war](http://localhost:8080/jboss-as-ejb-in-war).

