package com.android.server.connectivity;

import android.net.LinkProperties;
import android.os.INetworkManagementService;
import android.os.Messenger;
import com.android.server.ConnectivityService;
import java.util.HashMap;

public interface IHwConnectivityServiceInner {
    int getCodeRemoveLegacyrouteToHost();

    String getDescriptor();

    NetworkAgentInfo getHwNetworkForType(int i);

    INetworkManagementService getNetd();

    HashMap<Messenger, NetworkAgentInfo> getNetworkAgentInfos();

    ConnectivityService getService();

    void hwUpdateLinkProperties(NetworkAgentInfo networkAgentInfo, LinkProperties linkProperties);
}
