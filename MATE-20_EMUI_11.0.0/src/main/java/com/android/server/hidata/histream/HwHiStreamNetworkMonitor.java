package com.android.server.hidata.histream;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.lcagent.client.LogCollectManager;

/* access modifiers changed from: package-private */
public class HwHiStreamNetworkMonitor {
    public static final int EVENT_CONNECTIVITY_CHANGE = 3;
    public static final int EVENT_MOBILE_DATA_DISABLED = 2;
    public static final int EVENT_WIFI_DISABLED = 1;
    private static HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private String mCurrBSSID = null;
    private Handler mHandler;
    private IntentFilter mIntentFilter;
    public long mLastCellDisableTime = 0;
    public long mLastHandoverTime = 0;
    public long mLastWifiDisabledTime = 0;
    public long mLastWifiEnabledTime = 0;
    private LogCollectManager mLogCollectManager;
    private NetworkInfo mNetworkInfo;
    private ContentResolver mResolver;
    private TelephonyManager mTelephonyManager;
    private UserDataEnableObserver mUserDataEnableObserver;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;

    private HwHiStreamNetworkMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        registerBroadcastReceiver();
        this.mUserDataEnableObserver = new UserDataEnableObserver(this.mHandler);
        this.mUserDataEnableObserver.register();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mLogCollectManager = new LogCollectManager(this.mContext);
    }

    public static HwHiStreamNetworkMonitor createInstance(Context context, Handler handler) {
        if (mHwHiStreamNetworkMonitor == null) {
            mHwHiStreamNetworkMonitor = new HwHiStreamNetworkMonitor(context, handler);
        }
        return mHwHiStreamNetworkMonitor;
    }

    public static HwHiStreamNetworkMonitor getInstance() {
        return mHwHiStreamNetworkMonitor;
    }

    public int getCurrNetworkType(int uid) {
        return HwArbitrationFunction.getCurrentNetwork(this.mContext, uid);
    }

    public String getCurBSSID() {
        if (this.mCurrBSSID == null) {
            this.mWifiInfo = this.mWifiManager.getConnectionInfo();
            WifiInfo wifiInfo = this.mWifiInfo;
            if (wifiInfo != null) {
                this.mCurrBSSID = wifiInfo.getBSSID();
            }
        }
        return this.mCurrBSSID;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiConnected() {
        this.mWifiInfo = this.mWifiManager.getConnectionInfo();
        WifiInfo wifiInfo = this.mWifiInfo;
        if (wifiInfo != null) {
            this.mCurrBSSID = wifiInfo.getBSSID();
        }
    }

    public boolean getMoblieDateSettings() {
        return getSettingsGlobalBoolean(this.mContext.getContentResolver(), "mobile_data", false);
    }

    private boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.Global.getInt(cr, name, def ? 1 : 0) == 1;
    }

    private void registerBroadcastReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.hidata.histream.HwHiStreamNetworkMonitor.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED.equals(action)) {
                        HwHiStreamNetworkMonitor.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (HwHiStreamNetworkMonitor.this.mNetworkInfo != null && HwHiStreamNetworkMonitor.this.mNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            HwHiStreamNetworkMonitor.this.handleWifiConnected();
                        }
                    } else if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("event", 3);
                        HwHiStreamNetworkMonitor.this.mHandler.sendMessageDelayed(HwHiStreamNetworkMonitor.this.mHandler.obtainMessage(3, bundle), 500);
                    } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                        int wifistatue = intent.getIntExtra("wifi_state", 4);
                        if (1 == wifistatue) {
                            HwHiStreamNetworkMonitor.this.mLastWifiDisabledTime = System.currentTimeMillis();
                            Bundle bundle2 = new Bundle();
                            bundle2.putInt("event", 1);
                            HwHiStreamNetworkMonitor.this.mHandler.sendMessage(HwHiStreamNetworkMonitor.this.mHandler.obtainMessage(3, bundle2));
                        } else if (3 == wifistatue) {
                            HwHiStreamUtils.logD(false, "+++++++WIFI enabled ++++++", new Object[0]);
                            HwHiStreamNetworkMonitor.this.mLastWifiEnabledTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED);
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    public String getCurrentDefaultDataImsi() {
        String mCurrentDefaultDataImsi = this.mTelephonyManager.getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
        LogCollectManager logCollectManager = this.mLogCollectManager;
        if (logCollectManager == null || mCurrentDefaultDataImsi == null) {
            return mCurrentDefaultDataImsi;
        }
        try {
            return logCollectManager.doEncrypt(mCurrentDefaultDataImsi);
        } catch (RemoteException e) {
            HwHiStreamUtils.logE(false, "getCurrentDefaultDataImsi doEncrypt error:%{public}s", e.getMessage());
            return null;
        }
    }

    public boolean isUserDataEnabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) != 0;
    }

    private class UserDataEnableObserver extends ContentObserver {
        public UserDataEnableObserver(Handler handler) {
            super(handler);
            HwHiStreamNetworkMonitor.this.mResolver = HwHiStreamNetworkMonitor.this.mContext.getContentResolver();
        }

        public void register() {
            HwHiStreamNetworkMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, this);
        }

        public void unregister() {
            HwHiStreamNetworkMonitor.this.mResolver.unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            if (!HwHiStreamNetworkMonitor.this.isUserDataEnabled()) {
                HwHiStreamNetworkMonitor.this.mLastCellDisableTime = System.currentTimeMillis();
                Bundle bundle = new Bundle();
                bundle.putInt("event", 2);
                HwHiStreamNetworkMonitor.this.mHandler.sendMessage(HwHiStreamNetworkMonitor.this.mHandler.obtainMessage(3, bundle));
            }
        }
    }
}
