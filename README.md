# DDMSProjectAkkaCluster
Akka Cluster nodes connected to MySQL instances on various machine to provide fail over and fault tolerance

This Application demostrates the power of Akka Cluster and its features 

Each Akka cluster is Connected to a MySQL instance and 

There is a Akka Cluster Backend node which performs Database queries on its Database instance. Also, Each Akka cluster 

gossips to other nodes and keeps their membership. Basically it acts like a DynamoDB.


