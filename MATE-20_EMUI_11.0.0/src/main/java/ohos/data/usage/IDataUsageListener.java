package ohos.data.usage;

import ohos.rpc.IRemoteBroker;

public interface IDataUsageListener extends IRemoteBroker {
    public static final int TRANSACTION_ON_STORAGE_STATE_CHANGED = 1;
    public static final int TRANSACTION_ON_VOLUME_STATE_CHANGED = 2;
    public static final String descriptor = "datausagecallback";

    void onStorageStateChanged(String str, String str2, String str3);

    void onVolumeStateChanged(VolumeView volumeView, int i, int i2);
}
