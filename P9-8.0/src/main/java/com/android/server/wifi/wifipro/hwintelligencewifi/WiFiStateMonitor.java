package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import java.util.List;

public class WiFiStateMonitor {
    private static final String ASSOCIATION_REJECT_STATUS_CODE = "wifi_association_reject_status_code";
    private static final int ASSOC_REJECT_COMMON = 1;
    private static final int ASSOC_REJECT_SERVER_FULL = 17;
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isRegister = false;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver(this, null);
    private Context mContext;
    private String mCurrentBSSID;
    private WifiConfiguration mCurrentConfigs;
    private String mCurrentSSID;
    private Handler mHandler;
    private WifiManager mWifiManager;

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ WifiBroadcastReceiver(WiFiStateMonitor this$0, WifiBroadcastReceiver -this1) {
            this();
        }

        private WifiBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                switch (intent.getIntExtra("wifi_state", 4)) {
                    case 1:
                        Log.e(MessageUtil.TAG, "WIFI_STATE_DISABLED");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(4);
                        return;
                    case 3:
                        Log.e(MessageUtil.TAG, "WIFI_STATE_ENABLED");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(3);
                        return;
                    default:
                        return;
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo != null) {
                    if (netInfo.getState() == State.DISCONNECTED) {
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(2);
                        return;
                    }
                    if (netInfo.getState() == State.CONNECTED) {
                        Log.e(MessageUtil.TAG, "NetworkInfo.State.CONNECTED");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(1);
                        return;
                    }
                    if (netInfo.getState() == State.CONNECTING) {
                        Log.e(MessageUtil.TAG, "NetworkInfo.State.CONNECTING");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(27);
                        WifiInfo mWifiInfo = WiFiStateMonitor.this.mWifiManager.getConnectionInfo();
                        if (mWifiInfo != null) {
                            WiFiStateMonitor.this.mCurrentBSSID = mWifiInfo.getBSSID();
                            WiFiStateMonitor.this.mCurrentSSID = mWifiInfo.getSSID();
                            List<WifiConfiguration> configs = WiFiStateMonitor.this.mWifiManager.getConfiguredNetworks();
                            if (configs != null) {
                                for (WifiConfiguration config : configs) {
                                    if (config != null && config.networkId == mWifiInfo.getNetworkId()) {
                                        WiFiStateMonitor.this.mCurrentConfigs = config;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(7);
            } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)) {
                Log.e(MessageUtil.TAG, "CONFIGURED_NETWORKS_CHANGED_ACTION");
                if (intent.getIntExtra("changeReason", 4) == 1) {
                    WifiConfiguration netInfo2 = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                    if (netInfo2 != null && (netInfo2.isTempCreated ^ 1) != 0) {
                        Log.e(MessageUtil.TAG, "CHANGE_REASON_REMOVED");
                        Bundle data = new Bundle();
                        data.putString("bssid", netInfo2.BSSID);
                        data.putString("ssid", netInfo2.SSID);
                        Message msg = new Message();
                        msg.what = 8;
                        msg.setData(data);
                        WiFiStateMonitor.this.mHandler.sendMessage(msg);
                    }
                }
            } else if ("huawei.conn.NETWORK_CONDITIONS_MEASURED".equals(action)) {
                Log.e(MessageUtil.TAG, "ACTION_NETWORK_CONDITIONS_MEASURED");
                switch (intent.getIntExtra("extra_is_internet_ready", -1)) {
                    case -1:
                        Log.e(MessageUtil.TAG, "INTERNET_CHECK_RESULT_NO_INTERNET");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(12);
                        return;
                    case 5:
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(11);
                        return;
                    case 6:
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(13);
                        return;
                    default:
                        return;
                }
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(21);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(22);
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (networkInfo == null || !networkInfo.isConnected()) {
                    WiFiStateMonitor.this.mHandler.sendEmptyMessage(15);
                } else {
                    WiFiStateMonitor.this.mHandler.sendEmptyMessage(14);
                }
            } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                WifiConfiguration errorConfig = WiFiStateMonitor.this.mCurrentConfigs;
                if (errorConfig != null) {
                    NetworkSelectionStatus errorStatus = errorConfig.getNetworkSelectionStatus();
                    int disableReason = errorStatus.getNetworkSelectionDisableReason();
                    if ((errorConfig.status == 1 && disableReason > 0) || (errorStatus.isNetworkEnabled() ^ 1) != 0) {
                        Log.e(MessageUtil.TAG, "auto open connect failed");
                        if (errorStatus.isNetworkEnabled()) {
                            switch (disableReason) {
                                case -1:
                                    WiFiStateMonitor.this.sendConnectFailedReason(0);
                                    return;
                                case 2:
                                    Log.e(MessageUtil.TAG, "DISABLED_ASSOCIATION_REJECT");
                                    if (System.getInt(context.getContentResolver(), WiFiStateMonitor.ASSOCIATION_REJECT_STATUS_CODE, 1) == 17) {
                                        WiFiStateMonitor.this.sendConnectFailedReason(3);
                                        return;
                                    } else {
                                        WiFiStateMonitor.this.sendConnectFailedReason(4);
                                        return;
                                    }
                                case 3:
                                    WiFiStateMonitor.this.sendConnectFailedReason(1);
                                    return;
                                case 4:
                                    WiFiStateMonitor.this.sendConnectFailedReason(2);
                                    return;
                                case 5:
                                    WiFiStateMonitor.this.sendConnectFailedReason(5);
                                    return;
                                default:
                                    return;
                            }
                        } else if (disableReason == 4) {
                            WiFiStateMonitor.this.sendConnectFailedReason(2);
                        } else if (disableReason == 10) {
                            WiFiStateMonitor.this.sendConnectFailedReason(0);
                        } else {
                            switch (disableReason) {
                                case 2:
                                    Log.e(MessageUtil.TAG, "DISABLED_ASSOCIATION_REJECT");
                                    if (System.getInt(context.getContentResolver(), WiFiStateMonitor.ASSOCIATION_REJECT_STATUS_CODE, 1) == 17) {
                                        WiFiStateMonitor.this.sendConnectFailedReason(3);
                                        return;
                                    } else {
                                        WiFiStateMonitor.this.sendConnectFailedReason(4);
                                        return;
                                    }
                                case 3:
                                    WiFiStateMonitor.this.sendConnectFailedReason(1);
                                    return;
                                default:
                                    return;
                            }
                        }
                    }
                }
            }
        }
    }

    public WiFiStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public void startMonitor() {
        registerBroadcastReceiver();
    }

    public void stopMonitor() {
        unRegisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        if (!this.isRegister) {
            this.intentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
            this.intentFilter.addAction("huawei.conn.NETWORK_CONDITIONS_MEASURED");
            this.intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
            this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.intentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            this.intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
            this.intentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
            this.isRegister = true;
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (this.isRegister) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.isRegister = false;
        }
    }

    private void sendConnectFailedReason(int reason) {
        Bundle data = new Bundle();
        data.putInt("reason", reason);
        data.putString("bssid", this.mCurrentBSSID);
        data.putString("ssid", this.mCurrentSSID);
        Message msg = new Message();
        msg.what = 24;
        msg.setData(data);
        this.mHandler.sendMessage(msg);
    }
}
