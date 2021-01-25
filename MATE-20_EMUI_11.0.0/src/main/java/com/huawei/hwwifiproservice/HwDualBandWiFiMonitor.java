package com.huawei.hwwifiproservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HwDualBandWiFiMonitor {
    private boolean isRegister = false;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private Context mContext;
    private Handler mHandler;
    private IntentFilter mIntentFilter = new IntentFilter();

    public HwDualBandWiFiMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public void startMonitor() {
        registerBroadcastReceiver();
    }

    public void stopMonitor() {
        unRegisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        if (!this.isRegister) {
            this.mIntentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
            this.mIntentFilter.addAction("huawei.conn.NETWORK_CONDITIONS_MEASURED");
            this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
            this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.mIntentFilter.addAction("android.intent.action.SCREEN_ON");
            this.mIntentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
            this.isRegister = true;
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (this.isRegister) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.isRegister = false;
        }
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private WifiBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                HwDualBandWiFiMonitor.this.handleWifiStateChangeAction(intent);
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                HwDualBandWiFiMonitor.this.handleNetStateChangedAction(action, (NetworkInfo) intent.getParcelableExtra("networkInfo"));
            } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                HwDualBandWiFiMonitor.this.mHandler.sendEmptyMessage(7);
            } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)) {
                HwDualBandWiFiMonitor.this.handleCfgNetChgAction(intent);
            } else if ("huawei.conn.NETWORK_CONDITIONS_MEASURED".equals(action)) {
                Log.e(HwDualBandMessageUtil.TAG, "ACTION_NETWORK_CONDITIONS_MEASURED");
                int isInternetResult = intent.getIntExtra("extra_is_internet_ready", -1);
                if (isInternetResult == -1) {
                    Log.e(HwDualBandMessageUtil.TAG, "INTERNET_CHECK_RESULT_NO_INTERNET");
                    HwDualBandWiFiMonitor.this.mHandler.sendEmptyMessage(12);
                } else if (isInternetResult == 5) {
                    HwDualBandWiFiMonitor.this.mHandler.sendEmptyMessage(11);
                } else if (isInternetResult == 6) {
                    HwDualBandWiFiMonitor.this.mHandler.sendEmptyMessage(13);
                }
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwDualBandWiFiMonitor.this.mHandler.sendEmptyMessage(14);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwDualBandWiFiMonitor.this.mHandler.sendEmptyMessage(15);
            } else {
                Log.i(HwDualBandMessageUtil.TAG, "action:" + action);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCfgNetChgAction(Intent intent) {
        int reason = intent.getIntExtra("changeReason", 4);
        Log.e(HwDualBandMessageUtil.TAG, "CONFIGURED_NETWORKS_CHANGED_ACTION reason = " + reason);
        if (reason == 1) {
            WifiConfiguration netInfo = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
            Log.e(HwDualBandMessageUtil.TAG, "CONFIGURED_NETWORKS_CHANGED_ACTION");
            if (isValid(netInfo) && !netInfo.isTempCreated) {
                Log.e(HwDualBandMessageUtil.TAG, "CHANGE_REASON_REMOVED");
                Bundle data = new Bundle();
                data.putString("bssid", netInfo.BSSID);
                data.putString("ssid", netInfo.SSID);
                data.putInt(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE, netInfo.getAuthType());
                Message msg = Message.obtain();
                msg.what = 8;
                msg.setData(data);
                this.mHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetStateChangedAction(String action, NetworkInfo netInfo) {
        if (netInfo == null) {
            return;
        }
        if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
            this.mHandler.sendEmptyMessage(2);
        } else if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
            Log.e(HwDualBandMessageUtil.TAG, "NetworkInfo.State.CONNECTED");
            this.mHandler.sendEmptyMessage(1);
        } else if (netInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            Log.e(HwDualBandMessageUtil.TAG, "NetworkInfo.DetailedState.VERIFYING_POOR_LINK");
            this.mHandler.sendEmptyMessage(19);
        } else {
            Log.i(HwDualBandMessageUtil.TAG, "action:" + action);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiStateChangeAction(Intent intent) {
        int wifistatue = intent.getIntExtra("wifi_state", 4);
        if (wifistatue == 1) {
            Log.e(HwDualBandMessageUtil.TAG, "WIFI_STATE_DISABLED");
            this.mHandler.sendEmptyMessage(4);
        } else if (wifistatue == 3) {
            Log.e(HwDualBandMessageUtil.TAG, "WIFI_STATE_ENABLED");
            this.mHandler.sendEmptyMessage(3);
        }
    }

    private boolean isValid(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        int cc = config.allowedKeyManagement.cardinality();
        Log.e(HwDualBandMessageUtil.TAG, "config isValid cardinality=" + cc);
        if (cc <= 1) {
            return true;
        }
        return false;
    }
}
