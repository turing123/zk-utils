zk-utils
========
[Apache ZooKeeper](http://zookeeper.apache.org/) is an elegant solution to enable 
"highly reliable distributed coordination". It can be used to implement some important 
distributed application facilities, such as configuration management, master-slaves 
deployment, distributed locks, etc.

Apache ZooKeeper provides some low level and abstract APIs for common distributed coordination tasks.
Usually we need to wrap these APIs for high level applications.

zk-utils is designed as a "middle level" between Apache ZooKeeper APIs and high level
applications by providing easy-to-use and reliable APIs for different use scenarios.

> **Note:** Currently zk-utils only provides Java APIs though Apache ZooKeeper provides both Java and C APIs.