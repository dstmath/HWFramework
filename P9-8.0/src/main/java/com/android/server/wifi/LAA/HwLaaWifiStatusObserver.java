package com.android.server.wifi.LAA;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.Handler;

public class HwLaaWifiStatusObserver {
    private static final String TAG = "LAA_HwLaaWifiStatusObserver";
    private boolean is5GHz_Ap = false;
    private boolean is5GHz_P2p = false;
    private boolean is5GHz_Sta = false;
    private final ConnectivityManager mConnectivityManager;
    private Context mContext;
    private Handler mHwLaaControllerHandler;
    private final WifiManager mWifiManager;

    public HwLaaWifiStatusObserver(Context context, Handler handler) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mHwLaaControllerHandler = handler;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    HwLaaWifiStatusObserver.this.handleConnectivityAction();
                } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                    HwLaaWifiStatusObserver.this.handleWifiApStateChangeAction();
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    HwLaaWifiStatusObserver.this.handleWifiP2pConnectionChangedAction(intent);
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    HwLaaWifiStatusObserver.this.handleWiFiNetworkChangeAction(intent);
                }
            }
        }, filter);
    }

    private void handleWiFiNetworkChangeAction(Intent intent) {
        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (networkInfo == null) {
            return;
        }
        if (networkInfo.isConnected()) {
            notificateNetworkChanged(2, 0);
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            if (wifiInfo == null) {
                return;
            }
            if (!wifiInfo.is5GHz()) {
                this.is5GHz_Sta = false;
            } else if (!this.is5GHz_Sta) {
                this.is5GHz_Sta = true;
                HwLaaUtils.logD(TAG, "Wifi STA 5G connected*");
                requestSendLaaCmd(0, 1);
            }
        } else if (networkInfo.getState() == State.DISCONNECTED && this.is5GHz_Sta) {
            HwLaaUtils.logD(TAG, "Wifi STA 5G DisConnected");
            this.is5GHz_Sta = false;
            requestSendLaaCmd(1, 1);
        }
    }

    private void handleConnectivityAction() {
        NetworkInfo networkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == 0) {
            HwLaaUtils.logD(TAG, "TYPE_MOBILE networkInfo:" + networkInfo.getState());
            if (networkInfo.isConnected()) {
                notificateNetworkChanged(1, 0);
            } else if (networkInfo.getState() == State.DISCONNECTED) {
                notificateNetworkChanged(2, 0);
            }
        }
    }

    private void handleWifiApStateChangeAction() {
        if (this.mWifiManager.isWifiApEnabled()) {
            WifiConfiguration wifiApConfiguration = this.mWifiManager.getWifiApConfiguration();
            if (wifiApConfiguration == null) {
                return;
            }
            if (1 == wifiApConfiguration.apBand) {
                HwLaaUtils.logD(TAG, "Wifi AP 5G Enabled");
                this.is5GHz_Ap = true;
                requestSendLaaCmd(0, 3);
                return;
            }
            this.is5GHz_Ap = false;
        } else if (this.is5GHz_Ap) {
            this.is5GHz_Ap = false;
            HwLaaUtils.logD(TAG, "Wifi AP 5G Disenabled");
            requestSendLaaCmd(1, 3);
        }
    }

    private void handleWifiP2pConnectionChangedAction(Intent intent) {
        if (((NetworkInfo) intent.getParcelableExtra("networkInfo")).isConnected()) {
            if (ScanResult.is5GHz(((WifiP2pGroup) intent.getParcelableExtra("p2pGroupInfo")).getFrequence())) {
                this.is5GHz_P2p = true;
                HwLaaUtils.logD(TAG, "Wifi P2P 5G Connected");
                requestSendLaaCmd(0, 2);
                return;
            }
            this.is5GHz_P2p = false;
        } else if (this.is5GHz_P2p) {
            this.is5GHz_P2p = false;
            HwLaaUtils.logD(TAG, "Wifi P2P 5G DisConnected");
            requestSendLaaCmd(1, 2);
        }
    }

    private synchronized void requestSendLaaCmd(int cmd, int type) {
        this.mHwLaaControllerHandler.sendMessage(this.mHwLaaControllerHandler.obtainMessage(1, cmd, type));
    }

    private synchronized void notificateNetworkChanged(int status, int type) {
        this.mHwLaaControllerHandler.sendMessage(this.mHwLaaControllerHandler.obtainMessage(3, status, type));
    }
}
