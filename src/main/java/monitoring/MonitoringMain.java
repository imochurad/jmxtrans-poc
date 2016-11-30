package monitoring;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.googlecode.jmxtrans.JmxTransformer;
import com.googlecode.jmxtrans.cli.JmxTransConfiguration;
import com.googlecode.jmxtrans.guice.JmxTransModule;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import monitoring.config.CassandraJmxMetrics;
import monitoring.config.JmxConnectionInfo;
import monitoring.config.JmxMetrics;
import monitoring.jmxtrans.config.serialization.OutputWriterView;
import monitoring.jmxtrans.config.serialization.QueryView;
import monitoring.jmxtrans.config.serialization.ServerView;
import monitoring.jmxtrans.config.serialization.ServerViewsWrapper;
import monitoring.jmxtrans.config.serialization.SimpleGraphiteWriterView;
import monitoring.jmxtrans.config.serialization.QueryView.QueryViewBuilder;
import monitoring.jmxtrans.config.serialization.ServerView.ServerViewBuilder;
import monitoring.util.HerokuAppConfigReader;
import monitoring.util.HerokuJmxUrlParser;
import monitoring.util.TrustStoreHandler;

public class MonitoringMain {

    // @formatter:off
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringMain.class);

    private static final String JMXTRANS_CONFIG_FILE = "/tmp/jmxtrans_servers_config.json";
    private static final String HOSTED_GRAPHITE_API_KEY = System.getenv("HOSTEDGRAPHITE_APIKEY") != null ? System.getenv("HOSTEDGRAPHITE_APIKEY") : "bla-blah";
    private static final String HOSTED_GRAPHITE_HOST = "carbon.hostedgraphite.com";
    // private static final String HOSTED_GRAPHITE_HOST = "localhost";
    private static final Integer HOSTED_GRPAHITE_PORT = 2003;
    // @formatter:on

    // Params for GraphiteWriterFactory setup, do not remove!

    // legitimate values are: never, always, timeBased (requires flushDelayInSeconds set)
    // private static final String GRAPHITE_WTITER_FACTORY_FLUSH_STRATEGY = "timeBased";
    // private static final Integer GRAPHITE_WRITER_FACTORY_FLUSH_DELAY_IN_SECONDS = 2;
    // private static final Integer GRAPHITE_WRITER_FACTORY_POOL_SIZE = 10;

    private static final OutputWriterView OUTPUT_WRITER = initOutputWriter();

    private static final String APPS = System.getenv("APPS_WITH_REMOTE_JMX_SERVICES");

    private static OutputWriterView initOutputWriter() {
        return new SimpleGraphiteWriterView(HOSTED_GRAPHITE_API_KEY, HOSTED_GRAPHITE_HOST,
                HOSTED_GRPAHITE_PORT);
    }

    // @formatter:off
    private static List<ServerView> getJMXServersFromAppsConfigVars() {
        List<ServerView> servers = Lists.newArrayList();

        LOG.debug("Apps with remote JMX services = {}", APPS);

        // in local environment APPS won't get set
        if (APPS == null)
            servers.addAll(createServerView(new JmxConnectionInfo("localhost", 7199, null, null), CassandraJmxMetrics.values()));
        else {
            servers.addAll(getCassandraServers());
            // TODO add Kafka servers here
        }
        return servers;
    }
    // @formatter:on

    // @formatter:off
    private static List<ServerView> getCassandraServers() {
        Set<String> cassandraJmxUrls = HerokuAppConfigReader.instance().readCassandraJmxUrls(APPS.split(","));
        LOG.debug("CASSANDRA JMX URLS = {}", cassandraJmxUrls);
        
        Collection<JmxConnectionInfo> cassandraJmxConnections = HerokuJmxUrlParser.instance().parse(CassandraJmxMetrics.protocol(), 
                cassandraJmxUrls.toArray(new String[0]));
        List<ServerView> servers = cassandraJmxConnections.stream()
                .map(jmxConnection -> createServerView(jmxConnection, CassandraJmxMetrics.values()))
                .flatMap(list -> list.stream())
                .collect(toList());
        return servers;
    }
    // @formatter:on

    // @formatter:off
    private static List<ServerView> createServerView(JmxConnectionInfo jmxConnectionInfo, JmxMetrics[] jmxMetrics)
    {
        List<ServerView> servers = Lists.newArrayList();
        // partitioning jmx metrics based on metrics refresh frequency 
        Map<Integer, List<JmxMetrics>> byFrequency = Arrays.asList(jmxMetrics).stream()
                .collect(groupingBy(JmxMetrics::frequency));
        // Currently there is no way to set refresh rate on an individual Query, refresh rate is being set on a Server object only.
        // Therefore, we need to create several server objects having different refresh frequencies pointing at the same JMX servers.
        // Read more here: https://github.com/jmxtrans/jmxtrans/issues/522
        
        for(Integer frequency : byFrequency.keySet())
        {
            List<JmxMetrics> metrics = byFrequency.get(frequency);
            List<QueryView> queries = metrics.stream()
                    .map(metric -> createQueryView(metric))
                    .collect(toList());
            
            servers.add(new ServerViewBuilder(jmxConnectionInfo)
                    .queries(queries)
                    .outputWriter(OUTPUT_WRITER)
                    .numQueryThreads(metrics.size())
                    .runPeriodSeconds(frequency)
                    .create());
        }
        return servers;
    }
    // @formatter:on

    private static QueryView createQueryView(JmxMetrics jmxMetric) {
        return new QueryViewBuilder(jmxMetric).create();
    }

    // @formatter:off
    public static void main(String[] args) throws Exception {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
        
        LOG.info("==========================---------Heroku Monitoring with JMXTRANS----------===================================");
        
        importCertificates();
        
        Collection<ServerView> serverViews = getJMXServersFromAppsConfigVars();
        
        createJmxtransConfigFile(serverViews);

        JmxTransformer transformer = createTransformer();

        transformer.start();
    }
    // @formatter:on

    private static void importCertificates() {
        Set<String> certificates =
                HerokuAppConfigReader.instance().read("CASSANDRA_TRUSTED_CERT", APPS.split(","));
        certificates.stream().forEach(cert -> TrustStoreHandler.importCert(cert, "some_alias"));
    }

    private static JmxTransformer createTransformer() {
        File f = new File(JMXTRANS_CONFIG_FILE);
        JmxTransConfiguration config = new JmxTransConfiguration();
        config.setJsonFile(f);
        Injector injector = JmxTransModule.createInjector(config);
        JmxTransformer transformer = injector.getInstance(JmxTransformer.class);
        return transformer;
    }

    private static void createJmxtransConfigFile(Collection<ServerView> serverViews)
            throws JsonProcessingException, IOException, JsonGenerationException,
            JsonMappingException {
        ServerViewsWrapper serverViewsWrapper = new ServerViewsWrapper(serverViews);
        ObjectMapper mapper = new ObjectMapper();
        LOG.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(serverViewsWrapper));
        mapper.writeValue(new File(JMXTRANS_CONFIG_FILE), serverViewsWrapper);
    }

}
