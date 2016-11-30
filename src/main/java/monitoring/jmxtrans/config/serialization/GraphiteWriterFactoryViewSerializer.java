package monitoring.jmxtrans.config.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.googlecode.jmxtrans.model.output.GraphiteWriterFactory;

public class GraphiteWriterFactoryViewSerializer extends JsonSerializer<OutputWriterView> {
    @Override
    public void serialize(OutputWriterView value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException {
        
        jgen.writeStartObject();
        if (value instanceof GraphiteWriterFactoryView) {
            GraphiteWriterFactoryView graphiteWriterFactoryView = (GraphiteWriterFactoryView) value;
            jgen.writeStringField("@class", GraphiteWriterFactory.class.getName());
            jgen.writeStringField("rootPrefix", graphiteWriterFactoryView.getRootPrefix());
            jgen.writeStringField("host", graphiteWriterFactoryView.getHost());
            jgen.writeNumberField("port", graphiteWriterFactoryView.getPort());
            jgen.writeStringField("flushStrategy", graphiteWriterFactoryView.getFlushStrategy());
            
            if(graphiteWriterFactoryView.getFlushDelayInSeconds() != null)
                jgen.writeNumberField("flushDelayInSeconds", graphiteWriterFactoryView.getFlushDelayInSeconds());
            
            jgen.writeNumberField("poolSize", graphiteWriterFactoryView.getPoolSize());
        }
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(OutputWriterView value, JsonGenerator jgen,
            SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        serialize(value, jgen, provider);
    }

}

