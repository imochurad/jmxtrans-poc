package monitoring.util;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import monitoring.config.JmxConnectionInfo;

public class HerokuJmxUrlParser {

    private static final HerokuJmxUrlParser INSTANCE = new HerokuJmxUrlParser();

    private HerokuJmxUrlParser() {
        
    }
    
    public Collection<JmxConnectionInfo> parse(String protocol, String... jmxUrls)
    {
        return Arrays.asList(jmxUrls).
                stream().
                map(jmxUrl -> parse(protocol, jmxUrl)).
                flatMap(c -> c.stream()).
                collect(toList());
    }

    public Collection<JmxConnectionInfo> parse(String protocol, String jmxUrl) {
        Preconditions.checkNotNull(jmxUrl);
        Preconditions.checkNotNull(protocol);
        return Arrays.asList(jmxUrl.split(",")).stream().map(url -> fromURL(url, protocol))
                .collect(toList());
    }
    
    

    private JmxConnectionInfo fromURL(String url, String protocol) {
        int userBeginIndex = protocol.length() + "://".length();
        int userEndIndex = url.indexOf(":", userBeginIndex);
        if (userEndIndex > userBeginIndex) {
            String user = url.substring(userBeginIndex, userEndIndex);

            int passwordBeginIndex = userEndIndex + 1;
            int passwordEndIndex = url.indexOf("@", passwordBeginIndex);

            if (passwordEndIndex > passwordBeginIndex) {

                String password = url.substring(passwordBeginIndex, passwordEndIndex);

                int hostBeginIndex = passwordEndIndex + 1;
                int hostEndIndex = url.indexOf(":", hostBeginIndex);

                if (hostEndIndex > hostBeginIndex) {
                    String host = url.substring(hostBeginIndex, hostEndIndex);

                    if (hostEndIndex + 1 < url.length()) {
                        Integer port = Ints.tryParse(url.substring(hostEndIndex + 1));
                        if (port != null)
                            return new JmxConnectionInfo(host, port, user, password);
                    }
                }
            }
        }
        return null;
    }

    public static HerokuJmxUrlParser instance() {
        return INSTANCE;
    }

}
