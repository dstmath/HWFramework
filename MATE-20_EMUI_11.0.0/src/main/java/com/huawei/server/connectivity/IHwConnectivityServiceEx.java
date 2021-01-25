package com.huawei.server.connectivity;

import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Message;
import com.android.server.connectivity.NetworkAgentInfo;

public interface IHwConnectivityServiceEx {
    int getCacheNetworkState(int i, String str);

    NetworkAgentInfo getIdenticalActiveNetworkAgentInfo(NetworkAgentInfo networkAgentInfo);

    void maybeHandleNetworkAgentMessageEx(Message message, NetworkAgentInfo networkAgentInfo, Handler handler);

    boolean releaseNetworkSliceByApp(NetworkRequest networkRequest, int i);

    void setCacheNetworkState(int i, String str, boolean z);

    void setupUniqueDeviceName();
}
