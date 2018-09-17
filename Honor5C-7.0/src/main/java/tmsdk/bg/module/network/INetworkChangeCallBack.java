package tmsdk.bg.module.network;

import tmsdk.common.module.network.NetworkInfoEntity;

/* compiled from: Unknown */
public interface INetworkChangeCallBack {
    void onClosingDateReached();

    void onDayChanged();

    void onNormalChanged(NetworkInfoEntity networkInfoEntity);
}
