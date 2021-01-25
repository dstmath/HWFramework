package org.apache.http.params;

@Deprecated
public abstract class AbstractHttpParams implements HttpParams {
    protected AbstractHttpParams() {
    }

    @Override // org.apache.http.params.HttpParams
    public long getLongParameter(String name, long defaultValue) {
        Object param = getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return ((Long) param).longValue();
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams setLongParameter(String name, long value) {
        setParameter(name, new Long(value));
        return this;
    }

    @Override // org.apache.http.params.HttpParams
    public int getIntParameter(String name, int defaultValue) {
        Object param = getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return ((Integer) param).intValue();
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams setIntParameter(String name, int value) {
        setParameter(name, new Integer(value));
        return this;
    }

    @Override // org.apache.http.params.HttpParams
    public double getDoubleParameter(String name, double defaultValue) {
        Object param = getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return ((Double) param).doubleValue();
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams setDoubleParameter(String name, double value) {
        setParameter(name, new Double(value));
        return this;
    }

    @Override // org.apache.http.params.HttpParams
    public boolean getBooleanParameter(String name, boolean defaultValue) {
        Object param = getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return ((Boolean) param).booleanValue();
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams setBooleanParameter(String name, boolean value) {
        setParameter(name, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    @Override // org.apache.http.params.HttpParams
    public boolean isParameterTrue(String name) {
        return getBooleanParameter(name, false);
    }

    @Override // org.apache.http.params.HttpParams
    public boolean isParameterFalse(String name) {
        return !getBooleanParameter(name, false);
    }
}
