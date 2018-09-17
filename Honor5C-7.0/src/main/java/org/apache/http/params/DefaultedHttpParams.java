package org.apache.http.params;

@Deprecated
public final class DefaultedHttpParams extends AbstractHttpParams {
    private final HttpParams defaults;
    private final HttpParams local;

    public DefaultedHttpParams(HttpParams local, HttpParams defaults) {
        if (local == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.local = local;
        this.defaults = defaults;
    }

    public HttpParams copy() {
        return new DefaultedHttpParams(this.local.copy(), this.defaults);
    }

    public Object getParameter(String name) {
        Object obj = this.local.getParameter(name);
        if (obj != null || this.defaults == null) {
            return obj;
        }
        return this.defaults.getParameter(name);
    }

    public boolean removeParameter(String name) {
        return this.local.removeParameter(name);
    }

    public HttpParams setParameter(String name, Object value) {
        return this.local.setParameter(name, value);
    }

    public HttpParams getDefaults() {
        return this.defaults;
    }
}
