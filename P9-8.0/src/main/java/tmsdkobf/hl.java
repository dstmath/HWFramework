package tmsdkobf;

import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptor;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.common.module.aresengine.TelephonyEntity;

public final class hl<T extends TelephonyEntity> implements DataInterceptor<T> {
    private DataMonitor<T> pR;
    private DataFilter<T> pS;
    private DataHandler pT;

    public hl(DataMonitor<T> dataMonitor, DataFilter<T> dataFilter, DataHandler dataHandler) {
        this.pR = dataMonitor;
        this.pS = dataFilter;
        this.pT = dataHandler;
    }

    public DataFilter<T> dataFilter() {
        return this.pS;
    }

    public DataHandler dataHandler() {
        return this.pT;
    }

    public DataMonitor<T> dataMonitor() {
        return this.pR;
    }
}
