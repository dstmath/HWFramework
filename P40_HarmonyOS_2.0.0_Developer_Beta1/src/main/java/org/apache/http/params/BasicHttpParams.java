package org.apache.http.params;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public final class BasicHttpParams extends AbstractHttpParams implements Serializable, Cloneable {
    private static final long serialVersionUID = -7086398485908701455L;
    private HashMap parameters;

    @Override // org.apache.http.params.HttpParams
    public Object getParameter(String name) {
        HashMap hashMap = this.parameters;
        if (hashMap != null) {
            return hashMap.get(name);
        }
        return null;
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams setParameter(String name, Object value) {
        if (this.parameters == null) {
            this.parameters = new HashMap();
        }
        this.parameters.put(name, value);
        return this;
    }

    @Override // org.apache.http.params.HttpParams
    public boolean removeParameter(String name) {
        HashMap hashMap = this.parameters;
        if (hashMap == null || !hashMap.containsKey(name)) {
            return false;
        }
        this.parameters.remove(name);
        return true;
    }

    public void setParameters(String[] names, Object value) {
        for (String str : names) {
            setParameter(str, value);
        }
    }

    public boolean isParameterSet(String name) {
        return getParameter(name) != null;
    }

    public boolean isParameterSetLocally(String name) {
        HashMap hashMap = this.parameters;
        return (hashMap == null || hashMap.get(name) == null) ? false : true;
    }

    public void clear() {
        this.parameters = null;
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams copy() {
        BasicHttpParams clone = new BasicHttpParams();
        copyParams(clone);
        return clone;
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        BasicHttpParams clone = (BasicHttpParams) super.clone();
        copyParams(clone);
        return clone;
    }

    /* access modifiers changed from: protected */
    public void copyParams(HttpParams target) {
        HashMap hashMap = this.parameters;
        if (hashMap != null) {
            for (Map.Entry me : hashMap.entrySet()) {
                if (me.getKey() instanceof String) {
                    target.setParameter((String) me.getKey(), me.getValue());
                }
            }
        }
    }
}
