package java.security;

import java.net.URI;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.ProtectionParameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DomainLoadStoreParameter implements LoadStoreParameter {
    private final URI configuration;
    private final Map<String, ProtectionParameter> protectionParams;

    public DomainLoadStoreParameter(URI configuration, Map<String, ProtectionParameter> protectionParams) {
        if (configuration == null || protectionParams == null) {
            throw new NullPointerException("invalid null input");
        }
        this.configuration = configuration;
        this.protectionParams = Collections.unmodifiableMap(new HashMap((Map) protectionParams));
    }

    public URI getConfiguration() {
        return this.configuration;
    }

    public Map<String, ProtectionParameter> getProtectionParams() {
        return this.protectionParams;
    }

    public ProtectionParameter getProtectionParameter() {
        return null;
    }
}
