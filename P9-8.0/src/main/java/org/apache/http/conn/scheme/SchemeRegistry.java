package org.apache.http.conn.scheme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;

@Deprecated
public final class SchemeRegistry {
    private final Map<String, Scheme> registeredSchemes = new LinkedHashMap();

    public final synchronized Scheme getScheme(String name) {
        Scheme found;
        found = get(name);
        if (found == null) {
            throw new IllegalStateException("Scheme '" + name + "' not registered.");
        }
        return found;
    }

    public final synchronized Scheme getScheme(HttpHost host) {
        if (host == null) {
            throw new IllegalArgumentException("Host must not be null.");
        }
        return getScheme(host.getSchemeName());
    }

    public final synchronized Scheme get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null.");
        }
        return (Scheme) this.registeredSchemes.get(name);
    }

    public final synchronized Scheme register(Scheme sch) {
        if (sch == null) {
            throw new IllegalArgumentException("Scheme must not be null.");
        }
        return (Scheme) this.registeredSchemes.put(sch.getName(), sch);
    }

    public final synchronized Scheme unregister(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null.");
        }
        return (Scheme) this.registeredSchemes.remove(name);
    }

    public final synchronized List<String> getSchemeNames() {
        return new ArrayList(this.registeredSchemes.keySet());
    }

    public synchronized void setItems(Map<String, Scheme> map) {
        if (map != null) {
            this.registeredSchemes.clear();
            this.registeredSchemes.putAll(map);
        }
    }
}
