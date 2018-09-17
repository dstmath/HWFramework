package org.apache.http.auth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.http.params.HttpParams;

@Deprecated
public final class AuthSchemeRegistry {
    private final Map<String, AuthSchemeFactory> registeredSchemes = new LinkedHashMap();

    public synchronized void register(String name, AuthSchemeFactory factory) {
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        } else if (factory == null) {
            throw new IllegalArgumentException("Authentication scheme factory may not be null");
        } else {
            this.registeredSchemes.put(name.toLowerCase(Locale.ENGLISH), factory);
        }
    }

    public synchronized void unregister(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.registeredSchemes.remove(name.toLowerCase(Locale.ENGLISH));
    }

    public synchronized AuthScheme getAuthScheme(String name, HttpParams params) throws IllegalStateException {
        AuthSchemeFactory factory;
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        factory = (AuthSchemeFactory) this.registeredSchemes.get(name.toLowerCase(Locale.ENGLISH));
        if (factory != null) {
        } else {
            throw new IllegalStateException("Unsupported authentication scheme: " + name);
        }
        return factory.newInstance(params);
    }

    public synchronized List<String> getSchemeNames() {
        return new ArrayList(this.registeredSchemes.keySet());
    }

    public synchronized void setItems(Map<String, AuthSchemeFactory> map) {
        if (map != null) {
            this.registeredSchemes.clear();
            this.registeredSchemes.putAll(map);
        }
    }
}
