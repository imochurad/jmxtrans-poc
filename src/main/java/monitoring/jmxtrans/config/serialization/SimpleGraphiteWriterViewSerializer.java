package monitoring.jmxtrans.config.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import monitoring.jmxtrans.output.SimpleGraphiteWriter;

public class SimpleGraphiteWriterViewSerializer extends JsonSerializer<OutputWriterView> {
    @Override
    public void serialize(OutputWriterView value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException {
        
        jgen.writeStartObject();
        if (value instanceof SimpleGraphiteWriterView) {
            SimpleGraphiteWriterView graphiteWriterFactoryView = (SimpleGraphiteWriterView) value;
            jgen.writeStringField("@class", SimpleGraphiteWriter.class.getName());
            jgen.writeStringField("rootPrefix", graphiteWriterFactoryView.getRootPrefix());
            jgen.writeStringField("host", graphiteWriterFactoryView.getHost());
            jgen.writeNumberField("port", graphiteWriterFactoryView.getPort());
        }
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(OutputWriterView value, JsonGenerator jgen,
            SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        serialize(value, jgen, provider);
    }

}

