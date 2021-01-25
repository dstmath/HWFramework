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
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.intellicom.common.SmartDualCardConsts;

public class HwAPPQoESystemStateMonitor {
    private static String TAG = "HiData_HwAPPQoESystemStateMonitor";
    public HwAPPStateInfo curAPPStateInfo = new HwAPPStateInfo();
    private IntentFilter emcomIntentFilter = new IntentFilter();
    private IntentFilter intentFilter = new IntentFilter();
    private NetworkInfo mActiveNetworkInfo;
    private BroadcastReceiver mBroadcastReceiver = new SystemBroadcastReceiver();
    private ConnectivityManager mConnectivityManager;
    private int mConnectivityType = 802;
    private Context mContext;
    private Handler mHandler;
    private ContentResolver mResolver;
    private UserDataEnableObserver mUserDataEnableObserver;

    public HwAPPQoESystemStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        registerBroadcastReceiver();
        this.mUserDataEnableObserver = new UserDataEnableObserver(handler);
        this.mUserDataEnableObserver.register();
    }

    private void registerBroadcastReceiver() {
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.intentFilter.addAction("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
        this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED);
        this.intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.intentFilter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        this.intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        this.intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter, "com.huawei.hidata.permission.MPLINK_START_CHECK", null);
        this.emcomIntentFilter.addAction(HwAPPQoEUtils.EMCOM_PARA_READY_ACTION);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.emcomIntentFilter, HwAPPQoEUtils.EMCOM_PARA_UPGRADE_PERMISSION, null);
    }

    private class SystemBroadcastReceiver extends BroadcastReceiver {
        private SystemBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwAPPQoEUtils.logE(HwAPPQoESystemStateMonitor.TAG, false, "intent is null", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                int wifistatus = intent.getIntExtra("wifi_state", 4);
                if (wifistatus == 1) {
                    HwAPPQoEUtils.logE(HwAPPQoEUtils.TAG, false, "WIFI_STATE_DISABLED", new Object[0]);
                } else if (wifistatus == 3) {
                    HwAPPQoEUtils.logE(HwAPPQoEUtils.TAG, false, "WIFI_STATE_ENABLED", new Object[0]);
                }
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED.equals(action)) {
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, false, "Wifi Connection State Changed", new Object[0]);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, false, "ACTION_BOOT_COMPLETED", new Object[0]);
                HwAPPQoESystemStateMonitor.this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                HwAPPQoESystemStateMonitor.this.onConnectivityNetworkChange();
                HwAPPQoESystemStateMonitor.this.mHandler.sendEmptyMessage(200);
            } else if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, false, "Connectivity Changed", new Object[0]);
                HwAPPQoESystemStateMonitor.this.onConnectivityNetworkChange();
            } else if ("com.android.server.hidata.arbitration.HwArbitrationStateMachine".equals(action)) {
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, false, " HwArbitrationStateMachine broadcast test", new Object[0]);
                int network = intent.getIntExtra("MPLinkSuccessNetworkKey", 802);
                if (intent.getIntExtra("MPLinkSuccessUIDKey", -1) == HwAPPQoESystemStateMonitor.this.curAPPStateInfo.mAppUID) {
                    HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, false, "MPLINK_STATE_CHANGE:%{public}d", Integer.valueOf(network));
                    HwAPPQoESystemStateMonitor.this.mHandler.sendMessage(HwAPPQoESystemStateMonitor.this.mHandler.obtainMessage(201, Integer.valueOf(network)));
                }
            } else if (HwAPPQoEUtils.EMCOM_PARA_READY_ACTION.equals(action)) {
                int cotaParaBitRec = intent.getIntExtra(HwAPPQoEUtils.EMCOM_PARA_READY_REC, 0);
                HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, false, "emcom para is ready: cotaParaBitRec:%{public}d", Integer.valueOf(cotaParaBitRec));
                if ((cotaParaBitRec & 16) == 0) {
                    HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, false, "broadcast is not for no cell", new Object[0]);
                    return;
                }
                HwAPPQoEResourceManger mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
                if (mHwAPPQoEResourceManger != null) {
                    mHwAPPQoEResourceManger.onConfigFilePathChanged();
                }
            } else if ("android.net.wifi.RSSI_CHANGED".equals(action) || SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                HwAPPQoESystemStateMonitor.this.startWeakNetworkMonitor();
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                HwAPPQoESystemStateMonitor.this.stopWeakNetworkMonitor();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConnectivityNetworkChange() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager != null) {
            this.mActiveNetworkInfo = connectivityManager.getActiveNetworkInfo();
            NetworkInfo networkInfo = this.mActiveNetworkInfo;
            if (networkInfo == null) {
                HwAPPQoEUtils.logD(TAG, false, "onConnectivityNetworkChange-prev_type is:%{public}d", Integer.valueOf(this.mConnectivityType));
                int i = this.mConnectivityType;
                if (800 == i) {
                    this.mHandler.sendEmptyMessage(4);
                } else if (801 == i) {
                    this.mHandler.sendEmptyMessage(8);
                }
                this.mConnectivityType = 802;
            } else if (1 == networkInfo.getType()) {
                if (true == this.mActiveNetworkInfo.isConnected()) {
                    HwAPPQoEUtils.logD(TAG, false, "onConnectivityNetworkChange, MSG_WIFI_STATE_CONNECTED", new Object[0]);
                    this.mHandler.sendEmptyMessage(3);
                    this.mConnectivityType = 800;
                    return;
                }
                HwAPPQoEUtils.logD(TAG, false, "onConnectivityNetworkChange-Wifi:%{public}d", this.mActiveNetworkInfo.getState());
            } else if (this.mActiveNetworkInfo.getType() != 0) {
            } else {
                if (true == this.mActiveNetworkInfo.isConnected()) {
                    HwAPPQoEUtils.logD(TAG, false, "onConnectivityNetworkChange, MSG_CELL_STATE_CONNECTED", new Object[0]);
                    this.mHandler.sendEmptyMessage(7);
                    this.mConnectivityType = 801;
                    return;
                }
                HwAPPQoEUtils.logD(TAG, false, "onConnectivityNetworkChange-Cell:%{public}d", this.mActiveNetworkInfo.getState());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWeakNetworkMonitor() {
        HwAPPStateInfo hwAPPStateInfo = this.curAPPStateInfo;
        if (hwAPPStateInfo != null && hwAPPStateInfo.mAppId != -1) {
            if (!HwAPPQoEManager.isAppStartMonitor(this.curAPPStateInfo, this.mContext)) {
                HwAPPQoEUtils.logD(TAG, false, "not start WeakNetworkMonitor", new Object[0]);
                return;
            }
            HwAPPQoEUtils.logD(TAG, false, "system monitor curAPPStateInfo.mAppId = %{public}d", Integer.valueOf(this.curAPPStateInfo.mAppId));
            WifiInfo info = ((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo();
            if (info != null) {
                int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(info.getFrequency(), info.getRssi());
                if (rssiLevel <= 1 && !this.mHandler.hasMessages(109)) {
                    HwAPPQoEUtils.logD(TAG, false, "system monitor send weak network message", new Object[0]);
                    this.mHandler.sendEmptyMessageDelayed(109, 4000);
                } else if (rssiLevel >= 2 && this.mHandler.hasMessages(109)) {
                    HwAPPQoEUtils.logD(TAG, false, "system monitor remove weak network message", new Object[0]);
                    this.mHandler.removeMessages(109);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopWeakNetworkMonitor() {
        HwAPPQoEUtils.logD(TAG, false, "system monitor stopWeakNetworkMonitor", new Object[0]);
        this.mHandler.removeMessages(109);
    }

    public boolean isDefaultApnType(String apnType) {
        return AppActConstant.VALUE_DEFAULT.equals(apnType);
    }

    public boolean isSlotIdValid(int slotId) {
        return slotId >= 0 && 2 > slotId;
    }

    public boolean getMpLinkState() {
        return HwArbitrationFunction.isInMPLink(this.mContext, this.curAPPStateInfo.mAppUID);
    }

    private class UserDataEnableObserver extends ContentObserver {
        public UserDataEnableObserver(Handler handler) {
            super(handler);
            HwAPPQoESystemStateMonitor.this.mResolver = HwAPPQoESystemStateMonitor.this.mContext.getContentResolver();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            HwAPPQoEUtils.logD(HwAPPQoESystemStateMonitor.TAG, false, "User change Data service state = %{public}d", Integer.valueOf(Settings.Global.getInt(HwAPPQoESystemStateMonitor.this.mContext.getContentResolver(), "mobile_data", -1)));
        }

        public void register() {
            HwAPPQoESystemStateMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, this);
        }

        public void unregister() {
            HwAPPQoESystemStateMonitor.this.mResolver.unregisterContentObserver(this);
        }
    }
}
