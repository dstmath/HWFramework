package com.android.server.wifi.ABS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.os.Handler;

public class HwABSWiFiScenario {
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isRegister = false;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private Context mContext;
    private SupplicantState mCurrentsupplicantState = SupplicantState.INTERFACE_DISABLED;
    private Handler mHandler;

    public HwABSWiFiScenario(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
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
            HwABSUtils.logD(false, "HwABSWiFiScenario registerBroadcastReceiver", new Object[0]);
            this.intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.intentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
            this.isRegister = true;
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
                    int wifistatue = intent.getIntExtra("wifi_state", 4);
                    if (wifistatue == 1) {
                        HwABSUtils.logD(false, "WifiBroadcastReceiver WIFI_STATE_DISABLED", new Object[0]);
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(4);
                    } else if (wifistatue == 3) {
                        HwABSUtils.logD(false, "WifiBroadcastReceiver WIFI_STATE_ENABLED", new Object[0]);
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(3);
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (netInfo == null) {
                        return;
                    }
                    if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        HwABSUtils.logD(false, "NetworkInfo.State.DISCONNECTED", new Object[0]);
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(2);
                    } else if (netInfo.getState() == NetworkInfo.State.CONNECTED || netInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                        HwABSUtils.logD(false, "NetworkInfo.State.CONNECTED", new Object[0]);
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(1);
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    HwABSUtils.logD(false, "ACTION_SCREEN_ON", new Object[0]);
                    HwABSWiFiScenario.this.mHandler.sendEmptyMessage(5);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    HwABSUtils.logD(false, "ACTION_SCREEN_OFF", new Object[0]);
                    HwABSWiFiScenario.this.mHandler.sendEmptyMessage(6);
                } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action) && intent.getParcelableExtra("newState") != null && (intent.getParcelableExtra("newState") instanceof SupplicantState)) {
                    HwABSWiFiScenario.this.mCurrentsupplicantState = (SupplicantState) intent.getParcelableExtra("newState");
                    HwABSUtils.logD(false, "SUPPLICANT_STATE_CHANGED_ACTION mCurrentsupplicantState = %{public}d", HwABSWiFiScenario.this.mCurrentsupplicantState);
                    if (HwABSWiFiScenario.this.mCurrentsupplicantState == SupplicantState.COMPLETED) {
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(24);
                    }
                }
            }
        }
    }

    public boolean isSupInCompleteState() {
        return true;
    }
}
