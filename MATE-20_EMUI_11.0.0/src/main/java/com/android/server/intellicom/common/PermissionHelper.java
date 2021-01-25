package com.android.server.intellicom.common;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import huawei.android.net.slice.IAppInfoCallback;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class PermissionHelper {
    private static final int DEVICE_NOT_PROVISIONED = 0;
    private static final int DEVICE_PROVISIONED = 1;
    private static final String KIT_CATEGORY_SLICE = "com.huawei.cellularkit.permission.slice";
    private static final String TAG = "PermissionHelper";
    private Map<Integer, IAppInfoCallback> mCallbacks;
    private Context mContext;
    private Map<Integer, Boolean> mPermissions;
    private ContentObserver mProvisionedObserver;

    private PermissionHelper() {
        this.mCallbacks = new ConcurrentHashMap();
        this.mPermissions = new ConcurrentHashMap();
    }

    public static PermissionHelper getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void init(Context context) {
        if (context == null) {
            log("init error context is null");
            return;
        }
        this.mContext = context;
        if (isProvisioned()) {
            initHwKitAssistant();
        }
        log("init finished");
    }

    public void initAppInfo(String appId, int uid, IAppInfoCallback appInfoCallback) {
        if (Stream.of(appId, appInfoCallback).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            log("initAppInfo error, appInfoCallback or appId is null.");
        } else {
            this.mCallbacks.put(Integer.valueOf(uid), appInfoCallback);
        }
    }

    public boolean hasPermission(int appUid) {
        Context context = this.mContext;
        if (context == null) {
            log("mContext is null appUid = " + appUid);
            return false;
        }
        try {
            context.enforceCallingPermission("android.permission.CHANGE_NETWORK_STATE", "No permissions to network slice");
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isProvisioned() {
        Context context = this.mContext;
        if (context != null && Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 1) == 1) {
            return true;
        }
        return false;
    }

    private void listenProvision() {
        if (this.mContext != null) {
            this.mProvisionedObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                /* class com.android.server.intellicom.common.PermissionHelper.AnonymousClass1 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    PermissionHelper.log("Povisioned is changed: " + PermissionHelper.this.isProvisioned());
                    if (PermissionHelper.this.isProvisioned()) {
                        PermissionHelper.this.initHwKitAssistant();
                    }
                }
            };
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), true, this.mProvisionedObserver);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initHwKitAssistant() {
        if (this.mContext != null) {
        }
    }

    private void onAppBindFinish(int appUid, int retCode) {
        IAppInfoCallback appInfoCallback = this.mCallbacks.get(Integer.valueOf(appUid));
        if (appInfoCallback == null) {
            log("Can not find appInfoCallback by appUid: " + appUid);
            return;
        }
        log("AppUid =" + appUid + " onAppBindFinish, resultCode = " + retCode);
        onPermissionCheckCallback(appInfoCallback, false);
    }

    private void onPermissionCheckCallback(IAppInfoCallback appInfoCallback, boolean result) {
        if (appInfoCallback != null) {
            try {
                appInfoCallback.onPermissionCheckCallback(result);
            } catch (RemoteException e) {
                log("onPermissionCheckCallback RemoteException occurs.");
            }
        }
    }

    /* access modifiers changed from: private */
    public static void log(String msg) {
        Log.i(TAG, msg);
    }

    private static class SingletonInstance {
        private static final PermissionHelper INSTANCE = new PermissionHelper();

        private SingletonInstance() {
        }
    }
}
