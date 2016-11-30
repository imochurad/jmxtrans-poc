package monitoring.jmxtrans.config.serialization;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Strings;
import com.googlecode.jmxtrans.model.Query;

import lombok.Getter;
import monitoring.config.JmxMetrics;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(value = {"obj", "attr", "typeNames", "resultAlias", "keys", "allowDottedKeys",
        "useAllTypeNames", "outputWriters"})

public class QueryView {

    @Nonnull
    @Getter
    private final String obj;
    @Nonnull
    @Getter
    private final List<String> keys;
    @Nonnull
    @Getter
    private final List<String> attr;
    @Getter
    private final Set<String> typeNames;
    @Getter
    private final String resultAlias;
    @Getter
    private final boolean useObjDomainAsKey;
    @Getter
    private final boolean allowDottedKeys;
    @Getter
    private final boolean useAllTypeNames;

    private QueryView(String obj, List<String> keys, List<String> attr, Set<String> typeNames,
            String resultAlias, boolean useObjDomainAsKey, boolean allowDottedKeys,
            boolean useAllTypeNames) {
        this.obj = obj;
        this.keys = keys;
        this.attr = attr;
        this.typeNames = typeNames;
        this.resultAlias = resultAlias;
        this.useObjDomainAsKey = useObjDomainAsKey;
        this.allowDottedKeys = allowDottedKeys;
        this.useAllTypeNames = useAllTypeNames;
    }

    static final Function<Query, QueryView> TO_VIEW = new Function<Query, QueryView>() {

        @Override
        public QueryView apply(Query query) {
            return new QueryView(query.getObjectName().toString(), query.getKeys(), query.getAttr(),
                    query.getTypeNames(), query.getResultAlias(), query.isUseObjDomainAsKey(),
                    query.isAllowDottedKeys(), query.isUseAllTypeNames());
        }
    };

    public static class QueryViewBuilder {
        private String obj;
        private List<String> keys;
        private List<String> attr;
        private Set<String> typeNames;
        private String resultAlias;
        private boolean useObjDomainAsKey;
        private boolean allowDottedKeys;
        private boolean useAllTypeNames;

        public QueryViewBuilder(final JmxMetrics jmxMetrics) {
            this(jmxMetrics.jmxPath(), Collections.singletonList(jmxMetrics.attribute()));
            if(!Strings.isNullOrEmpty(jmxMetrics.alias()))
                resultAlias(jmxMetrics.alias());
        }

        public QueryViewBuilder(final String obj, final List<String> attr) {
            this.obj = obj;
            this.attr = attr;
        }

        public QueryViewBuilder keys(final List<String> keys) {
            this.keys = keys;
            return this;
        }

        public QueryViewBuilder attr(final List<String> attr) {
            this.attr = attr;
            return this;
        }

        public QueryViewBuilder typeNames(final Set<String> typeNames) {
            this.typeNames = typeNames;
            return this;
        }

        public QueryViewBuilder resultAlias(final String resultAlias) {
            this.resultAlias = resultAlias;
            return this;
        }

        public QueryViewBuilder useObjDomainAsKey(final boolean useObjDomainAsKey) {
            this.useObjDomainAsKey = useObjDomainAsKey;
            return this;
        }

        public QueryViewBuilder allowDottedKeys(final boolean allowDottedKeys) {
            this.allowDottedKeys = allowDottedKeys;
            return this;
        }

        public QueryViewBuilder useAllTypeNames(final boolean useAllTypeNames) {
            this.useAllTypeNames = useAllTypeNames;
            return this;
        }

        public QueryView create() {
            return new QueryView(obj, keys, attr, typeNames, resultAlias, useObjDomainAsKey,
                    allowDottedKeys, useAllTypeNames);
        }
    }

}
