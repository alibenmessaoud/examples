This is a demonstration of a standalone System JAR file. The system document
and a repository of required bundles are combined into a single file,
`webconsole-system.jar`. When this System is deployed into a Fabric it will run
a single instance of the [Apache Felix Web Console](https://felix.apache.org/site/apache-felix-web-console.html).



Rebuilding
==========

If either the System XML or the repository is modified, the System JAR will
need to be rebuilt. This can be done using the `jar` command as follows: 

    jar cmf manifest.txt webconsole-system.jar webconsole-system.xml repo

If the repository contents are modified then the repository index file `index-nim.xml` must be regenerated, *then* the System JAR rebuilt. To regenerate the index:

    cd repo
    posh -c nim:index webconsole . index-nim.xml

