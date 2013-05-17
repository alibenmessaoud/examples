Java EE Application Server Packager examples
=============

These components provide a basic example for deploying Java EE applications into a packaged Java EE Application Server. Currently we have examples for JBoss AS 7.1.1 and Apache TomEE 1.5.2.

Prerequisites
--------------

* [Eclipse](http://www.eclipse.org)
* [Bndtools 2.0 or higher](http://bndtools.org)

Full Tutorial
--------------

[Available here](https://docs.paremus.com/display/SF19/Using+the+JBoss+AS+package+type)

Quick start
------------

1. Import the folders as "existing Eclipse projects" into your Eclipse Workspace
2. Open the *runJavaEEJBoss.bndrun* or *runJavaEETomee.bndrun* file in the *com.paremus.packager.test.javaee* project, and click *Run OSGi*.
3. Go to [http://localhost:8081/system/console/configManager](http://localhost:8081/system/console/configManager) and enter the following in the Java EEProperties: 
    - **com.paremus.packager.test** 
    - **0** 
    - **javaee**
    - **jboss-as-helloworld** and **jboss-as-ejb-in-war** (you'll need to click the plus to add the second context root).
4. Save the config
5. JBoss/TomEE will start, and the applications will be available at [http://localhost:8080/jboss-as-helloworld](http://localhost:8080/jboss-as-helloworld) and [http://localhost:8080/jboss-as-ejb-in-war](http://localhost:8080/jboss-as-ejb-in-war).

