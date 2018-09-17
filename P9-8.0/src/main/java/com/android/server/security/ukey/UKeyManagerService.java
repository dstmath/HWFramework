package com.android.server.security.ukey;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.android.server.security.ukey.UKeyApplicationScanner.UKeyApkInfo;
import com.android.server.security.ukey.jni.UKeyJNI;
import huawei.android.security.IUKeyManager.Stub;

public class UKeyManagerService extends Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    public static final Creator CREATOR = new Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(UKeyManagerService.TAG, "create UKeyManagerService");
            return new UKeyManagerService(context);
        }

        public String getPluginPermission() {
            return null;
        }
    };
    private static final int FAILED = -1;
    private static final boolean IS_UKEY_SWITCH_ON = SystemProperties.getBoolean(UKEY_SWITCH_PRO, false);
    private static final String TAG = "UKeyManagerService";
    private static final String UKEY_MANAGER_PERMISSION = "com.huawei.ukey.permission.UKEY_MANAGER";
    private static final String UKEY_SWITCH_PRO = "ro.config.hw_ukey_on";
    private static final String UKEY_SWITCH_STATUS = "UKEY_SWITCH_STATUS";
    private static final int UKEY_UNSUPPORTED = 0;
    private static final int UKEY_VERSION = SystemProperties.getInt(UKEY_VERSION_PRO, 1);
    private static final int UKEY_VERSION_ONE = 1;
    private static final String UKEY_VERSION_PRO = "ro.config.hw_ukey_version";
    private static final int UKEY_VERSION_TWO = 2;
    private Context mContext;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor(this, null);
    private UKeyApplicationScanner uKeyApplicationScanner = null;

    private final class MyPackageMonitor extends PackageMonitor {
        /* synthetic */ MyPackageMonitor(UKeyManagerService this$0, MyPackageMonitor -this1) {
            this();
        }

        private MyPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int uid) {
            Slog.i(UKeyManagerService.TAG, "app is added. The packageName is : " + packageName + "uid = " + uid);
            if (UserHandle.getUserId(uid) == 0 && UKeyManagerService.this.isValidPackageName(packageName)) {
                UKeyManagerService.this.setUKeySwitchDisabled(packageName, false);
            }
        }
    }

    public UKeyManagerService(Context context) {
        this.mContext = context;
        this.uKeyApplicationScanner = new UKeyApplicationScanner(this.mContext);
    }

    public void onStart() {
        this.uKeyApplicationScanner = new UKeyApplicationScanner(this.mContext);
        this.uKeyApplicationScanner.loadUKeyApkWhitelist();
        this.mMyPackageMonitor.register(this.mContext, null, UserHandle.OWNER, false);
    }

    public void onStop() {
    }

    public IBinder asBinder() {
        return this;
    }

    public int isSwitchFeatureOn() {
        if (UserHandle.getUserId(Binder.getCallingUid()) == 0 && IS_UKEY_SWITCH_ON) {
            return UKEY_VERSION;
        }
        return 0;
    }

    public int isUKeySwitchDisabled(String packageName) {
        if (isSwitchFeatureOn() < 2 || (isValidPackageName(packageName) ^ 1) != 0) {
            return -1;
        }
        return UKeyJNI.isUKeySwitchDisabled(this.uKeyApplicationScanner.getRealUKeyPkgName(packageName));
    }

    public int setUKeySwitchDisabled(String packageName, boolean isDisabled) {
        this.mContext.enforceCallingOrSelfPermission(UKEY_MANAGER_PERMISSION, "does not have ukey manager permission!");
        if (isSwitchFeatureOn() < 2 || (isValidPackageName(packageName) ^ 1) != 0) {
            return -1;
        }
        String realPkgName = this.uKeyApplicationScanner.getRealUKeyPkgName(packageName);
        UKeyApkInfo uKeyApkInfo = this.uKeyApplicationScanner.getUKeyApkInfo(realPkgName);
        if (uKeyApkInfo != null) {
            return UKeyJNI.setUKeySwitchDisabled(realPkgName, uKeyApkInfo.mUKeyId, isDisabled);
        }
        return -1;
    }

    public Bundle getUKeyApkInfo(String packageName) {
        this.mContext.enforceCallingOrSelfPermission(UKEY_MANAGER_PERMISSION, "does not have ukey manager permission!");
        if (isSwitchFeatureOn() < 2 || (isValidPackageName(packageName) ^ 1) != 0) {
            return null;
        }
        Bundle bundle = this.uKeyApplicationScanner.getUKeyApkInfoData(packageName);
        if (bundle != null) {
            bundle.putInt(UKEY_SWITCH_STATUS, isUKeySwitchDisabled(packageName));
        }
        return bundle;
    }

    private boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        if (this.uKeyApplicationScanner.isWhiteListedUKeyApp(packageName)) {
            return true;
        }
        Slog.i(TAG, "The app is not ukey application, packageName : " + packageName);
        return false;
    }
}
