package com.huawei.ukey;

import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.hwpanpayservice.IHwSEService;
import com.huawei.hwpartsecurity.BuildConfig;
import huawei.android.ukey.HwSEServiceManager;

public class UKeyManager {
    private static final String APK_NAME_CN = "apkNameCn";
    private static final String APK_NAME_EN = "apkNameEn";
    private static final String CERT_MGR_NAME = "certMgrName";
    public static final int FAILED = -1;
    private static final boolean IS_UKEY_SWITCH_ON = SystemPropertiesEx.getBoolean(UKEY_SWITCH_PRO, false);
    private static final String PACKAGE_NAME = "packageName";
    private static final Object SERVICE_SYNC = new Object();
    public static final int SUCCESS = 0;
    public static final int SWITCH_OFF = 0;
    public static final int SWITCH_ON = 1;
    private static final String TAG = "UkeyManager";
    public static final int UEKY_VERSION_2 = 2;
    private static final String UKEY_SWITCH_PRO = "ro.config.hw_ukey_on";
    private static final String UKEY_SWITCH_STATUS = "UKEY_SWITCH_STATUS";
    private static final int UKEY_VERSION = SystemPropertiesEx.getInt(UKEY_VERSION_PRO, 1);
    public static final int UKEY_VERSION_1 = 1;
    private static final int UKEY_VERSION_ONE = 1;
    private static final String UKEY_VERSION_PRO = "ro.config.hw_ukey_version";
    public static final int UNSUPPORT_UKEY = 0;
    private static volatile UKeyManager sSelf = null;
    private IHwSEService mHwSEService = null;

    private UKeyManager() {
    }

    public static UKeyManager getInstance() {
        if (sSelf == null) {
            synchronized (UKeyManager.class) {
                if (sSelf == null) {
                    sSelf = new UKeyManager();
                }
            }
        }
        return sSelf;
    }

    private IHwSEService getHwSEService() {
        IHwSEService iHwSEService;
        synchronized (SERVICE_SYNC) {
            HwSEServiceManager.initRemoteService(ActivityThreadEx.currentApplication().getApplicationContext());
            this.mHwSEService = HwSEServiceManager.getRemoteServiceInstance();
            if (this.mHwSEService != null) {
                Log.d(TAG, "getHwPanPayService successfully");
            } else {
                Log.e(TAG, "getHwPanPayService failed!!!!");
            }
            iHwSEService = this.mHwSEService;
        }
        return iHwSEService;
    }

    public int getSupportVersion() {
        if (UserHandleEx.getUserId(Binder.getCallingUid()) == 0 && IS_UKEY_SWITCH_ON) {
            return UKEY_VERSION;
        }
        return 0;
    }

    public int isUKeySwitchDisabled(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        IHwSEService hwSEService = getHwSEService();
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        }
        if (hwSEService != null) {
            try {
                return hwSEService.isUKeySwitchDisabled(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while getting ukey switch status.");
            }
        }
        return -1;
    }

    public int setUKeySwitchDisabled(String packageName, boolean isDisabled) {
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        IHwSEService hwSEService = getHwSEService();
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        }
        if (hwSEService != null) {
            try {
                return hwSEService.setUKeySwitchDisabled(packageName, isDisabled);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while settings ukey switch status.");
            }
        }
        return -1;
    }

    public Bundle getUKeyApkInfo(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString("packageName", BuildConfig.FLAVOR);
        bundle.putString(APK_NAME_CN, BuildConfig.FLAVOR);
        bundle.putString(APK_NAME_EN, BuildConfig.FLAVOR);
        bundle.putString(CERT_MGR_NAME, BuildConfig.FLAVOR);
        bundle.putInt(UKEY_SWITCH_STATUS, -1);
        return bundle;
    }
}
