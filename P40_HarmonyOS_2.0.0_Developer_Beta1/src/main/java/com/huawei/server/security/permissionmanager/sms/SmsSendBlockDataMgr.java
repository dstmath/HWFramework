package com.huawei.server.security.permissionmanager.sms;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.android.content.pm.OnPermissionsChangedListenerEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.util.SlogEx;
import com.huawei.server.security.permissionmanager.HwSmsBlockDbAdapter;
import com.huawei.server.security.permissionmanager.sms.smsutils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsSendBlockDataMgr {
    private static final String CLOUD_CONFIG_TABLE_COL_DEFAULT_RESULT = "smsPermission";
    private static final String CLOUD_CONFIG_TABLE_COL_PACKAGE_NAME = "packageName";
    private static final int DEFAULT_LENGTH = 16;
    private static final long DELAY_LOAD = 2000;
    private static final long DELAY_REGISTER = 2000;
    private static final int EVT_ON_OPS_CHANGE = 1004;
    private static final int EVT_ON_PERMISSION_CHANGE = 1003;
    private static final int EVT_PACKAGE_REMOVE = 1001;
    private static final int EVT_PERMISSION_RESET = 1002;
    private static final int EVT_READ_CFG_FILE = 1000;
    private static final int EVT_REGISTER_LISTENER = 1005;
    private static final int NO_USER_DISTINGUISH = -1;
    private static final String SEND_SMS_PERMISSION = "android.permission.SEND_SMS";
    private static final String TAG = "SmsSendBlockDataMgr";
    private static SmsSendBlockDataMgr sInstance;
    private final AppOpsManager mAppOpsManager;
    private final Context mContext;
    private Handler mHandler;
    private HwSmsBlockDbAdapter mHwSmsBlockDbAdapter;
    private long mLastWriteTime;
    private HashMap<String, SmsSendOpsChangeListener> mOpsListenerMap = new HashMap<>(16);
    private List<String> mPreDefinedSmsApps = new ArrayList(16);
    private OnPermissionsChangedListenerEx mRuntimePermissionChangedListener = new OnPermissionsChangedListenerEx() {
        /* class com.huawei.server.security.permissionmanager.sms.SmsSendBlockDataMgr.AnonymousClass1 */

        public void onPermissionsChanged(int uid) {
            PackageManager packageManager = SmsSendBlockDataMgr.this.mContext.getPackageManager();
            if (packageManager == null) {
                SlogEx.e(SmsSendBlockDataMgr.TAG, "onPermissionChange can't get package manager");
                return;
            }
            String[] pkgNames = packageManager.getPackagesForUid(uid);
            if (pkgNames == null || pkgNames.length == 0) {
                SlogEx.e(SmsSendBlockDataMgr.TAG, "onPermissionChange pkgName is null!");
                return;
            }
            String packageName = null;
            synchronized (SmsSendBlockDataMgr.this.mSendPermissionCfg) {
                int length = pkgNames.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String pkgName = pkgNames[i];
                    if (!SmsSendBlockDataMgr.this.mSendPermissionCfg.isEmpty()) {
                        if (SmsSendBlockDataMgr.this.mSendPermissionCfg.get(pkgName) != null) {
                            if (SmsSendBlockDataMgr.this.isValidAuthResult(((Integer) SmsSendBlockDataMgr.this.mSendPermissionCfg.get(pkgName)).intValue())) {
                                packageName = pkgName;
                                break;
                            }
                        }
                    }
                    i++;
                }
            }
            if (TextUtils.isEmpty(packageName)) {
                SlogEx.v(SmsSendBlockDataMgr.TAG, "onPermissionChange don't need call for package " + pkgNames[0]);
                return;
            }
            SlogEx.v(SmsSendBlockDataMgr.TAG, "onPermissionChange packageName " + packageName + " permission change!");
            SmsSendBlockDataMgr.this.mHandler.obtainMessage(SmsSendBlockDataMgr.EVT_ON_PERMISSION_CHANGE, uid, 0, packageName).sendToTarget();
        }
    };
    private final HashMap<String, Integer> mSendPermissionCfg;

    private SmsSendBlockDataMgr(Context context) {
        SlogEx.v(TAG, "Create SmsSendBlockDataMgr");
        this.mContext = context;
        this.mSendPermissionCfg = new HashMap<>(16);
        HandlerThread cmdThread = new HandlerThread("sms_send_block_task", -8);
        cmdThread.start();
        this.mHandler = new MyHandler(cmdThread.getLooper());
        this.mHwSmsBlockDbAdapter = HwSmsBlockDbAdapter.getInstance(this.mContext);
        this.mPreDefinedSmsApps = this.mHwSmsBlockDbAdapter.getPreDefinedSmsApps();
        this.mHandler.obtainMessage(EVT_READ_CFG_FILE).sendToTarget();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVT_REGISTER_LISTENER), 2000);
    }

    public static SmsSendBlockDataMgr getInstance(Context context) {
        SmsSendBlockDataMgr smsSendBlockDataMgr;
        synchronized (SmsSendBlockDataMgr.class) {
            if (sInstance == null) {
                sInstance = new SmsSendBlockDataMgr(context);
            }
            smsSendBlockDataMgr = sInstance;
        }
        return smsSendBlockDataMgr;
    }

    /* access modifiers changed from: private */
    public class SmsSendOpsChangeListener implements AppOpsManager.OnOpChangedListener {
        private SmsSendOpsChangeListener() {
        }

        @Override // android.app.AppOpsManager.OnOpChangedListener
        public void onOpChanged(String op, String packageName) {
            SlogEx.v(SmsSendBlockDataMgr.TAG, "onOpChanged OP = " + op + " packageName is " + packageName);
            SmsSendBlockDataMgr.this.mHandler.obtainMessage(SmsSendBlockDataMgr.EVT_ON_OPS_CHANGE, packageName).sendToTarget();
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SmsSendBlockDataMgr.EVT_READ_CFG_FILE /* 1000 */:
                    SmsSendBlockDataMgr.this.loadDefaultConfig();
                    return;
                case SmsSendBlockDataMgr.EVT_PACKAGE_REMOVE /* 1001 */:
                    SmsSendBlockDataMgr.this.onPackageDeleteInner((String) msg.obj);
                    return;
                case SmsSendBlockDataMgr.EVT_PERMISSION_RESET /* 1002 */:
                    SmsSendBlockDataMgr.this.onPermissionReset();
                    return;
                case SmsSendBlockDataMgr.EVT_ON_PERMISSION_CHANGE /* 1003 */:
                    SmsSendBlockDataMgr.this.onPermissionChangeInner(msg.arg1, (String) msg.obj);
                    return;
                case SmsSendBlockDataMgr.EVT_ON_OPS_CHANGE /* 1004 */:
                    SmsSendBlockDataMgr.this.onSmsSendOpsChange((String) msg.obj);
                    return;
                case SmsSendBlockDataMgr.EVT_REGISTER_LISTENER /* 1005 */:
                    SmsSendBlockDataMgr.this.registerListener();
                    return;
                default:
                    return;
            }
        }
    }

    public void onNotifyPkgRemove(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            this.mHandler.obtainMessage(EVT_PACKAGE_REMOVE, pkgName).sendToTarget();
        }
    }

    public void onNotifyUserReset() {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVT_PERMISSION_RESET), 2000);
    }

    public void parseCfgFromCloud(String packageName, int defaultVal) {
        addAuthResultForApk(packageName, defaultVal, true);
    }

    public void addAuthResultForApk(String packageName, int result, boolean isFromCloud) {
        SlogEx.v(TAG, "addAuthResultForAPK packageName = " + packageName + " auth_result = " + result);
        if (TextUtils.isEmpty(packageName) || !isValidAuthResult(result)) {
            SlogEx.e(TAG, "addAuthResultForAPK parameter is invalid!");
            return;
        }
        synchronized (this.mSendPermissionCfg) {
            if (this.mSendPermissionCfg.isEmpty() || this.mSendPermissionCfg.get(packageName) == null || this.mSendPermissionCfg.get(packageName).intValue() != result) {
                this.mSendPermissionCfg.put(packageName, Integer.valueOf(result));
                startListenOpsChange(packageName, isFromCloud);
                this.mHwSmsBlockDbAdapter.replaceSmsBlockAuthResult(packageName, result, -1);
                return;
            }
            SlogEx.e(TAG, "addAuthResultForAPK has the Same one!");
        }
    }

    public int getDefaultConfigForApk(String packageName, int userId) {
        if (TextUtils.isEmpty(packageName)) {
            SlogEx.e(TAG, "getDefaultAuthResult packageName IS NULL!");
            return 2;
        }
        synchronized (this.mSendPermissionCfg) {
            int i = 4;
            if (!this.mSendPermissionCfg.isEmpty()) {
                if (this.mSendPermissionCfg.get(packageName) != null) {
                    int result = this.mSendPermissionCfg.get(packageName).intValue();
                    SlogEx.v(TAG, "getDefaultConfigForApk PKG = " + packageName + " result = " + result);
                    if (isValidAuthResult(result)) {
                        i = result;
                    }
                    return i;
                }
            }
            return 4;
        }
    }

    private void deleteAuthResultForApk(String pkgName) {
        onPackageDeleteInner(pkgName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPackageDeleteInner(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "onPackageDeleteInner packageName IS NULL!");
        } else if (hasDefaultCfg(pkgName)) {
            SlogEx.v(TAG, "onPackageDeleteInner pkgName = " + pkgName + " hasDefaultCfg!");
        } else {
            synchronized (this.mSendPermissionCfg) {
                if (this.mSendPermissionCfg.containsKey(pkgName)) {
                    SlogEx.v(TAG, "onPackageDeleteInner pkgName = " + pkgName + " need delete the data!");
                    this.mSendPermissionCfg.remove(pkgName);
                    stopListenOpsChange(pkgName);
                    this.mHwSmsBlockDbAdapter.deleteSmsBlockAuthResult(pkgName, -1);
                }
            }
        }
    }

    private void cleanAllAuthenticationData() {
        synchronized (this.mSendPermissionCfg) {
            if (!this.mSendPermissionCfg.isEmpty()) {
                for (String pkgName : this.mSendPermissionCfg.keySet()) {
                    this.mHwSmsBlockDbAdapter.deleteSmsBlockAuthResult(pkgName, -1);
                }
                this.mSendPermissionCfg.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPermissionReset() {
        SlogEx.v(TAG, "onPermissionReset()");
        cleanAllAuthenticationData();
        unRegisterAllOpsChangeListener();
        loadDefaultCloudCfg();
    }

    private void loadDefaultCloudCfg() {
        Cursor cursor = this.mHwSmsBlockDbAdapter.getCursorOfDefaultSmsConfig();
        if (cursor == null || cursor.getCount() == 0) {
            SlogEx.e(TAG, "loadDefaultCloudCfg db is null!");
            if (cursor != null) {
                cursor.close();
                return;
            }
            return;
        }
        int packageNameIndex = cursor.getColumnIndex("packageName");
        int smsPermissionIndex = cursor.getColumnIndex("smsPermission");
        while (cursor.moveToNext()) {
            String pkgName = cursor.getString(packageNameIndex);
            int result = cursor.getInt(smsPermissionIndex);
            SlogEx.v(TAG, "loadDefaultCloudCfg pkgName = " + pkgName + " result = " + result);
            addAuthResultForApk(pkgName, result, true);
        }
        cursor.close();
    }

    private boolean hasDefaultCfg(String pkgName) {
        return this.mPreDefinedSmsApps.contains(pkgName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidAuthResult(int result) {
        return (result == 3) || (result == 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadDefaultConfig() {
        HashMap<String, Integer> tempMap = this.mHwSmsBlockDbAdapter.getAllSmsBlockSetting(-1);
        synchronized (this.mSendPermissionCfg) {
            this.mSendPermissionCfg.clear();
            this.mSendPermissionCfg.putAll(tempMap);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerListener() {
        SlogEx.v(TAG, "registerListener begin!");
        registerPermissionChangeListener();
        registerOpsChangeListener();
    }

    public void unregisterAllListener() {
        SlogEx.e(TAG, "unregisterListener begin!");
        unregisterPermissionChangeListener();
        unRegisterAllOpsChangeListener();
    }

    private void registerPermissionChangeListener() {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            SlogEx.e(TAG, "registerPermissionChangeListener get packagemanager failed!");
            return;
        }
        if (this.mRuntimePermissionChangedListener == null) {
            SlogEx.e(TAG, "registerPermissionChangeListener listener is null");
        }
        PackageManagerExt.addOnPermissionsChangeListener(packageManager, this.mRuntimePermissionChangedListener);
    }

    private void unregisterPermissionChangeListener() {
        SlogEx.v(TAG, "unregisterPermissionChangeListener!");
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            SlogEx.e(TAG, "unregisterPermissionChangeListener get packagemanager failed!");
            return;
        }
        if (this.mRuntimePermissionChangedListener == null) {
            SlogEx.e(TAG, "unregisterPermissionChangeListener listener is null");
        }
        PackageManagerExt.removeOnPermissionsChangeListener(packageManager, this.mRuntimePermissionChangedListener);
    }

    private void registerOpsChangeListener() {
        List<String> userDefinedSmsApps = this.mHwSmsBlockDbAdapter.getUserDefinedSmsApps();
        if (userDefinedSmsApps == null || userDefinedSmsApps.isEmpty()) {
            SlogEx.e(TAG, "List userDefinedSmsApps is empty");
            return;
        }
        int size = userDefinedSmsApps.size();
        for (int i = 0; i < size; i++) {
            SmsSendOpsChangeListener opsChangeListener = new SmsSendOpsChangeListener();
            SlogEx.v(TAG, "registerOpsChangeListener add pkg " + userDefinedSmsApps.get(i) + " into map!");
            this.mOpsListenerMap.put(userDefinedSmsApps.get(i), opsChangeListener);
        }
        if (this.mOpsListenerMap.isEmpty()) {
            SlogEx.v(TAG, "registerOpsChangeListener mOpsPackageNameList is empty!");
            return;
        }
        String smsOp = AppOpsManager.permissionToOp(SEND_SMS_PERMISSION);
        for (Map.Entry<String, SmsSendOpsChangeListener> entry : this.mOpsListenerMap.entrySet()) {
            this.mAppOpsManager.startWatchingMode(smsOp, entry.getKey(), entry.getValue());
        }
        SlogEx.v(TAG, "registerOpsChangeListener complete");
    }

    private void unRegisterAllOpsChangeListener() {
        for (Map.Entry<String, SmsSendOpsChangeListener> entry : this.mOpsListenerMap.entrySet()) {
            entry.getKey();
            this.mAppOpsManager.stopWatchingMode(entry.getValue());
        }
        this.mOpsListenerMap.clear();
        SlogEx.v(TAG, "unRegisterAllOpsChangeListener finish");
    }

    private boolean needListenOpsChange(PackageInfo pkgInfo) {
        if (pkgInfo == null || pkgInfo.applicationInfo.targetSdkVersion > 22) {
            String pkgName = pkgInfo != null ? pkgInfo.packageName : null;
            SlogEx.v(TAG, "needListenOpsChange sdk > 23 pkg is " + pkgName);
            return false;
        } else if (hasDefaultCfg(pkgInfo.packageName)) {
            SlogEx.e(TAG, "needListenOpsChange in the white list!");
            return false;
        } else {
            synchronized (this.mSendPermissionCfg) {
                if (!this.mSendPermissionCfg.isEmpty()) {
                    if (this.mSendPermissionCfg.get(pkgInfo.packageName) != null) {
                        return isValidAuthResult(this.mSendPermissionCfg.get(pkgInfo.packageName).intValue());
                    }
                }
                SlogEx.e(TAG, "needListenOpsChange DATA BASE IS EMPTY!");
                return false;
            }
        }
    }

    private void startListenOpsChange(String pkgName, boolean isFromCloud) {
        if (isFromCloud) {
            SlogEx.v(TAG, "startListenOpsChange packageName " + pkgName + " is in list, don't listen!");
            return;
        }
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(pkgName, 12288);
            if (packageInfo == null) {
                SlogEx.e(TAG, "isOpAllowed: packageInfo is null: " + pkgName);
            } else if (needListenOpsChange(packageInfo)) {
                SmsSendOpsChangeListener opsChangeListener = this.mOpsListenerMap.get(packageInfo.packageName);
                if (opsChangeListener != null) {
                    this.mOpsListenerMap.remove(packageInfo.packageName);
                    this.mAppOpsManager.stopWatchingMode(opsChangeListener);
                }
                SlogEx.v(TAG, "startListenOpsChange packageName is " + pkgName);
                String smsOp = AppOpsManager.permissionToOp(SEND_SMS_PERMISSION);
                SmsSendOpsChangeListener opsChangeListener2 = new SmsSendOpsChangeListener();
                this.mAppOpsManager.startWatchingMode(smsOp, pkgName, opsChangeListener2);
                this.mOpsListenerMap.put(packageInfo.packageName, opsChangeListener2);
            }
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "startListenOpsChange can't get pkgInfo for " + pkgName);
        }
    }

    private void stopListenOpsChange(String pkgName) {
        SmsSendOpsChangeListener listener = this.mOpsListenerMap.get(pkgName);
        if (listener != null) {
            SlogEx.v(TAG, "stopListenOpsChange packageName is " + pkgName);
            this.mAppOpsManager.stopWatchingMode(listener);
            this.mOpsListenerMap.remove(pkgName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSmsSendOpsChange(String pkgName) {
        if (!isOpAllowed(AppOpsManager.permissionToOp(SEND_SMS_PERMISSION), pkgName)) {
            SlogEx.v(TAG, "onSmsSendOpsChange remove the pkg is " + pkgName);
            deleteAuthResultForApk(pkgName);
        }
    }

    private boolean isOpAllowed(String op, String pkgName) {
        boolean isAppOpAllowed = false;
        if (TextUtils.isEmpty(op) || TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "isApkOpAllow op is empty, or aom is null!");
            return false;
        }
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(pkgName, 12288);
            if (packageInfo == null) {
                SlogEx.e(TAG, "isOpAllowed: packageInfo is null: " + pkgName);
                return false;
            }
            if (this.mAppOpsManager.checkOpNoThrow(op, packageInfo.applicationInfo.uid, pkgName) == 0) {
                isAppOpAllowed = true;
            }
            return isAppOpAllowed;
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "isOpAllowed can't get pkgInfo for " + pkgName);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPermissionChangeInner(int uid, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "onPermissionChangeInner pkgName is NULL!");
        } else if (!Utils.checkRuntimePermission(this.mContext, pkgName, SEND_SMS_PERMISSION, uid)) {
            deleteAuthResultForApk(pkgName);
        }
    }
}
