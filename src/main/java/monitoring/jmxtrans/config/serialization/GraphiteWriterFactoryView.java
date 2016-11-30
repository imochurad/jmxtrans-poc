package monitoring.jmxtrans.config.serialization;

import com.googlecode.jmxtrans.model.output.GraphiteWriterFactory;

import lombok.Getter;

/**
 * 
 * This class serves as a view of the {@link GraphiteWriterFactory} class
 *
 */
public class GraphiteWriterFactoryView implements OutputWriterView {
    
    @Getter
    private final String rootPrefix;
    @Getter
    private final String host;
    @Getter
    private final Integer port;
    @Getter
    private final Integer poolSize;
    @Getter
    private final String flushStrategy;
    @Getter
    private final Integer flushDelayInSeconds;
    
    public GraphiteWriterFactoryView(String rootPrefix, String host, Integer port, String flushStrategy, Integer flushDelayInSeconds, Integer poolSize) {
        this.rootPrefix = rootPrefix;
        this.host = host; 
        this.port = port;
        this.flushStrategy = flushStrategy;
        this.flushDelayInSeconds = flushDelayInSeconds;
        this.poolSize = poolSize;
    }

}
