package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import com.android.internal.util.StateMachine;

public abstract class WifiRepeater extends StateMachine {
    protected static final String TAG = "WifiRepeater";

    public abstract void handleClientListChanged(WifiP2pGroup wifiP2pGroup);

    public abstract void handleP2pTethered(WifiP2pGroup wifiP2pGroup);

    public abstract void handleP2pUntethered();

    public abstract void handleWifiConnect(WifiInfo wifiInfo, WifiConfiguration wifiConfiguration);

    public abstract void handleWifiDisconnect();

    public abstract boolean isEncryptionTypeTetheringAllowed();

    public abstract boolean isStereoAudioWorking();

    public abstract int retrieveDownstreamBand();

    public abstract int retrieveDownstreamChannel();

    public abstract void sendStateChangedBroadcast();

    public abstract void setStereoAudioWorkingFlag(boolean z);

    protected WifiRepeater() {
        super(TAG);
    }
}
