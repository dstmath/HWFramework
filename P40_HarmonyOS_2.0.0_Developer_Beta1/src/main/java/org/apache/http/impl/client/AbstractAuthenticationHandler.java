package org.apache.http.impl.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {
    private static final List<String> DEFAULT_SCHEME_PRIORITY = Arrays.asList("ntlm", "digest", "basic");
    private final Log log = LogFactory.getLog(getClass());

    /* access modifiers changed from: protected */
    public Map<String, Header> parseChallenges(Header[] headers) throws MalformedChallengeException {
        CharArrayBuffer buffer;
        int pos;
        Map<String, Header> map = new HashMap<>(headers.length);
        for (Header header : headers) {
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader) header).getBuffer();
                pos = ((FormattedHeader) header).getValuePos();
            } else {
                String s = header.getValue();
                if (s != null) {
                    buffer = new CharArrayBuffer(s.length());
                    buffer.append(s);
                    pos = 0;
                } else {
                    throw new MalformedChallengeException("Header value is null");
                }
            }
            while (pos < buffer.length() && HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            while (pos < buffer.length() && !HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            map.put(buffer.substring(pos, pos).toLowerCase(Locale.ENGLISH), header);
        }
        return map;
    }

    /* access modifiers changed from: protected */
    public List<String> getAuthPreferences() {
        return DEFAULT_SCHEME_PRIORITY;
    }

    @Override // org.apache.http.client.AuthenticationHandler
    public AuthScheme selectScheme(Map<String, Header> challenges, HttpResponse response, HttpContext context) throws AuthenticationException {
        AuthSchemeRegistry registry = (AuthSchemeRegistry) context.getAttribute(ClientContext.AUTHSCHEME_REGISTRY);
        if (registry != null) {
            List<?> authPrefs = (List) context.getAttribute(ClientContext.AUTH_SCHEME_PREF);
            if (authPrefs == null) {
                authPrefs = getAuthPreferences();
            }
            if (this.log.isDebugEnabled()) {
                Log log2 = this.log;
                log2.debug("Authentication schemes in the order of preference: " + authPrefs);
            }
            AuthScheme authScheme = null;
            int i = 0;
            while (true) {
                if (i >= authPrefs.size()) {
                    break;
                }
                String id = authPrefs.get(i);
                if (challenges.get(id.toLowerCase(Locale.ENGLISH)) != null) {
                    if (this.log.isDebugEnabled()) {
                        Log log3 = this.log;
                        log3.debug(id + " authentication scheme selected");
                    }
                    try {
                        authScheme = registry.getAuthScheme(id, response.getParams());
                        break;
                    } catch (IllegalStateException e) {
                        if (this.log.isWarnEnabled()) {
                            Log log4 = this.log;
                            log4.warn("Authentication scheme " + id + " not supported");
                        }
                    }
                } else {
                    if (this.log.isDebugEnabled()) {
                        Log log5 = this.log;
                        log5.debug("Challenge for " + id + " authentication scheme not available");
                    }
                    i++;
                }
            }
            if (authScheme != null) {
                return authScheme;
            }
            throw new AuthenticationException("Unable to respond to any of these challenges: " + challenges);
        }
        throw new IllegalStateException("AuthScheme registry not set in HTTP context");
    }
}
