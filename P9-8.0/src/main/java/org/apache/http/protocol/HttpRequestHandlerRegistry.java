package org.apache.http.protocol;

import java.util.Map;

@Deprecated
public class HttpRequestHandlerRegistry implements HttpRequestHandlerResolver {
    private final UriPatternMatcher matcher = new UriPatternMatcher();

    public void register(String pattern, HttpRequestHandler handler) {
        this.matcher.register(pattern, handler);
    }

    public void unregister(String pattern) {
        this.matcher.unregister(pattern);
    }

    public void setHandlers(Map map) {
        this.matcher.setHandlers(map);
    }

    public HttpRequestHandler lookup(String requestURI) {
        return (HttpRequestHandler) this.matcher.lookup(requestURI);
    }

    @Deprecated
    protected boolean matchUriRequestPattern(String pattern, String requestUri) {
        return this.matcher.matchUriRequestPattern(pattern, requestUri);
    }
}
