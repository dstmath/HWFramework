package java.security;

import java.net.URI;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DomainLoadStoreParameter implements KeyStore.LoadStoreParameter {
    private final URI configuration;
    private final Map<String, KeyStore.ProtectionParameter> protectionParams;

    public DomainLoadStoreParameter(URI configuration2, Map<String, KeyStore.ProtectionParameter> protectionParams2) {
        if (configuration2 == null || protectionParams2 == null) {
            throw new NullPointerException("invalid null input");
        }
        this.configuration = configuration2;
        this.protectionParams = Collections.unmodifiableMap(new HashMap(protectionParams2));
    }

    public URI getConfiguration() {
        return this.configuration;
    }

    public Map<String, KeyStore.ProtectionParameter> getProtectionParams() {
        return this.protectionParams;
    }

    public KeyStore.ProtectionParameter getProtectionParameter() {
        return null;
    }
}
