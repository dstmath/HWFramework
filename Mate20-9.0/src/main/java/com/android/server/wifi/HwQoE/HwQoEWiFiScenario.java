package com.android.server.wifi.HwQoE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import com.android.server.wifi.wifipro.WifiproUtils;

public class HwQoEWiFiScenario {
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isRegister = false;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public HwQoEHilink mHwQoEHilink = null;

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private WifiBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                int wifistatue = intent.getIntExtra("wifi_state", 4);
                if (wifistatue == 1) {
                    HwQoEUtils.logD("WifiBroadcastReceiver WIFI_STATE_DISABLED");
                    HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_DISABLE);
                } else if (wifistatue == 3) {
                    HwQoEUtils.logD("WifiBroadcastReceiver WIFI_STATE_ENABLED");
                    HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_ENABLED);
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo == null) {
                    return;
                }
                if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    HwQoEUtils.logD("NetworkInfo.State.DISCONNECTED");
                    HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_DISCONNECT);
                } else if (netInfo.getState() == NetworkInfo.State.CONNECTED || netInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                    HwQoEUtils.logD("NetworkInfo.State.CONNECTED");
                    HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_CONNECTED);
                }
            } else if ("com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION".equals(action)) {
                int property = intent.getIntExtra("wifi_network_property", WifiproUtils.NET_INET_QOS_LEVEL_UNKNOWN);
                HwQoEUtils.logD("WifiBroadcastReceiver ACTION_NETWOR_PROPERTY_NOTIFICATION property = " + property);
                if (property == 5) {
                    HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_INTERNET);
                }
            } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_RSSI_CHANGED);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwQoEUtils.logD("BOOT_COMPLETED");
                HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_BOOT_COMPLETED);
            } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_SCAN_RESULTS);
            } else if ("android.bluetooth.adapter.action.DISCOVERY_STARTED".equals(action)) {
                HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_BT_SCAN_STARTED);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_SCREEN_OFF);
                HwQoEWiFiScenario.this.mHwQoEHilink.handleScreenStateChanged(false);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_SCREEN_ON);
                HwQoEWiFiScenario.this.mHwQoEHilink.handleScreenStateChanged(true);
            }
        }
    }

    public HwQoEWiFiScenario(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mHwQoEHilink = HwQoEHilink.getInstance(this.mContext);
    }

    public void startMonitor() {
        registerBroadcastReceiver();
    }

    public void stopMonitor() {
        unRegisterBroadcastReceiver();
    }

    private void unRegisterBroadcastReceiver() {
        if (this.isRegister) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.isRegister = false;
        }
    }

    private void registerBroadcastReceiver() {
        if (!this.isRegister) {
            HwQoEUtils.logD("HwQoEWiFiScenario registerBroadcastReceiver");
            this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
            this.intentFilter.addAction("com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION");
            this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
            this.intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
            this.intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED");
            this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.intentFilter.addCategory("android.net.wifi.WIFI_STATE_CHANGED@hwBrExpand@WifiStatus=WIFIENABLED|WifiStatus=WIFIDISABLED");
            this.intentFilter.addCategory("android.net.wifi.STATE_CHANGE@hwBrExpand@WifiNetStatus=WIFICON|WifiNetStatus=WIFIDSCON");
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
            this.isRegister = true;
        }
    }
}
