zk-utils-master-slaves
======================
This is the ZooKeeper utility to implement "one master, multiple slaves deployment" of any Java service which implements
the required `zkutils.masterslaves.Service` interface.

Usage
======================
* Implement your "service" by implementing the `zkutils.masterslaves.Service` interface.
* Write a properties file to specify the service information (class path, service name, etc.), and also specify the ZooKeeper server
information. Refer to `src/test/resources/config.properties` for an example.
* Start your service using "zkutils.masterslaves.ServiceRunner", e.g. `java zkutils.masterslaves.ServiceRunner <the path to the config.properties file>`.
Refer to the "Run Configurations" in the Eclipse project for an example.