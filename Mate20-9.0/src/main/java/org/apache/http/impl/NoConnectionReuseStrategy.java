package org.apache.http.impl;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class NoConnectionReuseStrategy implements ConnectionReuseStrategy {
    public boolean keepAlive(HttpResponse response, HttpContext context) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        } else if (context != null) {
            return false;
        } else {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
    }
}
