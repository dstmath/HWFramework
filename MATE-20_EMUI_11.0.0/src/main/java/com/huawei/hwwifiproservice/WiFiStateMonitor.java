package com.huawei.hwwifiproservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.wifi.HwHiLog;
import java.util.List;

public class WiFiStateMonitor {
    private static final String ASSOCIATION_REJECT_STATUS_CODE = "wifi_association_reject_status_code";
    private static final int ASSOC_REJECT_COMMON = 1;
    private static final int ASSOC_REJECT_SERVER_FULL = 17;
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isRegister = false;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private Context mContext;
    private String mCurrentBSSID;
    private WifiConfiguration mCurrentConfigs;
    private String mCurrentSSID;
    private Handler mHandler;
    private WifiManager mWifiManager;

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

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private WifiBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                int wifistatue = intent.getIntExtra("wifi_state", 4);
                if (wifistatue == 1) {
                    HwHiLog.i(MessageUtil.TAG, false, "WIFI_STATE_DISABLED", new Object[0]);
                    WiFiStateMonitor.this.mHandler.sendEmptyMessage(4);
                } else if (wifistatue == 3) {
                    HwHiLog.i(MessageUtil.TAG, false, "WIFI_STATE_ENABLED", new Object[0]);
                    WiFiStateMonitor.this.mHandler.sendEmptyMessage(3);
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                WiFiStateMonitor.this.handleNetworkStateChanged(intent);
            } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(7);
            } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)) {
                WiFiStateMonitor.this.handleConfiguredNetworkChange(intent);
            } else if ("huawei.conn.NETWORK_CONDITIONS_MEASURED".equals(action)) {
                WiFiStateMonitor.this.handleNetworkConditionsMeasured(intent);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(21);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                WiFiStateMonitor.this.mHandler.sendEmptyMessage(22);
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                NetworkInfo networkInfo = null;
                Object tempNetworkInfo = intent.getParcelableExtra("networkInfo");
                if (tempNetworkInfo instanceof NetworkInfo) {
                    networkInfo = (NetworkInfo) tempNetworkInfo;
                } else {
                    HwHiLog.w(MessageUtil.TAG, false, "networkInfo is not match the class", new Object[0]);
                }
                if (networkInfo == null || !networkInfo.isConnected()) {
                    WiFiStateMonitor.this.mHandler.sendEmptyMessage(15);
                } else {
                    WiFiStateMonitor.this.mHandler.sendEmptyMessage(14);
                }
            } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                WiFiStateMonitor.this.handleSupplicantStateChange(context);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkConditionsMeasured(Intent intent) {
        HwHiLog.i(MessageUtil.TAG, false, "ACTION_NETWORK_CONDITIONS_MEASURED", new Object[0]);
        int isInternetResult = intent.getIntExtra("extra_is_internet_ready", -1);
        if (isInternetResult == -1) {
            HwHiLog.i(MessageUtil.TAG, false, "INTERNET_CHECK_RESULT_NO_INTERNET", new Object[0]);
            this.mHandler.sendEmptyMessage(12);
        } else if (isInternetResult == 5) {
            this.mHandler.sendEmptyMessage(11);
        } else if (isInternetResult == 6) {
            this.mHandler.sendEmptyMessage(13);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConfiguredNetworkChange(Intent intent) {
        WifiConfiguration netInfo;
        HwHiLog.d(MessageUtil.TAG, false, "CONFIGURED_NETWORKS_CHANGED_ACTION", new Object[0]);
        if (intent.getIntExtra("changeReason", 4) == 1 && (netInfo = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration")) != null && !netInfo.isTempCreated) {
            HwHiLog.i(MessageUtil.TAG, false, "CHANGE_REASON_REMOVED", new Object[0]);
            Bundle data = new Bundle();
            data.putString("bssid", netInfo.BSSID);
            data.putString("ssid", netInfo.SSID);
            Message msg = Message.obtain();
            msg.what = 8;
            msg.setData(data);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkStateChanged(Intent intent) {
        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (netInfo == null) {
            return;
        }
        if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
            this.mHandler.sendEmptyMessage(2);
        } else if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
            HwHiLog.i(MessageUtil.TAG, false, "NetworkInfo.State.CONNECTED", new Object[0]);
            this.mHandler.sendEmptyMessage(1);
        } else if (netInfo.getState() == NetworkInfo.State.CONNECTING) {
            HwHiLog.i(MessageUtil.TAG, false, "NetworkInfo.State.CONNECTING", new Object[0]);
            this.mHandler.sendEmptyMessage(27);
            WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
            List<WifiConfiguration> configs = WifiproUtils.getAllConfiguredNetworks();
            if (!(mWifiInfo == null || configs == null)) {
                this.mCurrentBSSID = mWifiInfo.getBSSID();
                this.mCurrentSSID = mWifiInfo.getSSID();
                for (WifiConfiguration config : configs) {
                    if (config != null && config.networkId == mWifiInfo.getNetworkId()) {
                        this.mCurrentConfigs = config;
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSupplicantStateChange(Context context) {
        WifiConfiguration errorConfig = this.mCurrentConfigs;
        if (errorConfig != null) {
            WifiConfiguration.NetworkSelectionStatus errorStatus = errorConfig.getNetworkSelectionStatus();
            int disableReason = errorStatus.getNetworkSelectionDisableReason();
            if ((errorConfig.status == 1 && disableReason > 0) || !errorStatus.isNetworkEnabled()) {
                HwHiLog.e(MessageUtil.TAG, false, "auto open connect failed", new Object[0]);
                if (errorStatus.isNetworkEnabled()) {
                    handleErrorStatusIsNetworkEnabled(context, disableReason);
                } else if (disableReason == 4) {
                    sendConnectFailedReason(2);
                } else if (disableReason == 11) {
                    sendConnectFailedReason(0);
                } else if (disableReason == 2) {
                    HwHiLog.w(MessageUtil.TAG, false, "DISABLED_ASSOCIATION_REJECT", new Object[0]);
                    if (Settings.System.getInt(context.getContentResolver(), ASSOCIATION_REJECT_STATUS_CODE, 1) == 17) {
                        HwHiLog.w(MessageUtil.TAG, false, "ASSOC_REJECT_SERVER_FULL", new Object[0]);
                        sendConnectFailedReason(3);
                        return;
                    }
                    sendConnectFailedReason(4);
                } else if (disableReason == 3) {
                    sendConnectFailedReason(1);
                }
            }
        }
    }

    private void handleErrorStatusIsNetworkEnabled(Context context, int disableReason) {
        if (disableReason == -1) {
            sendConnectFailedReason(0);
        } else if (disableReason == 2) {
            HwHiLog.w(MessageUtil.TAG, false, "DISABLED_ASSOCIATION_REJECT", new Object[0]);
            if (Settings.System.getInt(context.getContentResolver(), ASSOCIATION_REJECT_STATUS_CODE, 1) == 17) {
                sendConnectFailedReason(3);
            } else {
                sendConnectFailedReason(4);
            }
        } else if (disableReason == 3) {
            sendConnectFailedReason(1);
        } else if (disableReason == 4) {
            sendConnectFailedReason(2);
        } else if (disableReason == 5) {
            sendConnectFailedReason(5);
        }
    }

    private void sendConnectFailedReason(int reason) {
        Bundle data = new Bundle();
        data.putInt("reason", reason);
        data.putString("bssid", this.mCurrentBSSID);
        data.putString("ssid", this.mCurrentSSID);
        Message msg = Message.obtain();
        msg.what = 24;
        msg.setData(data);
        this.mHandler.sendMessage(msg);
    }
}
