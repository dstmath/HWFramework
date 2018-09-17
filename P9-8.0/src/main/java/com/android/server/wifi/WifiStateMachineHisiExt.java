package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.HiSiWifiComm;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManagerHisiExt;
import android.os.UserHandle;
import android.util.Slog;
import java.util.concurrent.atomic.AtomicInteger;

public class WifiStateMachineHisiExt {
    private static final String TAG = "WifiStateMachineHisiExt";
    public static final String WIFI_STATE_DISABLE_HISI_ACTION = "android.net.hisi.wifi.WIFI_STATE_DISABLE";
    private Context mContext;
    private final int mDriverStopP2pDelayMs;
    private boolean mStartWifiForP2p = false;
    AtomicInteger mWifiApState;
    private WifiConfigManager mWifiConfigManager;
    private WifiP2pManager mWifiP2pManager = null;
    private WifiP2pManagerHisiExt mWifiP2pManagerHisiExt = null;
    AtomicInteger mWifiState;

    public WifiStateMachineHisiExt(Context context, WifiConfigManager wifiConfigManager, AtomicInteger wifiState, AtomicInteger wifiApState) {
        this.mContext = context;
        this.mWifiConfigManager = wifiConfigManager;
        this.mWifiState = wifiState;
        this.mWifiApState = wifiApState;
        this.mDriverStopP2pDelayMs = this.mContext.getResources().getInteger(34275332);
        this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        this.mWifiP2pManagerHisiExt = new WifiP2pManagerHisiExt();
    }

    public void sendSwitchToP2pBroadcast() {
        this.mContext.sendBroadcastAsUser(new Intent("android.net.wifi.p2p.hisi.SWITCH_TO_P2P_MODE"), UserHandle.ALL);
        Slog.d(TAG, "sendSwitchToP2pBroadcast.");
    }

    public void startWifiForP2pCheck() {
        if (this.mStartWifiForP2p) {
            WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
            this.mStartWifiForP2p = false;
            sendSwitchToP2pBroadcast();
        }
    }

    public static boolean hisiWifiEnabled() {
        return HiSiWifiComm.hisiWifiEnabled();
    }

    public void setRecoveryWifiFlag(boolean flag) {
        if (this.mWifiP2pManager == null) {
            this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        }
        this.mWifiP2pManagerHisiExt.setRecoveryWifiFlag(flag);
    }

    public void setWifiApEnabled(boolean enabled) {
        if (this.mWifiP2pManager == null) {
            this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        }
        if (enabled && this.mWifiP2pManagerHisiExt.isWifiP2pEnabled()) {
            setWifiApStateFlagByManual(true);
            setRecoveryWifiFlag(false);
            this.mWifiP2pManagerHisiExt.setWifiP2pEnabled(0);
        }
    }

    public boolean isP2pEnable() {
        if (this.mWifiP2pManagerHisiExt.isWifiP2pEnabled()) {
            return true;
        }
        return false;
    }

    public int getDriverStopP2pDelayMs() {
        return this.mDriverStopP2pDelayMs;
    }

    public void sendWifiStateDisabledBroadcast() {
        Slog.d(TAG, "sendBroadcast WIFI_STATE_DISABLE_HISI_ACTION.");
        Intent intent = new Intent(WIFI_STATE_DISABLE_HISI_ACTION);
        intent.addFlags(67108864);
        this.mContext.sendBroadcast(intent, null);
    }

    public void setWifiStateByManual(boolean enable) {
        int wifiState;
        int previousWifiState = this.mWifiState.get();
        Slog.d(TAG, "setWifiStateByManual: " + enable + ",previousWifiState:" + previousWifiState);
        if (enable) {
            wifiState = 3;
        } else {
            wifiState = 1;
        }
        this.mWifiState.set(wifiState);
        Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiState);
        intent.putExtra("previous_wifi_state", previousWifiState);
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } catch (Exception e) {
            Slog.e(TAG, "setWifiStateByManual exception:" + e);
        }
    }

    public int setWifiApStateFlagByManual(boolean enable) {
        int wifiApState;
        if (enable) {
            wifiApState = 13;
        } else {
            wifiApState = 11;
        }
        this.mWifiApState.set(wifiApState);
        return wifiApState;
    }

    public void setWifiApStateByManual(boolean enable) {
        int previousWifiApState = this.mWifiApState.get();
        Slog.d(TAG, "setWifiApStateByManual: " + enable + ",previousWifiApState:" + previousWifiApState);
        int wifiApState = setWifiApStateFlagByManual(enable);
        Intent intent = new Intent("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiApState);
        intent.putExtra("previous_wifi_state", previousWifiApState);
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } catch (Exception e) {
            Slog.e(TAG, "setWifiApStateByManual exception:" + e);
        }
    }

    public void setWifiEnableForP2p(boolean enable) {
        Slog.d(TAG, "setWifiEnableForP2p: " + enable);
        this.mStartWifiForP2p = enable;
    }
}
