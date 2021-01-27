package org.apache.http.protocol;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class BasicHttpContext implements HttpContext {
    private Map map;
    private final HttpContext parentContext;

    public BasicHttpContext() {
        this(null);
    }

    public BasicHttpContext(HttpContext parentContext2) {
        this.map = null;
        this.parentContext = parentContext2;
    }

    @Override // org.apache.http.protocol.HttpContext
    public Object getAttribute(String id) {
        HttpContext httpContext;
        if (id != null) {
            Object obj = null;
            Map map2 = this.map;
            if (map2 != null) {
                obj = map2.get(id);
            }
            if (obj != null || (httpContext = this.parentContext) == null) {
                return obj;
            }
            return httpContext.getAttribute(id);
        }
        throw new IllegalArgumentException("Id may not be null");
    }

    @Override // org.apache.http.protocol.HttpContext
    public void setAttribute(String id, Object obj) {
        if (id != null) {
            if (this.map == null) {
                this.map = new HashMap();
            }
            this.map.put(id, obj);
            return;
        }
        throw new IllegalArgumentException("Id may not be null");
    }

    @Override // org.apache.http.protocol.HttpContext
    public Object removeAttribute(String id) {
        if (id != null) {
            Map map2 = this.map;
            if (map2 != null) {
                return map2.remove(id);
            }
            return null;
        }
        throw new IllegalArgumentException("Id may not be null");
    }
}
