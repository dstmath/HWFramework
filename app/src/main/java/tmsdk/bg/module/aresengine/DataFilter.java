package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdkobf.in;

/* compiled from: Unknown */
public abstract class DataFilter<T extends TelephonyEntity> extends in<T> {
    private DataHandler ss;
    private Object wO;
    private FilterConfig wP;

    public DataFilter() {
        this.wO = new Object();
    }

    protected abstract FilterResult a(T t, Object... objArr);

    protected void a(DataHandler dataHandler) {
        synchronized (this.wO) {
            this.ss = dataHandler;
        }
    }

    protected void a(T t, FilterResult filterResult, Object... objArr) {
    }

    protected void b(T t, Object... objArr) {
    }

    public abstract FilterConfig defalutFilterConfig();

    public final FilterResult filter(T t, Object... objArr) {
        b(t, objArr);
        FilterResult a = a(t, objArr);
        a(t, a, objArr);
        synchronized (this.wO) {
            if (this.ss != null) {
                this.ss.sendMessage(a);
            }
        }
        return a;
    }

    public final synchronized FilterConfig getConfig() {
        return this.wP;
    }

    public final synchronized void setConfig(FilterConfig filterConfig) {
        if (filterConfig != null) {
            this.wP = filterConfig;
        } else {
            throw new NullPointerException("the filter's config can not be null");
        }
    }

    protected void unbind() {
        synchronized (this.wO) {
            this.ss = null;
        }
    }
}
