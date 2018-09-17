package tmsdkobf;

import tmsdk.bg.module.aresengine.DataInterceptor;
import tmsdk.common.module.aresengine.TelephonyEntity;

public abstract class hk<T extends TelephonyEntity> {
    public abstract DataInterceptor<T> create();
}
