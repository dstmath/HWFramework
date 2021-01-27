package org.apache.http.impl.client;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public class RedirectLocations {
    private final Set<URI> uris = new HashSet();

    public boolean contains(URI uri) {
        return this.uris.contains(uri);
    }

    public void add(URI uri) {
        this.uris.add(uri);
    }

    public boolean remove(URI uri) {
        return this.uris.remove(uri);
    }
}
