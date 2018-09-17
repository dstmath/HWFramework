package org.apache.http.client.protocol;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.ProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class RequestAddCookies implements HttpRequestInterceptor {
    private final Log log = LogFactory.getLog(getClass());

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else {
            CookieStore cookieStore = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
            if (cookieStore == null) {
                this.log.info("Cookie store not available in HTTP context");
                return;
            }
            CookieSpecRegistry registry = (CookieSpecRegistry) context.getAttribute(ClientContext.COOKIESPEC_REGISTRY);
            if (registry == null) {
                this.log.info("CookieSpec registry not available in HTTP context");
                return;
            }
            HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            if (targetHost == null) {
                throw new IllegalStateException("Target host not specified in HTTP context");
            }
            ManagedClientConnection conn = (ManagedClientConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
            if (conn == null) {
                throw new IllegalStateException("Client connection not specified in HTTP context");
            }
            URI requestURI;
            Header header;
            String policy = HttpClientParams.getCookiePolicy(request.getParams());
            if (this.log.isDebugEnabled()) {
                this.log.debug("CookieSpec selected: " + policy);
            }
            if (request instanceof HttpUriRequest) {
                requestURI = ((HttpUriRequest) request).getURI();
            } else {
                try {
                    URI uri = new URI(request.getRequestLine().getUri());
                } catch (URISyntaxException ex) {
                    throw new ProtocolException("Invalid request URI: " + request.getRequestLine().getUri(), ex);
                }
            }
            String hostName = targetHost.getHostName();
            int port = targetHost.getPort();
            if (port < 0) {
                port = conn.getRemotePort();
            }
            CookieOrigin cookieOrigin = new CookieOrigin(hostName, port, requestURI.getPath(), conn.isSecure());
            CookieSpec cookieSpec = registry.getCookieSpec(policy, request.getParams());
            List<Cookie> cookies = new ArrayList(cookieStore.getCookies());
            List<Cookie> matchedCookies = new ArrayList();
            for (Cookie cookie : cookies) {
                if (cookieSpec.match(cookie, cookieOrigin)) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Cookie " + cookie + " match " + cookieOrigin);
                    }
                    matchedCookies.add(cookie);
                }
            }
            if (!matchedCookies.isEmpty()) {
                for (Header header2 : cookieSpec.formatCookies(matchedCookies)) {
                    request.addHeader(header2);
                }
            }
            int ver = cookieSpec.getVersion();
            if (ver > 0) {
                boolean needVersionHeader = false;
                for (Cookie cookie2 : matchedCookies) {
                    if (ver != cookie2.getVersion()) {
                        needVersionHeader = true;
                    }
                }
                if (needVersionHeader) {
                    header2 = cookieSpec.getVersionHeader();
                    if (header2 != null) {
                        request.addHeader(header2);
                    }
                }
            }
            context.setAttribute(ClientContext.COOKIE_SPEC, cookieSpec);
            context.setAttribute(ClientContext.COOKIE_ORIGIN, cookieOrigin);
        }
    }
}
