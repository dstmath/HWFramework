package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.HiSiWifiComm;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManagerHisiExt;
import android.provider.Settings.Global;
import android.util.Slog;

public class WifiServiceHisiExt {
    public static final int MIN_RSSI = -200;
    private static final String TAG = "WifiServiceHisiExt";
    private Context mContext;
    public boolean mIsReceiverRegistered = false;
    private WifiP2pManager mWifiP2pManager = null;
    private WifiP2pManagerHisiExt mWifiP2pManagerHisiExt = null;
    public WifiStateMachineHisiExt mWifiStateMachineHisiExt;

    public WifiServiceHisiExt(Context context) {
        this.mContext = context;
        this.mWifiP2pManagerHisiExt = new WifiP2pManagerHisiExt();
    }

    public static boolean hisiWifiEnabled() {
        return HiSiWifiComm.hisiWifiEnabled();
    }

    public boolean isWifiP2pEnabled() {
        if (this.mWifiP2pManager == null) {
            this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        }
        return this.mWifiP2pManagerHisiExt.isWifiP2pEnabled();
    }

    public void setWifiP2pEnabled(int p2pFlag) {
        if (this.mWifiP2pManager == null) {
            this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        }
        this.mWifiP2pManagerHisiExt.setWifiP2pEnabled(p2pFlag);
    }

    public int checkInteractAcrossUsersPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS");
    }

    public int checkInteractAcrossUsersFullPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL");
    }

    public int checkBroadcastStickyPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.BROADCAST_STICKY");
    }

    public boolean checkUseNotCoexistPermission() {
        if (-1 == checkInteractAcrossUsersPermission()) {
            Slog.e(TAG, "permission 'INTERACT_ACROSS_USERS' has not been granted,check continue");
            if (-1 == checkInteractAcrossUsersFullPermission()) {
                Slog.e(TAG, "permission 'INTERACT_ACROSS_USERS_FULL' has not been granted,return false");
                return false;
            }
        }
        if (-1 != checkBroadcastStickyPermission()) {
            return true;
        }
        Slog.e(TAG, "permission 'BROADCAST_STICKY' has not been granted,return false");
        return false;
    }

    public boolean isAirplaneSensitive() {
        String airplaneModeRadios = Global.getString(this.mContext.getContentResolver(), "airplane_mode_radios");
        if (airplaneModeRadios != null) {
            return airplaneModeRadios.contains("wifi");
        }
        return true;
    }

    public boolean isAirplaneModeOn() {
        return isAirplaneSensitive() && Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }
}
