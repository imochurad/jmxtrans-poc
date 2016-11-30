package monitoring.jmxtrans.config.serialization;

import lombok.Getter;
import monitoring.jmxtrans.output.SimpleGraphiteWriter;

/**
 * 
 * This class serves as a light view for the {@link SimpleGraphiteWriter} class
 *
 */
public class SimpleGraphiteWriterView implements OutputWriterView {
    
    @Getter
    private final String rootPrefix;
    @Getter
    private final String host;
    @Getter
    private final Integer port;
    
    public SimpleGraphiteWriterView(String rootPrefix, String host, Integer port) {
        this.rootPrefix = rootPrefix;
        this.host = host; 
        this.port = port;
    }

}
