package com.android.server.wifi.ABS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;

public class HwABSWiFiScenario {
    private IntentFilter intentFilter;
    private boolean isRegister;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private Handler mHandler;

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private WifiBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                switch (intent.getIntExtra("wifi_state", 4)) {
                    case WifiScanGenieController.MSG_CONFIGURED_CHANGED /*1*/:
                        HwABSUtils.logD("WifiBroadcastReceiver WIFI_STATE_DISABLED");
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(4);
                    case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                        HwABSUtils.logD("WifiBroadcastReceiver WIFI_STATE_ENABLED");
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(3);
                    default:
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo == null) {
                    return;
                }
                if (netInfo.getState() == State.DISCONNECTED) {
                    HwABSUtils.logD("NetworkInfo.State.DISCONNECTED");
                    HwABSWiFiScenario.this.mHandler.sendEmptyMessage(2);
                } else if (netInfo.getState() == State.CONNECTED) {
                    HwABSUtils.logD("NetworkInfo.State.CONNECTED");
                    HwABSWiFiScenario.this.mHandler.sendEmptyMessage(1);
                }
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwABSUtils.logD("ACTION_SCREEN_ON");
                HwABSWiFiScenario.this.mHandler.sendEmptyMessage(5);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwABSUtils.logD("ACTION_SCREEN_OFF");
                HwABSWiFiScenario.this.mHandler.sendEmptyMessage(6);
            }
        }
    }

    public HwABSWiFiScenario(Context context, Handler handler) {
        this.isRegister = false;
        this.intentFilter = new IntentFilter();
        this.mBroadcastReceiver = new WifiBroadcastReceiver();
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
            HwABSUtils.logD("HwABSWiFiScenario registerBroadcastReceiver");
            this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
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
}
