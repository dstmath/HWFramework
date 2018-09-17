package org.apache.http.impl.client;

import android.net.http.Headers;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class DefaultRedirectHandler implements RedirectHandler {
    private static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";
    private final Log log = LogFactory.getLog(getClass());

    public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        switch (response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_MOVED_PERMANENTLY /*301*/:
            case HttpStatus.SC_MOVED_TEMPORARILY /*302*/:
            case HttpStatus.SC_SEE_OTHER /*303*/:
            case HttpStatus.SC_TEMPORARY_REDIRECT /*307*/:
                return true;
            default:
                return false;
        }
    }

    public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        Header locationHeader = response.getFirstHeader(Headers.LOCATION);
        if (locationHeader == null) {
            throw new ProtocolException("Received redirect response " + response.getStatusLine() + " but no location header");
        }
        String location = locationHeader.getValue();
        if (this.log.isDebugEnabled()) {
            this.log.debug("Redirect requested to location '" + location + "'");
        }
        try {
            URI uri = new URI(location);
            HttpParams params = response.getParams();
            if (!uri.isAbsolute()) {
                if (params.isParameterTrue(ClientPNames.REJECT_RELATIVE_REDIRECT)) {
                    throw new ProtocolException("Relative redirect location '" + uri + "' not allowed");
                }
                HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (target == null) {
                    throw new IllegalStateException("Target host not available in the HTTP context");
                }
                try {
                    uri = URIUtils.resolve(URIUtils.rewriteURI(new URI(((HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST)).getRequestLine().getUri()), target, true), uri);
                } catch (URISyntaxException ex) {
                    throw new ProtocolException(ex.getMessage(), ex);
                }
            }
            if (params.isParameterFalse(ClientPNames.ALLOW_CIRCULAR_REDIRECTS)) {
                URI redirectURI;
                RedirectLocations redirectLocations = (RedirectLocations) context.getAttribute(REDIRECT_LOCATIONS);
                if (redirectLocations == null) {
                    redirectLocations = new RedirectLocations();
                    context.setAttribute(REDIRECT_LOCATIONS, redirectLocations);
                }
                if (uri.getFragment() != null) {
                    try {
                        redirectURI = URIUtils.rewriteURI(uri, new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), true);
                    } catch (URISyntaxException ex2) {
                        throw new ProtocolException(ex2.getMessage(), ex2);
                    }
                }
                redirectURI = uri;
                if (redirectLocations.contains(redirectURI)) {
                    throw new CircularRedirectException("Circular redirect to '" + redirectURI + "'");
                }
                redirectLocations.add(redirectURI);
            }
            return uri;
        } catch (URISyntaxException ex22) {
            throw new ProtocolException("Invalid redirect URI: " + location, ex22);
        }
    }
}
