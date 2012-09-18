DataNucleus Example Application
===============================

This example builds a simple application based on DataNucleus using the JDO API.

BUILD INSTRUCTIONS
------------------

1. Change to the `cnf` directory and copy `template.fabric.properties` to `fabric.properties`.
2. Edit `fabric.properties` and set the location of the Paremus Service Fabric or Nimble home directory.
3. Run `ant`. Artifacts will be generated, along with a Nimble index file, in the `cnf/releaserepo` directory.

DEPLOY/RUN
----------

1. Start up posh with an infra node.
2. Add the repositories: `fabric:repos -lm file:/Path/To/datanucleus/cnf/repos.properties`.
3. Import the system doc: EITHER `fabric:import datanucleus-derby-system.xml` OR
   `fabric:import datanucleus-mongodb.system.xml`.
4. Deploy the system: `fabric:deploy DataNucleusExample`
5. Wait for the system to be fully deployed. The `product:list` and `product:create` commands
   will now be available.

