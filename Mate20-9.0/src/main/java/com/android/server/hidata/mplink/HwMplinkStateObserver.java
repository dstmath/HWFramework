package com.android.server.hidata.mplink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.ServiceState;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.android.net.hwmplink.MpLinkCommonUtils;

public class HwMplinkStateObserver {
    private static final String TAG = "HiData_HwMplinkStateObserver";
    /* access modifiers changed from: private */
    public IMpLinkStateObserverCallback mCallback;
    /* access modifiers changed from: private */
    public Context mContext;

    private class StateBroadcastReceiver extends BroadcastReceiver {
        private StateBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MpLinkCommonUtils.logI(HwMplinkStateObserver.TAG, "action:" + action);
            if ("android.intent.action.SERVICE_STATE".equals(action)) {
                HwMplinkStateObserver.this.mCallback.onTelephonyServiceStateChanged(ServiceState.newFromBundle(intent.getExtras()), intent.getIntExtra("subscription", -1));
            } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                HwMplinkStateObserver.this.mCallback.onTelephonyDefaultDataSubChanged(intent.getIntExtra("subscription", -1));
            } else if ("android.intent.action.ANY_DATA_STATE".equals(action)) {
                String apnType = intent.getStringExtra("apnType");
                MpLinkCommonUtils.logD(HwMplinkStateObserver.TAG, "apnType:" + apnType);
                if (MemoryConstant.MEM_SCENE_DEFAULT.equals(apnType)) {
                    HwMplinkStateObserver.this.mCallback.onTelephonyDataConnectionChanged(intent.getStringExtra("state"), intent.getStringExtra("iface"), intent.getIntExtra("subscription", -1));
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                HwMplinkStateObserver.this.mCallback.onWifiNetworkStateChanged((NetworkInfo) intent.getParcelableExtra("networkInfo"));
            } else if ("mplink_intent_check_request_success".equals(action)) {
                HwMplinkStateObserver.this.mCallback.onMpLinkRequestTimeout(intent.getIntExtra("mplink_intent_key_check_request", -1));
            }
        }
    }

    public HwMplinkStateObserver(Context context, IMpLinkStateObserverCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        registerMpLinkReceiver();
        registerMplinkSettingsChanges();
    }

    private void registerMpLinkReceiver() {
        IntentFilter filter = new IntentFilter();
        StateBroadcastReceiver receiver = new StateBroadcastReceiver();
        filter.addAction("android.intent.action.SERVICE_STATE");
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        filter.addAction("android.intent.action.ANY_DATA_STATE");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("mplink_intent_check_request_success");
        this.mContext.registerReceiver(receiver, filter);
    }

    private void registerMplinkSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_network_switching"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                HwMplinkStateObserver.this.mCallback.onMplinkSwitchChange(HwMplinkStateObserver.this.getMpLinkSwitchState());
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("wifipro_network_vpn_state"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                HwMplinkStateObserver.this.mCallback.onVpnStateChange(HwMplinkStateObserver.this.getVpnConnectState());
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("mplink_simulate_hibrain_request_for_test"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                HwMplinkStateObserver.this.mCallback.onSimulateHiBrainRequestForDemo(MpLinkCommonUtils.getSettingsSystemBoolean(HwMplinkStateObserver.this.mContext.getContentResolver(), "mplink_simulate_hibrain_request_for_test", false));
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                HwMplinkStateObserver.this.mCallback.onMobileDataSwitchChange(MpLinkCommonUtils.getSettingsGlobalBoolean(HwMplinkStateObserver.this.mContext.getContentResolver(), "mobile_data", false));
            }
        });
    }

    public void initSimulateHibrain() {
        Settings.System.putInt(this.mContext.getContentResolver(), "mplink_simulate_hibrain_request_for_test", 0);
    }

    public boolean getMpLinkSwitchState() {
        return MpLinkCommonUtils.isMpLinkEnabled(this.mContext);
    }

    public boolean getVpnConnectState() {
        return MpLinkCommonUtils.getSettingsSystemBoolean(this.mContext.getContentResolver(), "wifipro_network_vpn_state", false);
    }
}
