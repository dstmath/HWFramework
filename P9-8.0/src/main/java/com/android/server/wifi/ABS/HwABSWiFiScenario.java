package com.android.server.wifi.ABS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.SupplicantState;
import android.os.Handler;

public class HwABSWiFiScenario {
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isRegister = false;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver(this, null);
    private Context mContext;
    private SupplicantState mCurrentsupplicantState = SupplicantState.INTERFACE_DISABLED;
    private Handler mHandler;

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ WifiBroadcastReceiver(HwABSWiFiScenario this$0, WifiBroadcastReceiver -this1) {
            this();
        }

        private WifiBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                switch (intent.getIntExtra("wifi_state", 4)) {
                    case 1:
                        HwABSUtils.logD("WifiBroadcastReceiver WIFI_STATE_DISABLED");
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(4);
                        return;
                    case 3:
                        HwABSUtils.logD("WifiBroadcastReceiver WIFI_STATE_ENABLED");
                        HwABSWiFiScenario.this.mHandler.sendEmptyMessage(3);
                        return;
                    default:
                        return;
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo == null) {
                    return;
                }
                if (netInfo.getState() == State.DISCONNECTED) {
                    HwABSUtils.logD("NetworkInfo.State.DISCONNECTED");
                    HwABSWiFiScenario.this.mHandler.sendEmptyMessage(2);
                } else if (netInfo.getState() == State.CONNECTED || netInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                    HwABSUtils.logD("NetworkInfo.State.CONNECTED");
                    HwABSWiFiScenario.this.mHandler.sendEmptyMessage(1);
                }
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwABSUtils.logD("ACTION_SCREEN_ON");
                HwABSWiFiScenario.this.mHandler.sendEmptyMessage(5);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwABSUtils.logD("ACTION_SCREEN_OFF");
                HwABSWiFiScenario.this.mHandler.sendEmptyMessage(6);
            } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action) && intent.getParcelableExtra("newState") != null && (intent.getParcelableExtra("newState") instanceof SupplicantState)) {
                HwABSWiFiScenario.this.mCurrentsupplicantState = (SupplicantState) intent.getParcelableExtra("newState");
                HwABSUtils.logD("SUPPLICANT_STATE_CHANGED_ACTION mCurrentsupplicantState = " + HwABSWiFiScenario.this.mCurrentsupplicantState);
                if (HwABSWiFiScenario.this.mCurrentsupplicantState == SupplicantState.COMPLETED) {
                    HwABSWiFiScenario.this.mHandler.sendEmptyMessage(24);
                }
            }
        }
    }

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
            HwABSUtils.logD("HwABSWiFiScenario registerBroadcastReceiver");
            this.intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.intentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
            this.isRegister = true;
        }
    }

    public boolean isSupInCompleteState() {
        return true;
    }
}
