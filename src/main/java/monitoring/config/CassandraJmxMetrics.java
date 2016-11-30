package monitoring.config;

public enum CassandraJmxMetrics implements JmxMetrics {
    
    THROUGHPUT_WRITES("org.apache.cassandra.metrics:type=ClientRequest,scope=Write,name=Latency", "OneMinuteRate", "ThroughputWrites", 1),
    THROUGHPUT_READS("org.apache.cassandra.metrics:type=ClientRequest,scope=Read,name=Latency", "OneMinuteRate", "ThroughputReads", 1),
    LATENCY_WRITES("org.apache.cassandra.metrics:type=ClientRequest,scope=Write,name=TotalLatency", "Count", "LatencyWrites", 1),
    // TODO temporarily commenting this out since currently we are on a free HostedGraphite plan that doesn't allow more than 10 metrics 
    // since we have 3 C* nodes in a cluster, having 5 metric types will throw us over the limit
//    LATENCY_READS("org.apache.cassandra.metrics:type=ClientRequest,scope=Read,name=TotalLatency", "Count", "LatencyReads", 5),
//    LOAD("org.apache.cassandra.metrics:type=Storage,name=Load", "Count", "Load", 5)
    ;
    
    private final String path;
    private final String attribute;
    private final Integer frequency;
    private final String alias;
    
    private CassandraJmxMetrics(String path, String attribute, String alias, Integer frequency)
    {
        this.path = path;
        this.attribute = attribute;
        this.frequency = frequency;
        this.alias = alias;
    }

    @Override
    public String jmxPath() {
        return this.path;
    }

    @Override
    public String attribute() {
        return this.attribute;
    }

    @Override
    public Integer frequency() {
        return this.frequency;
    }
    
    @Override
    public String alias() {
        return this.alias;
    }
    
    public static String var()
    {
        return "CASSANDRA_JMX_URL";
    }

    public static String protocol() {
        return "cassandra+jmx";
    } 
    
}
