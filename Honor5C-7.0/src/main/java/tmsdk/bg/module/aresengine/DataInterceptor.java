package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.TelephonyEntity;

/* compiled from: Unknown */
public interface DataInterceptor<T extends TelephonyEntity> {
    DataFilter<T> dataFilter();

    DataHandler dataHandler();

    DataMonitor<T> dataMonitor();
}
