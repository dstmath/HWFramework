package org.apache.http.impl.client;

import java.util.HashMap;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

@Deprecated
public class BasicCredentialsProvider implements CredentialsProvider {
    private final HashMap<AuthScope, Credentials> credMap = new HashMap();

    public synchronized void setCredentials(AuthScope authscope, Credentials credentials) {
        if (authscope == null) {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }
        this.credMap.put(authscope, credentials);
    }

    private static Credentials matchCredentials(HashMap<AuthScope, Credentials> map, AuthScope authscope) {
        Credentials creds = (Credentials) map.get(authscope);
        if (creds != null) {
            return creds;
        }
        int bestMatchFactor = -1;
        Object bestMatch = null;
        for (AuthScope current : map.keySet()) {
            int factor = authscope.match(current);
            if (factor > bestMatchFactor) {
                bestMatchFactor = factor;
                bestMatch = current;
            }
        }
        if (bestMatch != null) {
            return (Credentials) map.get(bestMatch);
        }
        return creds;
    }

    public synchronized Credentials getCredentials(AuthScope authscope) {
        if (authscope == null) {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }
        return matchCredentials(this.credMap, authscope);
    }

    public String toString() {
        return this.credMap.toString();
    }

    public synchronized void clear() {
        this.credMap.clear();
    }
}
