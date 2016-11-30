package monitoring.jmxtrans.output;

import static com.google.common.base.Charsets.UTF_8;
import static com.googlecode.jmxtrans.util.NumberUtils.isNumeric;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.googlecode.jmxtrans.connections.SocketFactory;
import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.model.Server;
import com.googlecode.jmxtrans.model.ValidationException;
import com.googlecode.jmxtrans.model.naming.KeyUtils;
import com.googlecode.jmxtrans.model.output.BaseOutputWriter;
import com.googlecode.jmxtrans.model.output.Settings;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@NotThreadSafe
@EqualsAndHashCode(exclude = "pool")
@ToString
public class SimpleGraphiteWriter extends BaseOutputWriter {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleGraphiteWriter.class);

    private static final String DEFAULT_ROOT_PREFIX = "servers";

    private GenericKeyedObjectPool<InetSocketAddress, Socket> pool;

    private final String rootPrefix;
    private final InetSocketAddress address;


    @JsonCreator
    public SimpleGraphiteWriter(@JsonProperty("typeNames") ImmutableList<String> typeNames,
            @JsonProperty("booleanAsNumber") boolean booleanAsNumber,
            @JsonProperty("debug") Boolean debugEnabled,
            @JsonProperty("rootPrefix") String rootPrefix, @JsonProperty("host") String host,
            @JsonProperty("port") Integer port,
            @JsonProperty("settings") Map<String, Object> settings) {
        super(typeNames, booleanAsNumber, debugEnabled, settings);
        this.rootPrefix = firstNonNull(rootPrefix, (String) getSettings().get("rootPrefix"),
                DEFAULT_ROOT_PREFIX);
        if (host == null) {
            host = (String) getSettings().get(HOST);
        }
        if (host == null) {
            throw new NullPointerException("Host cannot be null.");
        }
        if (port == null) {
            port = Settings.getIntegerSetting(getSettings(), PORT, null);
        }
        if (port == null) {
            throw new NullPointerException("Port cannot be null.");
        }
        this.address = new InetSocketAddress(host, port);
        this.pool = createPool(new SocketFactory(), SocketFactory.class.getSimpleName());
    }

    private <K, V> GenericKeyedObjectPool<K, V> createPool(
            KeyedPoolableObjectFactory<K, V> factory, String poolName) {
        GenericKeyedObjectPool<K, V> pool = new GenericKeyedObjectPool<>(factory);
        pool.setTestOnBorrow(true);
        pool.setMaxActive(-1);
        pool.setMaxIdle(-1);
        pool.setTimeBetweenEvictionRunsMillis(MILLISECONDS.convert(5, MINUTES));
        pool.setMinEvictableIdleTimeMillis(MILLISECONDS.convert(5, MINUTES));
        return pool;
    }

    @Override
    public void validateSetup(Server server, Query query) throws ValidationException {
    }

    @Override
    protected void internalWrite(Server server, Query query, ImmutableList<Result> results)
            throws Exception {

        Socket socket = null;
        PrintWriter writer = null;

        try {
            socket = pool.borrowObject(address);
            // socket = new Socket(address.getHostString(), address.getPort());
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8),
                    false);

            List<String> typeNames = this.getTypeNames();

            for (Result result : results) {
                LOG.debug("Query result: {}", result);
                Map<String, Object> resultValues = result.getValues();
                for (Entry<String, Object> values : resultValues.entrySet()) {
                    Object value = values.getValue();
                    if (isNumeric(value)) {

                        String line = KeyUtils
                                .getKeyString(server, query, result, values, typeNames, rootPrefix)
                                .replaceAll("[()]", "_") + " " + value.toString() + " "
                                + result.getEpoch() / 1000 + "\n";
                        LOG.debug("Graphite Message: {}", line);
                        writer.write(line);
                    } else {
                        LOG.debug(
                                "Unable to submit non-numeric value to Graphite: [{}] from result [{}]",
                                value, result);
                    }
                }
            }
            writer.flush();
        } catch (Exception e) {
            LOG.error("Unable to write data to Graphite", e);
        } finally {
            if (writer != null && writer.checkError()) {
                LOG.error("Error writing to Graphite, clearing Graphite socket pool");
                pool.invalidateObject(address, socket);
            } else {
                pool.returnObject(address, socket);
            }
        }
    }


    public static final class Builder {
        private final ImmutableList.Builder<String> typeNames = ImmutableList.builder();
        private boolean booleanAsNumber;
        private Boolean debugEnabled;
        private String rootPrefix;
        private String host;
        private Integer port;

        private Builder() {}

        public Builder addTypeNames(List<String> typeNames) {
            this.typeNames.addAll(typeNames);
            return this;
        }

        public Builder addTypeName(String typeName) {
            typeNames.add(typeName);
            return this;
        }

        public Builder setBooleanAsNumber(boolean booleanAsNumber) {
            this.booleanAsNumber = booleanAsNumber;
            return this;
        }

        public Builder setDebugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
            return this;
        }

        public Builder setRootPrefix(String rootPrefix) {
            this.rootPrefix = rootPrefix;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public SimpleGraphiteWriter build() {
            return new SimpleGraphiteWriter(typeNames.build(), booleanAsNumber, debugEnabled,
                    rootPrefix, host, port, null);
        }
    }

}
