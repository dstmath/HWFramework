package com.android.server.wifi.HwQoE;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.HwHidataAppStateInfo;
import com.android.server.hidata.HwHidataManager;
import com.android.server.hidata.IHwHidataCallback;
import com.android.server.wifi.HwQoE.HwQoEHilink;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.util.ArrayList;
import java.util.List;

public class HwQoEAccAppAware {
    private static final String BOOT_PERMISSION = "android.permission.RECEIVE_BOOT_COMPLETED";
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final String INVALID_APP_NAME = "";
    private static final boolean IS_ENABLE_EN_ACC = SystemProperties.getBoolean("ro.config.wifi_enterprise_acc", true);
    private static final String TAG = "HwQoEAccAppAware";
    private List<String> mAccAppList = new ArrayList();
    private ActionReceiver mActionReceiver;
    private final IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.android.server.wifi.HwQoE.HwQoEAccAppAware.AnonymousClass2 */

        public void call(Bundle extras) {
            if (extras == null) {
                HwHiLog.d(HwQoEAccAppAware.TAG, false, "AMS callback , extras = null", new Object[0]);
                return;
            }
            Object tempComponentName = extras.getParcelable("comp");
            if (!(tempComponentName instanceof ComponentName)) {
                HwHiLog.d(HwQoEAccAppAware.TAG, false, "AMS callback, invalid input param", new Object[0]);
                return;
            }
            ComponentName componentName = (ComponentName) tempComponentName;
            int appUid = extras.getInt("uid");
            String className = "";
            String packageName = componentName != null ? componentName.getPackageName() : className;
            if (componentName != null) {
                className = componentName.getClassName();
            }
            String flag = extras.getString("state");
            HwHiLog.d(HwQoEAccAppAware.TAG, false, "uid = %{public}d, pktname = %{public}s, className = %{public}s", new Object[]{Integer.valueOf(appUid), packageName, className});
            if ("onResume".equals(flag)) {
                HwQoEAccAppAware.this.handleActivityChange(packageName, className, appUid);
            }
        }
    };
    private final HwQoEHilink.IAppListStateChangeCallback mAppListStateChangeCallback = new HwQoEHilink.IAppListStateChangeCallback() {
        /* class com.android.server.wifi.HwQoE.HwQoEAccAppAware.AnonymousClass1 */

        @Override // com.android.server.wifi.HwQoE.HwQoEHilink.IAppListStateChangeCallback
        public void notifyClearAppList() {
            HwQoEAccAppAware.this.clearAccAppList();
        }

        @Override // com.android.server.wifi.HwQoE.HwQoEHilink.IAppListStateChangeCallback
        public void notifyUpdateAppList(List<String> appList) {
            HwQoEAccAppAware.this.updateAccAppListAndTriggerAcc(appList);
        }
    };
    private Context mContext = null;
    private String mCurrentActiveAppName = "";
    private final IHwHidataCallback mHwHidataCallback = new IHwHidataCallback() {
        /* class com.android.server.wifi.HwQoE.HwQoEAccAppAware.AnonymousClass3 */

        public void onAppStateChangeCallBack(HwHidataAppStateInfo appStateInfo) {
            if (appStateInfo == null) {
                HwHiLog.d(HwQoEAccAppAware.TAG, false, "ERROR: appStateInfo is null", new Object[0]);
            } else if (appStateInfo.getCurScence() == 100105 || appStateInfo.getCurScence() == 100106) {
                String pacakgeName = HwQoEAccAppAware.this.mContext.getPackageManager().getNameForUid(appStateInfo.getCurUid());
                HwHiLog.d(HwQoEAccAppAware.TAG, false, "pacakgeName = %{public}s, state = %{public}d", new Object[]{pacakgeName, Integer.valueOf(appStateInfo.getCurState())});
                switch (appStateInfo.getCurState()) {
                    case 100:
                    case 103:
                        HwHiLog.d(HwQoEAccAppAware.TAG, false, "start accelerating ...", new Object[0]);
                        if (HwQoEAccAppAware.this.mHwQoeHilink != null) {
                            HwQoEAccAppAware.this.mHwQoeHilink.handleAccGameStateChanged(true, pacakgeName);
                            return;
                        } else {
                            HwHiLog.d(HwQoEAccAppAware.TAG, false, "start accelerating FAILED, mHwWifiBoost is null", new Object[0]);
                            return;
                        }
                    case 101:
                    case HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET /* 104 */:
                        HwHiLog.d(HwQoEAccAppAware.TAG, false, "stop accelerating ...", new Object[0]);
                        if (HwQoEAccAppAware.this.mHwQoeHilink != null) {
                            HwQoEAccAppAware.this.mHwQoeHilink.handleAccGameStateChanged(false, pacakgeName);
                            return;
                        } else {
                            HwHiLog.d(HwQoEAccAppAware.TAG, false, "stop accelerating FAILED, mHwWifiBoost is null", new Object[0]);
                            return;
                        }
                    case 102:
                    default:
                        HwHiLog.d(HwQoEAccAppAware.TAG, false, "unknown mCurState", new Object[0]);
                        return;
                }
            } else {
                HwHiLog.d(HwQoEAccAppAware.TAG, false, "INFO: mCurScence is not audio or video", new Object[0]);
            }
        }
    };
    private HwQoEHilink mHwQoeHilink = null;
    private boolean mIsBroadcastRegisted = false;
    private String mLastClassName = "";
    private String mLastPkgName = "";
    private int mLastUid = -1;
    private final Object mLock = new Object();

    public HwQoEAccAppAware(Context context, HwQoEHilink hwQoeHilink) {
        this.mContext = context;
        this.mHwQoeHilink = hwQoeHilink;
        if (IS_ENABLE_EN_ACC) {
            registerActionReceiver();
        }
    }

    private void registerActionReceiver() {
        this.mActionReceiver = new ActionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(HW_SYSTEM_SERVER_START);
        this.mContext.registerReceiver(this.mActionReceiver, filter, BOOT_PERMISSION, null);
    }

    /* access modifiers changed from: private */
    public class ActionReceiver extends BroadcastReceiver {
        private ActionReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && HwQoEAccAppAware.HW_SYSTEM_SERVER_START.equals(intent.getAction())) {
                HwHiLog.d(HwQoEAccAppAware.TAG, false, "register HwQoEAccAppAware callback!", new Object[0]);
                HwQoEAccAppAware.this.registerHiDataMonitor();
                HwQoEAccAppAware hwQoEAccAppAware = HwQoEAccAppAware.this;
                hwQoEAccAppAware.registerAppListStateChangeCallback(hwQoEAccAppAware.mAppListStateChangeCallback);
                ActivityManagerEx.registerHwActivityNotifier(HwQoEAccAppAware.this.mActivityNotifierEx, "activityLifeState");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerHiDataMonitor() {
        HwHidataManager hwHidataManager = HwHidataManager.getInstance();
        if (hwHidataManager != null) {
            HwHiLog.d(TAG, false, "registerHiDateMonitor", new Object[0]);
            hwHidataManager.registerHidataMonitor(this.mHwHidataCallback);
            return;
        }
        HwHiLog.e(TAG, false, "hwHidataManager is null", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerAppListStateChangeCallback(HwQoEHilink.IAppListStateChangeCallback appListStateChangeCallback) {
        HwHiLog.d(TAG, false, "registerAppListStateChangeCallback", new Object[0]);
        HwQoEHilink hwQoEHilink = this.mHwQoeHilink;
        if (hwQoEHilink != null) {
            hwQoEHilink.registerAppListStateChangeCallback(appListStateChangeCallback);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearAccAppList() {
        HwHiLog.i(TAG, false, "clear acc app list", new Object[0]);
        String accAppName = "";
        synchronized (this.mLock) {
            if (isNeedAcc(this.mCurrentActiveAppName)) {
                accAppName = this.mCurrentActiveAppName;
            }
            this.mAccAppList.clear();
        }
        if (this.mHwQoeHilink != null && !TextUtils.isEmpty(accAppName)) {
            this.mHwQoeHilink.handleAccGameStateChanged(false, accAppName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAccAppListAndTriggerAcc(List<String> appList) {
        String accAppName = "";
        synchronized (this.mLock) {
            if (this.mAccAppList.isEmpty()) {
                for (String appName : appList) {
                    if (isNeedMonitorApp(appName)) {
                        this.mAccAppList.add(appName);
                    }
                }
            }
            if (isNeedAcc(this.mCurrentActiveAppName)) {
                accAppName = this.mCurrentActiveAppName;
            }
        }
        HwHiLog.i(TAG, false, "update acc app list size %{public}d", new Object[]{Integer.valueOf(this.mAccAppList.size())});
        if (!(this.mHwQoeHilink == null || TextUtils.isEmpty(accAppName))) {
            this.mHwQoeHilink.handleAccGameStateChanged(true, accAppName);
        }
    }

    private boolean isNeedAcc(String appName) {
        if (!this.mAccAppList.isEmpty() && !TextUtils.isEmpty(appName)) {
            return this.mAccAppList.contains(appName);
        }
        return false;
    }

    private void triggerAccApp(boolean isEnable, String appName) {
        String accAppName = "";
        synchronized (this.mLock) {
            this.mCurrentActiveAppName = appName;
            if (isNeedAcc(this.mCurrentActiveAppName)) {
                accAppName = this.mCurrentActiveAppName;
            }
            if (!isEnable) {
                this.mCurrentActiveAppName = "";
            }
        }
        if (this.mHwQoeHilink != null && !TextUtils.isEmpty(accAppName)) {
            this.mHwQoeHilink.handleAccGameStateChanged(isEnable, accAppName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleActivityChange(String packageName, String className, int appUid) {
        if (this.mLastUid != appUid || !this.mLastPkgName.equals(packageName) || !this.mLastClassName.equals(className)) {
            if (this.mLastUid != appUid) {
                triggerAccApp(false, this.mLastPkgName);
                if (isNeedMonitorApp(packageName)) {
                    triggerAccApp(true, packageName);
                }
            } else if (!isNeedMonitorApp(packageName) || this.mLastClassName.equals(className)) {
                HwHiLog.i(TAG, false, "need do nothing", new Object[0]);
            } else {
                triggerAccApp(false, packageName);
                triggerAccApp(true, packageName);
            }
            this.mLastUid = appUid;
            this.mLastPkgName = packageName;
            this.mLastClassName = className;
            return;
        }
        HwHiLog.i(TAG, false, "Nothing changed, no need update!", new Object[0]);
    }

    private boolean isNeedMonitorApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            if (packageManager == null || (packageManager.getApplicationInfo(packageName, 0).flags & 1) == 1) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            HwHiLog.i(TAG, false, "App name not found!", new Object[0]);
        }
    }
}
