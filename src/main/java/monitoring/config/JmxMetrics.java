package monitoring.config;

public interface JmxMetrics {


    String jmxPath();

    String attribute();
    
    Integer frequency();

    String alias();

}
