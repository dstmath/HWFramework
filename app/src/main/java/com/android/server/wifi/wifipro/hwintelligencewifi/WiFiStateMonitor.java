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
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
import java.util.List;

public class WiFiStateMonitor {
    private static final String ASSOCIATION_REJECT_STATUS_CODE = "wifi_association_reject_status_code";
    private static final int ASSOC_REJECT_COMMON = 1;
    private static final int ASSOC_REJECT_SERVER_FULL = 17;
    private IntentFilter intentFilter;
    private boolean isRegister;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private String mCurrentBSSID;
    private WifiConfiguration mCurrentConfigs;
    private String mCurrentSSID;
    private Handler mHandler;
    private WifiManager mWifiManager;

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private WifiBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                switch (intent.getIntExtra("wifi_state", 4)) {
                    case WiFiStateMonitor.ASSOC_REJECT_COMMON /*1*/:
                        Log.e(MessageUtil.TAG, "WIFI_STATE_DISABLED");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(4);
                        return;
                    case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                        Log.e(MessageUtil.TAG, "WIFI_STATE_ENABLED");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(3);
                        return;
                    default:
                        return;
                }
            }
            if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo != null) {
                    if (netInfo.getState() == State.DISCONNECTED) {
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(2);
                        return;
                    }
                    if (netInfo.getState() == State.CONNECTED) {
                        Log.e(MessageUtil.TAG, "NetworkInfo.State.CONNECTED");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(WiFiStateMonitor.ASSOC_REJECT_COMMON);
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
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(7);
                return;
            }
            if (MessageUtil.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action)) {
                Log.e(MessageUtil.TAG, "CONFIGURED_NETWORKS_CHANGED_ACTION");
                if (intent.getIntExtra(MessageUtil.EXTRA_CHANGE_REASON, 4) == WiFiStateMonitor.ASSOC_REJECT_COMMON) {
                    WifiConfiguration netInfo2 = (WifiConfiguration) intent.getParcelableExtra(MessageUtil.EXTRA_WIFI_CONFIGURATION);
                    if (netInfo2 != null && !netInfo2.isTempCreated) {
                        Log.e(MessageUtil.TAG, "CHANGE_REASON_REMOVED");
                        Bundle data = new Bundle();
                        data.putString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID, netInfo2.BSSID);
                        data.putString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID, netInfo2.SSID);
                        Message msg = new Message();
                        msg.what = 8;
                        msg.setData(data);
                        WiFiStateMonitor.this.mHandler.sendMessage(msg);
                        return;
                    }
                    return;
                }
                return;
            }
            if (MessageUtil.ACTION_NETWORK_CONDITIONS_MEASURED.equals(action)) {
                Log.e(MessageUtil.TAG, "ACTION_NETWORK_CONDITIONS_MEASURED");
                switch (intent.getIntExtra(MessageUtil.EXTRA_IS_INTERNET_READY, -1)) {
                    case WifiScanGenieController.Scan /*-1*/:
                        Log.e(MessageUtil.TAG, "INTERNET_CHECK_RESULT_NO_INTERNET");
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(12);
                        return;
                    case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(11);
                        return;
                    case MessageUtil.MSG_WIFI_DISABLEING /*6*/:
                        WiFiStateMonitor.this.mHandler.sendEmptyMessage(13);
                        return;
                    default:
                        return;
                }
            }
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(21);
                return;
            }
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(22);
                return;
            }
            if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (networkInfo == null || !networkInfo.isConnected()) {
                    WiFiStateMonitor.this.mHandler.sendEmptyMessage(15);
                    return;
                }
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(14);
                return;
            }
            if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                WifiConfiguration errorConfig = WiFiStateMonitor.this.mCurrentConfigs;
                if (errorConfig != null) {
                    NetworkSelectionStatus errorStatus = errorConfig.getNetworkSelectionStatus();
                    int disableReason = errorStatus.getNetworkSelectionDisableReason();
                    int i = errorConfig.status;
                    if ((r0 == WiFiStateMonitor.ASSOC_REJECT_COMMON && disableReason > 0) || !errorStatus.isNetworkEnabled()) {
                        Log.e(MessageUtil.TAG, "auto open connect failed");
                        if (errorStatus.isNetworkEnabled()) {
                            switch (disableReason) {
                                case WifiScanGenieController.Scan /*-1*/:
                                    WiFiStateMonitor.this.sendConnectFailedReason(0);
                                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                                    Log.e(MessageUtil.TAG, "DISABLED_ASSOCIATION_REJECT");
                                    if (System.getInt(context.getContentResolver(), WiFiStateMonitor.ASSOCIATION_REJECT_STATUS_CODE, WiFiStateMonitor.ASSOC_REJECT_COMMON) == WiFiStateMonitor.ASSOC_REJECT_SERVER_FULL) {
                                        WiFiStateMonitor.this.sendConnectFailedReason(3);
                                        return;
                                    }
                                    WiFiStateMonitor.this.sendConnectFailedReason(4);
                                case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                                    WiFiStateMonitor.this.sendConnectFailedReason(WiFiStateMonitor.ASSOC_REJECT_COMMON);
                                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                                    WiFiStateMonitor.this.sendConnectFailedReason(2);
                                case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                                    WiFiStateMonitor.this.sendConnectFailedReason(5);
                                default:
                            }
                        } else if (disableReason == 4) {
                            WiFiStateMonitor.this.sendConnectFailedReason(2);
                        } else if (disableReason == 9) {
                            WiFiStateMonitor.this.sendConnectFailedReason(0);
                        } else {
                            switch (disableReason) {
                                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                                    Log.e(MessageUtil.TAG, "DISABLED_ASSOCIATION_REJECT");
                                    if (System.getInt(context.getContentResolver(), WiFiStateMonitor.ASSOCIATION_REJECT_STATUS_CODE, WiFiStateMonitor.ASSOC_REJECT_COMMON) == WiFiStateMonitor.ASSOC_REJECT_SERVER_FULL) {
                                        WiFiStateMonitor.this.sendConnectFailedReason(3);
                                        return;
                                    }
                                    WiFiStateMonitor.this.sendConnectFailedReason(4);
                                case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                                    WiFiStateMonitor.this.sendConnectFailedReason(WiFiStateMonitor.ASSOC_REJECT_COMMON);
                                default:
                            }
                        }
                    }
                }
            }
        }
    }

    public WiFiStateMonitor(Context context, Handler handler) {
        this.isRegister = false;
        this.intentFilter = new IntentFilter();
        this.mBroadcastReceiver = new WifiBroadcastReceiver();
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
            this.intentFilter.addAction(MessageUtil.CONFIGURED_NETWORKS_CHANGED_ACTION);
            this.intentFilter.addAction(MessageUtil.ACTION_NETWORK_CONDITIONS_MEASURED);
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
        data.putInt(MessageUtil.MSG_KEY_REASON, reason);
        data.putString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID, this.mCurrentBSSID);
        data.putString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID, this.mCurrentSSID);
        Message msg = new Message();
        msg.what = 24;
        msg.setData(data);
        this.mHandler.sendMessage(msg);
    }
}
