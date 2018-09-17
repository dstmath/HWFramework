package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.TelephonyEntity;

public interface DataInterceptor<T extends TelephonyEntity> {
    DataFilter<T> dataFilter();

    DataHandler dataHandler();

    DataMonitor<T> dataMonitor();
}
