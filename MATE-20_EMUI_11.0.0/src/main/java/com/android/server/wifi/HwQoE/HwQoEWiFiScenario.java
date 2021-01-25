package com.android.server.wifi.HwQoE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;

public class HwQoEWiFiScenario {
    private static final int NETWORK_PROPERTY_FLAG = -101;
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isRegister = false;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private Context mContext;
    private Handler mHandler;
    private HwQoEHilink mHwQoEHilink = null;

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
            HwQoEUtils.logD(false, "HwQoEWiFiScenario registerBroadcastReceiver", new Object[0]);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkStateChange(Intent intent) {
        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (netInfo == null) {
            return;
        }
        if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
            HwQoEUtils.logD(false, "NetworkInfo.State.DISCONNECTED", new Object[0]);
            this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_DISCONNECT);
        } else if (netInfo.getState() == NetworkInfo.State.CONNECTED || netInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            HwQoEUtils.logD(false, "NetworkInfo.State.CONNECTED", new Object[0]);
            this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_CONNECTED);
        } else {
            HwQoEUtils.logD(false, "NetworkInfo.State is invalid", new Object[0]);
        }
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private WifiBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    int wifiStatus = intent.getIntExtra("wifi_state", 4);
                    if (wifiStatus == 1) {
                        HwQoEUtils.logD(false, "WifiBroadcastReceiver WIFI_STATE_DISABLED", new Object[0]);
                        HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_DISABLE);
                    } else if (wifiStatus == 3) {
                        HwQoEUtils.logD(false, "WifiBroadcastReceiver WIFI_STATE_ENABLED", new Object[0]);
                        HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_ENABLED);
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    HwQoEWiFiScenario.this.handleNetworkStateChange(intent);
                } else if ("com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION".equals(action)) {
                    int property = intent.getIntExtra("wifi_network_property", HwQoEWiFiScenario.NETWORK_PROPERTY_FLAG);
                    HwQoEUtils.logD(false, "WifiBroadcastReceiver ACTION_NETWOR_PROPERTY_NOTIFICATION property = %{public}d", Integer.valueOf(property));
                    if (property == 5) {
                        HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_INTERNET);
                    }
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    HwQoEWiFiScenario.this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_RSSI_CHANGED);
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwQoEUtils.logD(false, "BOOT_COMPLETED", new Object[0]);
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
                } else {
                    HwQoEUtils.logD(false, "WifiBroadcastReceiver invalid action", new Object[0]);
                }
            }
        }
    }
}
