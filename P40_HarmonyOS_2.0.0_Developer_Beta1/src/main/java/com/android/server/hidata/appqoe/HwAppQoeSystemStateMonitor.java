package com.android.server.hidata.appqoe;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.intellicom.common.SmartDualCardConsts;

public class HwAppQoeSystemStateMonitor {
    private static String TAG = (HwArbitrationDefs.BASE_TAG + HwAppQoeSystemStateMonitor.class.getSimpleName());
    private NetworkInfo mActiveNetworkInfo;
    private BroadcastReceiver mBroadcastReceiver = new SystemBroadcastReceiver();
    private ConnectivityManager mConnectivityManager;
    private int mConnectivityType = 802;
    private Context mContext;
    public HwAppStateInfo mCurAppStateInfo = new HwAppStateInfo();
    private Handler mHandler;

    public HwAppQoeSystemStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        IntentFilter mEmcomIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        mIntentFilter.addAction("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
        mIntentFilter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        mIntentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        mIntentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        this.mContext.registerReceiver(this.mBroadcastReceiver, mIntentFilter, "com.huawei.hidata.permission.MPLINK_START_CHECK", null);
        mEmcomIntentFilter.addAction(HwAppQoeUtils.EMCOM_PARA_READY_ACTION);
        this.mContext.registerReceiver(this.mBroadcastReceiver, mEmcomIntentFilter, HwAppQoeUtils.EMCOM_PARA_UPGRADE_PERMISSION, null);
    }

    private class SystemBroadcastReceiver extends BroadcastReceiver {
        private SystemBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwAppQoeUtils.logE(HwAppQoeSystemStateMonitor.TAG, false, "intent is null", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwAppQoeUtils.logD(HwAppQoeSystemStateMonitor.TAG, false, "ACTION_BOOT_COMPLETED", new Object[0]);
                HwAppQoeSystemStateMonitor.this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                HwAppQoeSystemStateMonitor.this.onConnectivityNetworkChange();
                HwAppQoeSystemStateMonitor.this.mHandler.sendEmptyMessage(200);
            } else if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                HwAppQoeUtils.logD(HwAppQoeSystemStateMonitor.TAG, false, "Connectivity Changed", new Object[0]);
                HwAppQoeSystemStateMonitor.this.onConnectivityNetworkChange();
            } else if ("com.android.server.hidata.arbitration.HwArbitrationStateMachine".equals(action)) {
                HwAppQoeUtils.logD(HwAppQoeSystemStateMonitor.TAG, false, " HwArbitrationStateMachine broadcast test", new Object[0]);
                int network = intent.getIntExtra("MPLinkSuccessNetworkKey", 802);
                if (intent.getIntExtra("MPLinkSuccessUIDKey", -1) == HwAppQoeSystemStateMonitor.this.mCurAppStateInfo.mAppUid) {
                    HwAppQoeUtils.logD(HwAppQoeSystemStateMonitor.TAG, false, "MPLINK_STATE_CHANGE:%{public}d", Integer.valueOf(network));
                    HwAppQoeSystemStateMonitor.this.mHandler.sendMessage(HwAppQoeSystemStateMonitor.this.mHandler.obtainMessage(201, Integer.valueOf(network)));
                }
            } else if (HwAppQoeUtils.EMCOM_PARA_READY_ACTION.equals(action)) {
                int cotaParaBitRec = intent.getIntExtra(HwAppQoeUtils.EMCOM_PARA_READY_REC, 0);
                HwAppQoeUtils.logD(HwAppQoeSystemStateMonitor.TAG, false, "emcom para is ready: cotaParaBitRec:%{public}d", Integer.valueOf(cotaParaBitRec));
                if ((cotaParaBitRec & 16) == 0) {
                    HwAppQoeUtils.logD(HwAppQoeSystemStateMonitor.TAG, false, "broadcast is not for no cell", new Object[0]);
                    return;
                }
                HwAppQoeResourceManager mHwAppQoeResourceManager = HwAppQoeResourceManager.getInstance();
                if (mHwAppQoeResourceManager != null) {
                    mHwAppQoeResourceManager.onConfigFilePathChanged();
                }
            } else if ("android.net.wifi.RSSI_CHANGED".equals(action) || SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                HwAppQoeSystemStateMonitor.this.startWeakNetworkMonitor();
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                HwAppQoeSystemStateMonitor.this.stopWeakNetworkMonitor();
            }
        }
    }

    private void getActiveNetworkInfo() {
        if (this.mActiveNetworkInfo.getType() == 1) {
            if (this.mActiveNetworkInfo.isConnected()) {
                HwAppQoeUtils.logD(TAG, false, "onConnectivityNetworkChange, MSG_WIFI_STATE_CONNECTED", new Object[0]);
                this.mHandler.sendEmptyMessage(3);
                this.mConnectivityType = 800;
                return;
            }
            HwAppQoeUtils.logD(TAG, false, "onConnectivityNetworkChange-Wifi:%{public}d", this.mActiveNetworkInfo.getState());
        } else if (this.mActiveNetworkInfo.getType() != 0) {
            HwAppQoeUtils.logD(TAG, false, "other active type", new Object[0]);
        } else if (this.mActiveNetworkInfo.isConnected()) {
            HwAppQoeUtils.logD(TAG, false, "onConnectivityNetworkChange, MSG_CELL_STATE_CONNECTED", new Object[0]);
            this.mHandler.sendEmptyMessage(7);
            this.mConnectivityType = 801;
        } else {
            HwAppQoeUtils.logD(TAG, false, "onConnectivityNetworkChange-Cell:%{public}d", this.mActiveNetworkInfo.getState());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConnectivityNetworkChange() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager != null) {
            this.mActiveNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (this.mActiveNetworkInfo != null) {
                getActiveNetworkInfo();
                return;
            }
            HwAppQoeUtils.logD(TAG, false, "onConnectivityNetworkChange-prev_type is:%{public}d", Integer.valueOf(this.mConnectivityType));
            int i = this.mConnectivityType;
            if (i == 800) {
                this.mHandler.sendEmptyMessage(4);
            } else if (i == 801) {
                this.mHandler.sendEmptyMessage(8);
            }
            this.mConnectivityType = 802;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWeakNetworkMonitor() {
        HwAppStateInfo hwAppStateInfo = this.mCurAppStateInfo;
        if (hwAppStateInfo != null && hwAppStateInfo.mAppId != -1) {
            if (!HwAppQoeManager.isAppStartMonitor(this.mCurAppStateInfo, this.mContext)) {
                HwAppQoeUtils.logD(TAG, false, "not start WeakNetworkMonitor", new Object[0]);
                return;
            }
            HwAppQoeUtils.logD(TAG, false, "system monitor curAPPStateInfo.mAppId = %{public}d", Integer.valueOf(this.mCurAppStateInfo.mAppId));
            WifiInfo info = ((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo();
            if (info != null) {
                int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(info.getFrequency(), info.getRssi());
                if (rssiLevel <= 1 && !this.mHandler.hasMessages(109)) {
                    HwAppQoeUtils.logD(TAG, false, "system monitor send weak network message", new Object[0]);
                    this.mHandler.sendEmptyMessageDelayed(109, 4000);
                } else if (rssiLevel >= 2 && this.mHandler.hasMessages(109)) {
                    HwAppQoeUtils.logD(TAG, false, "system monitor remove weak network message", new Object[0]);
                    this.mHandler.removeMessages(109);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopWeakNetworkMonitor() {
        HwAppQoeUtils.logD(TAG, false, "system monitor stopWeakNetworkMonitor", new Object[0]);
        this.mHandler.removeMessages(109);
    }
}
