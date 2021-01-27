package com.android.server.connectivity;

import android.net.LinkProperties;
import android.os.INetworkManagementService;
import android.os.Messenger;
import com.android.server.ConnectivityService;
import java.util.HashMap;

public interface IHwConnectivityServiceInner {
    NetworkAgentInfo getHwNetworkForType(int i);

    HashMap<Messenger, NetworkAgentInfo> getNetworkAgentInfos();

    ConnectivityService getService();

    INetworkManagementService getmNMSHw();

    void hwUpdateLinkProperties(NetworkAgentInfo networkAgentInfo, LinkProperties linkProperties);
}
