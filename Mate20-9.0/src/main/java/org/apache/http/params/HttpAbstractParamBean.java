package org.apache.http.params;

@Deprecated
public abstract class HttpAbstractParamBean {
    protected final HttpParams params;

    public HttpAbstractParamBean(HttpParams params2) {
        if (params2 != null) {
            this.params = params2;
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }
}
