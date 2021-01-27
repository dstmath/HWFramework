package com.android.server.wifi.ABS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.os.Handler;

public class HwAbsWiFiScenario {
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private Context mContext;
    private SupplicantState mCurrentSupplicantState = SupplicantState.INTERFACE_DISABLED;
    private Handler mHandler;
    private IntentFilter mIntentFilter = new IntentFilter();
    private boolean mIsRegister = false;

    public HwAbsWiFiScenario(Context context, Handler handler) {
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
        if (this.mIsRegister) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mIsRegister = false;
        }
    }

    private void registerBroadcastReceiver() {
        if (!this.mIsRegister) {
            HwAbsUtils.logD(false, "HwAbsWiFiScenario registerBroadcastReceiver", new Object[0]);
            this.mIntentFilter.addAction("android.intent.action.SCREEN_ON");
            this.mIntentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
            this.mIsRegister = true;
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
                    HwAbsWiFiScenario.this.handleWifiStateChanged(intent);
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    HwAbsWiFiScenario.this.handleNetWorkStateChanged(intent);
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    HwAbsUtils.logD(false, "ACTION_SCREEN_ON", new Object[0]);
                    HwAbsWiFiScenario.this.mHandler.sendEmptyMessage(5);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    HwAbsUtils.logD(false, "ACTION_SCREEN_OFF", new Object[0]);
                    HwAbsWiFiScenario.this.mHandler.sendEmptyMessage(6);
                } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    HwAbsWiFiScenario.this.handleSupplicantStateChanged(intent);
                } else {
                    HwAbsUtils.logD(false, "No processing type", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiStateChanged(Intent intent) {
        int wifistatue = intent.getIntExtra("wifi_state", 4);
        if (wifistatue == 1) {
            HwAbsUtils.logD(false, "WifiBroadcastReceiver WIFI_STATE_DISABLED", new Object[0]);
            this.mHandler.sendEmptyMessage(4);
        } else if (wifistatue == 3) {
            HwAbsUtils.logD(false, "WifiBroadcastReceiver WIFI_STATE_ENABLED", new Object[0]);
            this.mHandler.sendEmptyMessage(3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetWorkStateChanged(Intent intent) {
        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (netInfo == null) {
            return;
        }
        if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
            HwAbsUtils.logD(false, "NetworkInfo.State.DISCONNECTED", new Object[0]);
            this.mHandler.sendEmptyMessage(2);
        } else if (netInfo.getState() == NetworkInfo.State.CONNECTED || netInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            HwAbsUtils.logD(false, "NetworkInfo.State.CONNECTED", new Object[0]);
            this.mHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSupplicantStateChanged(Intent intent) {
        if (intent.getParcelableExtra("newState") != null && (intent.getParcelableExtra("newState") instanceof SupplicantState)) {
            this.mCurrentSupplicantState = (SupplicantState) intent.getParcelableExtra("newState");
            HwAbsUtils.logD(false, "SUPPLICANT_STATE_CHANGED_ACTION mCurrentSupplicantState = %{public}d", this.mCurrentSupplicantState);
            if (this.mCurrentSupplicantState == SupplicantState.COMPLETED) {
                this.mHandler.sendEmptyMessage(24);
            }
        }
    }

    public boolean isSupInCompleteState() {
        return true;
    }
}
