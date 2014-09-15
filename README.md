RalloCloud
==========

Modeling and Optimization of Resource Allocation in Cloud

As cloud services continue to grow rapidly, the need for more efficient utilization of resources and better optimized distribution of workload emerges. Our aim is to provide a solution to this optimization problem from three perspectives: (1) Infrastructure level; (2) Application level; and (3) Platform level.

In infrastructure level, the problem is to distribute virtual machines (VMs) to the datacenters (DCs) with geographical locations in such a way that latency is minimized while WAN bandwidth and datacenter capacity limits are respected. We plan to model latency as a function of DC load, inter-DC communication and proximity to user. Both VM requests and cloud infrastructure can be represented by graphs where vertices correspond to machines (either physical or virtual) and edges correspond to network connections between them. Our approach will employ weighted graph similarity and subgraph matching to suggest an efficient placement or the list of migrations to reach an efficient placement.

VM request is an input to our optimization in infrastructure level but the requested number and capacity of VMs as well as their inter-connections may not be optimal for a specific cloud service. In application level, on the other hand, we plan to deal with the configuration of the MapReduce programming model (More specifically one of its implementations: Apache Hadoop) especially in terms of number of maps and reduces. Previous work has shown that the optimum configuration depends on the resource consumption of the cloud service and it can be determined by executing a fraction to generate a signature. Our aim is to determine the number of maps and reduces statically by analyzing the design model, source code and/or the input data.

Furthermore, in platform level we aim to analyze the shuffle and scheduling algorithms of the MapReduce system to achieve a better load balancing and less amount communication between nodes. Shuffle is the phase where the output of all map processors with the same key are assigned to the same reduce processor. We will carry out a cost based formal analysis of the existing Hadoop schedulers to determine which scheduler is more appropriate for given costs of map, reduce and shuffle.

In summary, a holistic approach will be carried out to improve the performance of cloud software by optimizing resource allocation. Intended optimizations are on infrastructure level (Resource Selection and Assignment), application level (Map Reduce Configuration) and platform level (Work Distribution and Load Balancing). Cloud environment and resource allocation problem will be modeled based on graphs and automata, and the solutions will be within this context as well.
