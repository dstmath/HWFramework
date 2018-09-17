package org.apache.http.impl.client;

import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class DefaultProxyAuthenticationHandler extends AbstractAuthenticationHandler {
    public boolean isAuthenticationRequested(HttpResponse response, HttpContext context) {
        if (response != null) {
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED;
        } else {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
    }

    public Map<String, Header> getChallenges(HttpResponse response, HttpContext context) throws MalformedChallengeException {
        if (response != null) {
            return parseChallenges(response.getHeaders(AUTH.PROXY_AUTH));
        }
        throw new IllegalArgumentException("HTTP response may not be null");
    }
}
