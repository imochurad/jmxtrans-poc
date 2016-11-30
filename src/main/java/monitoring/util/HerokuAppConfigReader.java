package monitoring.util;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import monitoring.config.CassandraJmxMetrics;

public class HerokuAppConfigReader implements AutoCloseable {

    // @formatter:off
    // Heroku API Authorization header allows basic authorization by passing value Basic <base64-encoded email + \":\" + password>
    // The value below is base64-encoded value of *login*:*password*
    private static final String BASIC_AUTH_TOKEN = "token";
    // Another way of authorization would be through web application authorization, application has to be converted into web application as described here:
    // https://devcenter.heroku.com/articles/oauth#web-application-authorization
    private static final String CONFIG_VARS_ENDPOINT_URL_FORMAT = "https://api.heroku.com/apps/%s/config-vars";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.heroku+json; version=3";
    private static final String AUTHORIZATION_HEADER_VALUE_FORMAT = "Basic %s";
    // @formatter:on
    
    private static final Logger LOG = LoggerFactory.getLogger(HerokuAppConfigReader.class);

    private final CloseableHttpClient httpClient;
    
    private static final HerokuAppConfigReader INSTANCE = new HerokuAppConfigReader();

    private HerokuAppConfigReader() {
        httpClient = HttpClients.createDefault();
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        
        Set<String> value = new HerokuAppConfigReader().readCassandraJmxUrls("app_name");
        System.out.println(value);
    }
    
    public Set<String> readCassandraJmxUrls(String... apps){
        return Arrays.asList(apps).stream()
                .map(app -> read(app, CassandraJmxMetrics.var()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }
    
    public Set<String> read(String key, String... apps){
        return Arrays.asList(apps).stream()
                .map(app -> read(app, key))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    public Optional<String> read(String app, String key) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(key);
        
        HttpGet httpGet = new HttpGet(String.format(CONFIG_VARS_ENDPOINT_URL_FORMAT, app));
        httpGet.addHeader(HttpHeaders.ACCEPT, ACCEPT_HEADER_VALUE);
        httpGet.addHeader(HttpHeaders.AUTHORIZATION, String.format(AUTHORIZATION_HEADER_VALUE_FORMAT, BASIC_AUTH_TOKEN));
        // TODO deal properly with auto closeable
        try
        {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            try
            {
                HttpEntity entity = response.getEntity();
                if (entity != null) 
                {
                    String responseAsText = EntityUtils.toString(entity);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode =  mapper.readValue(responseAsText, JsonNode.class);
                    JsonNode value = rootNode.get(key);
                    return value.isTextual() ? Optional.of(value.asText()) : Optional.empty(); 
                }
            }
            // TODO exception handling
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                response.close();
            }
        }
        catch(IOException e)
        {
            LOG.error("Unable to list config vars for application[" + app + "]", e);
        }
        return Optional.empty();
    }

    public static HerokuAppConfigReader instance()
    {
        return INSTANCE;
    }

    @Override
    public void close() throws Exception {
        httpClient.close();
    }

}
