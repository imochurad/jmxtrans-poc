package monitoring.jmxtrans.config.serialization;

import java.util.Collection;

import lombok.Getter;

public class ServerViewsWrapper {

    @Getter
    private final Collection<ServerView> servers;

    public ServerViewsWrapper(Collection<ServerView> servers) {
        this.servers = servers;
    }
    
}
