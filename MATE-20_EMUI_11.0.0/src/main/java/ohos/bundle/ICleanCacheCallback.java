package ohos.bundle;

import ohos.rpc.IRemoteBroker;

public interface ICleanCacheCallback extends IRemoteBroker {
    void onCleanCacheFinished(boolean z);
}
