package tmsdkobf;

import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptor;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.common.module.aresengine.TelephonyEntity;

/* compiled from: Unknown */
public final class ip<T extends TelephonyEntity> implements DataInterceptor<T> {
    private DataMonitor<T> sq;
    private DataFilter<T> sr;
    private DataHandler ss;

    public ip(DataMonitor<T> dataMonitor, DataFilter<T> dataFilter, DataHandler dataHandler) {
        this.sq = dataMonitor;
        this.sr = dataFilter;
        this.ss = dataHandler;
    }

    public DataFilter<T> dataFilter() {
        return this.sr;
    }

    public DataHandler dataHandler() {
        return this.ss;
    }

    public DataMonitor<T> dataMonitor() {
        return this.sq;
    }
}
