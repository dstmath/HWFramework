package tmsdk.bg.module.network;

import tmsdk.common.module.network.NetworkInfoEntity;

public interface INetworkChangeCallBack {
    void onClosingDateReached();

    void onDayChanged();

    void onNormalChanged(NetworkInfoEntity networkInfoEntity);
}
