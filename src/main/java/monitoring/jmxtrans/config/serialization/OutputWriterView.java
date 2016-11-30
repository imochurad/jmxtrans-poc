package monitoring.jmxtrans.config.serialization;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

// @JsonSerialize(using = GraphiteWriterFactoryViewSerializer.class)
@JsonSerialize(using = SimpleGraphiteWriterViewSerializer.class)
public interface OutputWriterView {

}
