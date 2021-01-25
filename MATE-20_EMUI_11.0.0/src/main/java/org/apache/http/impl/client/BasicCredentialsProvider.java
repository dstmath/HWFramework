package org.apache.http.impl.client;

import java.util.HashMap;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

@Deprecated
public class BasicCredentialsProvider implements CredentialsProvider {
    private final HashMap<AuthScope, Credentials> credMap = new HashMap<>();

    @Override // org.apache.http.client.CredentialsProvider
    public synchronized void setCredentials(AuthScope authscope, Credentials credentials) {
        if (authscope != null) {
            this.credMap.put(authscope, credentials);
        } else {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }
    }

    private static Credentials matchCredentials(HashMap<AuthScope, Credentials> map, AuthScope authscope) {
        Credentials creds = map.get(authscope);
        if (creds != null) {
            return creds;
        }
        int bestMatchFactor = -1;
        AuthScope bestMatch = null;
        for (AuthScope current : map.keySet()) {
            int factor = authscope.match(current);
            if (factor > bestMatchFactor) {
                bestMatchFactor = factor;
                bestMatch = current;
            }
        }
        if (bestMatch != null) {
            return map.get(bestMatch);
        }
        return creds;
    }

    @Override // org.apache.http.client.CredentialsProvider
    public synchronized Credentials getCredentials(AuthScope authscope) {
        if (authscope != null) {
        } else {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }
        return matchCredentials(this.credMap, authscope);
    }

    public String toString() {
        return this.credMap.toString();
    }

    @Override // org.apache.http.client.CredentialsProvider
    public synchronized void clear() {
        this.credMap.clear();
    }
}
