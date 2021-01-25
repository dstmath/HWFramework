package org.apache.http.protocol;

@Deprecated
public final class DefaultedHttpContext implements HttpContext {
    private final HttpContext defaults;
    private final HttpContext local;

    public DefaultedHttpContext(HttpContext local2, HttpContext defaults2) {
        if (local2 != null) {
            this.local = local2;
            this.defaults = defaults2;
            return;
        }
        throw new IllegalArgumentException("HTTP context may not be null");
    }

    @Override // org.apache.http.protocol.HttpContext
    public Object getAttribute(String id) {
        Object obj = this.local.getAttribute(id);
        if (obj == null) {
            return this.defaults.getAttribute(id);
        }
        return obj;
    }

    @Override // org.apache.http.protocol.HttpContext
    public Object removeAttribute(String id) {
        return this.local.removeAttribute(id);
    }

    @Override // org.apache.http.protocol.HttpContext
    public void setAttribute(String id, Object obj) {
        this.local.setAttribute(id, obj);
    }

    public HttpContext getDefaults() {
        return this.defaults;
    }
}
