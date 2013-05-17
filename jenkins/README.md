Jenkins Web Application Bundle Example
======================================

This example demonstrates deployment of a real-world web application into the Service Fabric. The application used for this purpose is [Jenkins][1].

Two modes of deployment are supported. The original, unchanged Jenkins WAR file can be deployed directly (in which case, it will be transformed on-the-fly into an OSGi bundle). Alternatively a minor build-time transformation can be applied which turns Jenkins into a Web Application Bundle (WAB) while remaining a valid WAR file that can still be deployed to traditional web containers. The WAB-style deployment is strongly recommended since it is faster.

This example uses [Eclipse Gemini Web][2] (which embeds Apache Tomcat) as its web container.

Running
-------

It is not necessary to build anything to run this example. Steps required to run are as follows, starting from an OS shell:

1. posh -kc fibre -I --tomcat
2. fabric:connect
3. fabric:repos -l $PWD/index-nim.xml
4. fabric:import jenkins.tomcat.system
5. fabric:deploy jenkins.tomcat.system

Once this is deployed, you should be able to use the Jenkins instance by pointing your web browser at `http://localhost:8080/jenkins`. Bear in mind that Jenkins is quite a large application and takes some time to start up.

Rebuilding
----------

The WAB for Jenkins is built using the original WAR file and a replacement manifest file, which can be found in `wab-manifest.txt`. If you want to experiment with changing this manifest, then the WAB can be rebuilt as follows:

	jar ufm jenkins-wab.jar wab-manifest.txt

References
----------

[1]: http://jenkins-ci.org/              "Jenkins"
[2]: http://wiki.eclipse.org/Gemini/Web  "Eclipse Gemini Web"
