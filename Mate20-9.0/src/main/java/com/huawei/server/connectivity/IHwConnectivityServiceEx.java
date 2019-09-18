package com.huawei.server.connectivity;

import android.net.RouteInfo;
import android.os.Message;
import com.android.server.connectivity.NetworkAgentInfo;

public interface IHwConnectivityServiceEx {
    NetworkAgentInfo getIdenticalActiveNetworkAgentInfo(NetworkAgentInfo networkAgentInfo);

    void maybeHandleNetworkAgentMessageEx(Message message, NetworkAgentInfo networkAgentInfo);

    void removeLegacyRouteToHost(int i, RouteInfo routeInfo, int i2);

    void setupUniqueDeviceName();
}
