package tmsdkobf;

import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.common.module.aresengine.TelephonyEntity;

public abstract class hj<T extends TelephonyEntity> {
    protected abstract void a(DataHandler dataHandler);

    protected abstract void unbind();
}
