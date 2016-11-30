package monitoring.jmxtrans.config.serialization;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Lists;
import com.googlecode.jmxtrans.model.Server;

import lombok.Getter;
import monitoring.config.JmxConnectionInfo;

/**
 * Serialize-friendly view of {@link Server} class
 * 
 * @author imochurad
 *
 */

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(value = {"alias", "host", "port", "username", "password", "numQueryThreads",
        "outputWriters", "queries"})

public class ServerView {

    @Getter
    private final String alias;
    @Getter
    private final String host;
    @Getter
    private final Integer port;
    @Getter
    private final String username;
    @Getter
    private final String password;
    /** The number of query threads for this server. */
    @Getter
    private final Integer numQueryThreads;
    
    @Nonnull
    @Getter
    private final List<OutputWriterView> outputWriters;

    @Getter
    private final List<QueryView> queries;

    @Getter
    @Nullable
    private final Integer runPeriodSeconds;

    private ServerView(String alias, String host, Integer port, String username, String password,
            Integer runPeriodSeconds, Integer numQueryThreads, List<QueryView> queries,
            List<OutputWriterView> outputWriters) {

        checkArgument(host != null && port != null, "You must provide host and port");

        this.alias = alias;
        this.port = port;
        this.username = username;
        this.password = password;
        this.runPeriodSeconds = runPeriodSeconds;
        this.numQueryThreads = numQueryThreads;
        this.queries = queries;
        this.host = host;
        this.outputWriters = outputWriters;
    }

    public static class ServerViewBuilder {
        private String alias;
        private String host;
        private Integer port;
        private String username;
        private String password;
        private Integer runPeriodSeconds;
        private Integer numQueryThreads;
        private List<QueryView> queries;
        private List<OutputWriterView> outputWriters = Lists.newArrayList();
        
        public ServerViewBuilder(final JmxConnectionInfo jmxConnectionInfo)
        {
             this(jmxConnectionInfo.getHost(), jmxConnectionInfo.getPort(), jmxConnectionInfo.getUsername(), jmxConnectionInfo.getPassword());
        }

        public ServerViewBuilder(final String host, final Integer port, final String username,
                final String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public ServerViewBuilder alias(final String alias) {
            this.alias = alias;
            return this;
        }

        public ServerViewBuilder runPeriodSeconds(final Integer runPeriodSeconds) {
            this.runPeriodSeconds = runPeriodSeconds;
            return this;
        }

        public ServerViewBuilder numQueryThreads(final Integer numQueryThreads) {
            this.numQueryThreads = numQueryThreads;
            return this;
        }

        public ServerViewBuilder queries(final List<QueryView> queries) {
            this.queries = queries;
            return this;
        }

        public ServerViewBuilder outputWriter(final OutputWriterView outputWriter) {
            outputWriters.add(outputWriter);
            return this;
        }
        
        public ServerView create() {
            return new ServerView(alias, host, port, username, password, runPeriodSeconds,
                    numQueryThreads, queries, outputWriters);
        }
    }

}

