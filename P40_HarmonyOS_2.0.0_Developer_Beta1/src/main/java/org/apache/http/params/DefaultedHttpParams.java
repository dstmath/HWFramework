package org.apache.http.params;

@Deprecated
public final class DefaultedHttpParams extends AbstractHttpParams {
    private final HttpParams defaults;
    private final HttpParams local;

    public DefaultedHttpParams(HttpParams local2, HttpParams defaults2) {
        if (local2 != null) {
            this.local = local2;
            this.defaults = defaults2;
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams copy() {
        return new DefaultedHttpParams(this.local.copy(), this.defaults);
    }

    @Override // org.apache.http.params.HttpParams
    public Object getParameter(String name) {
        HttpParams httpParams;
        Object obj = this.local.getParameter(name);
        if (obj != null || (httpParams = this.defaults) == null) {
            return obj;
        }
        return httpParams.getParameter(name);
    }

    @Override // org.apache.http.params.HttpParams
    public boolean removeParameter(String name) {
        return this.local.removeParameter(name);
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams setParameter(String name, Object value) {
        return this.local.setParameter(name, value);
    }

    public HttpParams getDefaults() {
        return this.defaults;
    }
}
