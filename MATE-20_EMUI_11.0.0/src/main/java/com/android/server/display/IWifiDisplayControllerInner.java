package com.android.server.display;

import android.media.RemoteDisplay;
import android.net.wifi.p2p.WifiP2pManager;
import com.android.server.display.WifiDisplayController;

public interface IWifiDisplayControllerInner {
    void disconnectInner();

    WifiP2pManager.Channel getWifiP2pChannelInner();

    WifiP2pManager getWifiP2pManagerInner();

    boolean getmDiscoverPeersInProgress();

    WifiDisplayController.Listener getmListener();

    RemoteDisplay getmRemoteDisplay();

    void postDelayedDiscover();

    void requestPeersEx();

    void requestStartScanInner();
}
