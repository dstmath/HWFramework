package com.android.server.hidata.appqoe;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class HwAPPQoESystemStateMonitor {
    /* access modifiers changed from: private */
    public static String TAG = "HiData_HwAPPQoESystemStateMonitor";
    public HwAPPStateInfo curAPPStateInfo = new HwAPPStateInfo();
    private IntentFilter emcomIntentFilter = new IntentFilter();
    private IntentFilter hidataIntentFilter = new IntentFilter();
    private IntentFilter intentFilter = new IntentFilter();
    private NetworkInfo mActiveNetworkInfo;
    private BroadcastReceiver mBroadcastReceiver = new SystemBroadcastReceiver();
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    private int mConnectivityType = 802;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public ContentResolver mResolver;
    private UserDataEnableObserver mUserDataEnableObserver;

    private class SystemBroadcastReceiver extends BroadcastReceiver {
        private SystemBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                int wifistatus = intent.getIntExtra("wifi_state", 4);
                if (wifistatus == 1) {
                    HwAPPQoEUtils.logE("WIFI_STATE_DISABLED");
                } else if (wifistatus == 3) {
                    HwAPPQoEUtils.logE("WIFI_STATE_ENABLED");
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, "Wifi Connection State Changed");
                HwAPPQoESystemStateMonitor.this.onReceiveWifiStateChanged(intent);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, "ACTION_BOOT_COMPLETED");
                ConnectivityManager unused = HwAPPQoESystemStateMonitor.this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                HwAPPQoESystemStateMonitor.this.onConnectivityNetworkChange();
                HwAPPQoESystemStateMonitor.this.mHandler.sendEmptyMessage(200);
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, "Connectivity Changed");
                HwAPPQoESystemStateMonitor.this.onConnectivityNetworkChange();
            } else if ("com.android.server.hidata.arbitration.HwArbitrationStateMachine".equals(action)) {
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, " HwArbitrationStateMachine broadcast test");
                int network = intent.getIntExtra("MPLinkSuccessNetworkKey", 802);
                if (intent.getIntExtra("MPLinkSuccessUIDKey", -1) == HwAPPQoESystemStateMonitor.this.curAPPStateInfo.mAppUID) {
                    String access$100 = HwAPPQoESystemStateMonitor.TAG;
                    HwAPPQoEUtils.logD(access$100, "MPLINK_STATE_CHANGE:" + network);
                    HwAPPQoESystemStateMonitor.this.mHandler.sendMessage(HwAPPQoESystemStateMonitor.this.mHandler.obtainMessage(201, Integer.valueOf(network)));
                }
            } else if (HwAPPQoEUtils.EMCOM_PARA_READY_ACTION.equals(action)) {
                int cotaParaBitRec = intent.getIntExtra(HwAPPQoEUtils.EMCOM_PARA_READY_REC, 0);
                String access$1002 = HwAPPQoESystemStateMonitor.TAG;
                HwAPPQoEUtils.logD(access$1002, "emcom para is ready: cotaParaBitRec:" + cotaParaBitRec);
                if ((cotaParaBitRec & 16) == 0) {
                    HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, "broadcast is not for no cell");
                    return;
                }
                HwAPPQoEResourceManger mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
                if (mHwAPPQoEResourceManger != null) {
                    mHwAPPQoEResourceManger.onConfigFilePathChanged();
                }
            } else if ("android.net.wifi.RSSI_CHANGED".equals(action) || "android.intent.action.SCREEN_ON".equals(action)) {
                HwAPPQoESystemStateMonitor.this.startWeakNetworkMonitor();
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwAPPQoESystemStateMonitor.this.stopWeakNetworkMonitor();
            }
        }
    }

    private class UserDataEnableObserver extends ContentObserver {
        public UserDataEnableObserver(Handler handler) {
            super(handler);
            ContentResolver unused = HwAPPQoESystemStateMonitor.this.mResolver = HwAPPQoESystemStateMonitor.this.mContext.getContentResolver();
        }

        public void onChange(boolean selfChange) {
            int state = Settings.Global.getInt(HwAPPQoESystemStateMonitor.this.mContext.getContentResolver(), "mobile_data", -1);
            String access$100 = HwAPPQoESystemStateMonitor.TAG;
            HwAPPQoEUtils.logD(access$100, "User change Data service state = " + state);
            if (state == 0 && HwArbitrationFunction.isInMPLink(HwAPPQoESystemStateMonitor.this.mContext, HwAPPQoESystemStateMonitor.this.curAPPStateInfo.mAppUID)) {
                HwAPPQoEUserAction mHwAPPQoEUserAction = HwAPPQoEUserAction.getInstance();
                if (mHwAPPQoEUserAction != null) {
                    mHwAPPQoEUserAction.resetUserActionType(HwAPPQoESystemStateMonitor.this.curAPPStateInfo.mAppId);
                    HwAPPQoESystemStateMonitor.this.mHandler.sendMessage(HwAPPQoESystemStateMonitor.this.mHandler.obtainMessage(203, 2, -1));
                }
            }
        }

        public void register() {
            HwAPPQoESystemStateMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, this);
        }

        public void unregister() {
            HwAPPQoESystemStateMonitor.this.mResolver.unregisterContentObserver(this);
        }
    }

    public HwAPPQoESystemStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        registerBroadcastReceiver();
        this.mUserDataEnableObserver = new UserDataEnableObserver(handler);
        this.mUserDataEnableObserver.register();
    }

    private void registerBroadcastReceiver() {
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.intentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
        this.hidataIntentFilter.addAction("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.hidataIntentFilter, "com.huawei.hidata.permission.MPLINK_START_CHECK", null);
        this.emcomIntentFilter.addAction(HwAPPQoEUtils.EMCOM_PARA_READY_ACTION);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.emcomIntentFilter, HwAPPQoEUtils.EMCOM_PARA_UPGRADE_PERMISSION, null);
    }

    /* access modifiers changed from: private */
    public void onReceiveWifiStateChanged(Intent intent) {
        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
            HwAPPChrManager.getInstance().uploadAppChrInfo();
        }
    }

    /* access modifiers changed from: private */
    public void onConnectivityNetworkChange() {
        if (this.mConnectivityManager != null) {
            this.mActiveNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
            if (this.mActiveNetworkInfo == null) {
                String str = TAG;
                HwAPPQoEUtils.logD(str, "onConnectivityNetworkChange-prev_type is:" + this.mConnectivityType);
                if (800 == this.mConnectivityType) {
                    this.mHandler.sendEmptyMessage(4);
                } else if (801 == this.mConnectivityType) {
                    this.mHandler.sendEmptyMessage(8);
                }
                this.mConnectivityType = 802;
            } else if (1 == this.mActiveNetworkInfo.getType()) {
                if (true == this.mActiveNetworkInfo.isConnected()) {
                    HwAPPQoEUtils.logD(TAG, "onConnectivityNetworkChange, MSG_WIFI_STATE_CONNECTED");
                    this.mHandler.sendEmptyMessage(3);
                    this.mConnectivityType = 800;
                } else {
                    String str2 = TAG;
                    HwAPPQoEUtils.logD(str2, "onConnectivityNetworkChange-Wifi:" + this.mActiveNetworkInfo.getState());
                }
            } else if (this.mActiveNetworkInfo.getType() == 0) {
                if (true == this.mActiveNetworkInfo.isConnected()) {
                    HwAPPQoEUtils.logD(TAG, "onConnectivityNetworkChange, MSG_CELL_STATE_CONNECTED");
                    this.mHandler.sendEmptyMessage(7);
                    this.mConnectivityType = 801;
                } else {
                    String str3 = TAG;
                    HwAPPQoEUtils.logD(str3, "onConnectivityNetworkChange-Cell:" + this.mActiveNetworkInfo.getState());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void startWeakNetworkMonitor() {
        if (this.curAPPStateInfo != null && this.curAPPStateInfo.mAppId != -1) {
            String str = TAG;
            HwAPPQoEUtils.logD(str, "system monitor curAPPStateInfo.mAppId = " + this.curAPPStateInfo.mAppId);
            WifiInfo info = ((WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE)).getConnectionInfo();
            if (info != null) {
                int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(info.getFrequency(), info.getRssi());
                if (rssiLevel <= 1 && !this.mHandler.hasMessages(109)) {
                    HwAPPQoEUtils.logD(TAG, "system monitor send weak network message");
                    this.mHandler.sendEmptyMessageDelayed(109, 4000);
                } else if (rssiLevel >= 2 && this.mHandler.hasMessages(109)) {
                    HwAPPQoEUtils.logD(TAG, "system monitor remove weak network message");
                    this.mHandler.removeMessages(109);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void stopWeakNetworkMonitor() {
        HwAPPQoEUtils.logD(TAG, "system monitor stopWeakNetworkMonitor");
        this.mHandler.removeMessages(109);
    }

    public boolean isDefaultApnType(String apnType) {
        return MemoryConstant.MEM_SCENE_DEFAULT.equals(apnType);
    }

    public boolean isSlotIdValid(int slotId) {
        return slotId >= 0 && 2 > slotId;
    }

    public boolean getMpLinkState() {
        return HwArbitrationFunction.isInMPLink(this.mContext, this.curAPPStateInfo.mAppUID);
    }
}
