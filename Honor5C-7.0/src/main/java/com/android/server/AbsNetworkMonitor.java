package com.android.server;

import android.os.Handler;
import com.android.internal.util.StateMachine;

public class AbsNetworkMonitor extends StateMachine {
    private static final int BASE = 532480;
    public static final int CMD_NETWORK_ROAMING_CONNECTED = 532581;

    public AbsNetworkMonitor(String tag) {
        super(tag);
    }

    public boolean isWifiProEnabled() {
        return false;
    }

    public boolean isCheckCompletedByWifiPro() {
        return false;
    }

    public int getRespCodeByWifiPro() {
        return 599;
    }

    public int resetReevaluateDelayMs(int ms) {
        return ms;
    }

    public long getReqTimestamp() {
        return 0;
    }

    public long getRespTimestamp() {
        return 0;
    }

    public boolean checkingCompletedByWifiPro(int respCode) {
        return false;
    }

    public void reportPortalNetwork(Handler handler, int netId, String redirectUrl) {
    }

    public void releaseNetworkPropertyChecker() {
    }

    public void resetNetworkMonitor() {
    }
}
