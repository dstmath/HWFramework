package com.android.server;

import android.content.Context;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.telephony.TelephonyManager;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.wifipro.WifiProCommonUtils;

public class HwNetworkMonitor extends NetworkMonitor {
    public static final int NETWORK_TEST_RESULT_PORTAL = 2;
    private boolean mCheckCompleted;
    private Context mContext;
    public HwNetworkPropertyChecker mHwNetworkPropertyChecker;
    private NetworkAgentInfo mNetworkAgentInfo;
    private boolean mWifiProEnabled;

    public HwNetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest) {
        super(context, handler, networkAgentInfo, defaultRequest);
        this.mWifiProEnabled = false;
        this.mCheckCompleted = false;
        this.mContext = null;
        this.mNetworkAgentInfo = null;
        this.mHwNetworkPropertyChecker = null;
        this.mWifiProEnabled = false;
        this.mCheckCompleted = false;
        this.mContext = context;
        this.mNetworkAgentInfo = networkAgentInfo;
        initialize(context, null, null, networkAgentInfo, true);
    }

    public void initialize(Context context, WifiManager wm, TelephonyManager tel, NetworkAgentInfo nai, boolean enabled) {
        if (WifiProCommonUtils.isWifiProPropertyEnabled() && nai != null && nai.networkInfo != null && nai.networkInfo.getType() == 1) {
            this.mHwNetworkPropertyChecker = new HwNetworkPropertyChecker(context, wm, tel, enabled, nai, true);
            this.mWifiProEnabled = true;
        }
    }

    public boolean isWifiProEnabled() {
        return this.mWifiProEnabled;
    }

    public boolean isCheckCompletedByWifiPro() {
        return this.mCheckCompleted;
    }

    public int getRespCodeByWifiPro() {
        int resp = WifiProCommonUtils.HTTP_UNREACHALBE;
        if (this.mWifiProEnabled) {
            resp = this.mHwNetworkPropertyChecker.isCaptivePortal(false);
            if (this.mHwNetworkPropertyChecker.isCheckingCompleted(resp)) {
                this.mCheckCompleted = true;
            }
        }
        return resp;
    }

    public int resetReevaluateDelayMs(int ms) {
        if (this.mWifiProEnabled) {
            return 2000;
        }
        return ms;
    }

    public long getReqTimestamp() {
        return this.mWifiProEnabled ? this.mHwNetworkPropertyChecker.getReqTimestamp() : 0;
    }

    public long getRespTimestamp() {
        return this.mWifiProEnabled ? this.mHwNetworkPropertyChecker.getRespTimestamp() : 0;
    }

    public void reportPortalNetwork(Handler handler, int netId, String redirectUrl) {
        handler.sendMessage(obtainMessage(532482, 2, netId, redirectUrl));
    }

    public void releaseNetworkPropertyChecker() {
        if (this.mHwNetworkPropertyChecker != null) {
            this.mHwNetworkPropertyChecker.release();
        }
    }

    public void resetNetworkMonitor() {
        this.mCheckCompleted = false;
        if (this.mHwNetworkPropertyChecker != null) {
            log("resetNetworkMonitor");
            this.mHwNetworkPropertyChecker.resetCheckerStatus();
            return;
        }
        log("resetNetworkMonitor initialize");
        initialize(this.mContext, null, null, this.mNetworkAgentInfo, true);
    }
}
