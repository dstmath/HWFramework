package com.huawei.securitycenter;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import huawei.android.security.ISecurityCenterManager;

public class HwSecurityManager {
    private static final String SECURITY_CENTER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String TAG = "HwSecurityManager";
    private static final Uri URL_RECOGNITION_PROVIDER = Uri.parse("content://com.huawei.securitycenter.urlrecognitionprovider");
    private static final int URL_RESULT_ERROR = -2;
    private static final int URL_RESULT_NON_OFFICIAL = -1;
    private static volatile HwSecurityManager sInstance;

    private HwSecurityManager() {
    }

    public static HwSecurityManager getInstance() {
        if (sInstance == null) {
            synchronized (HwSecurityManager.class) {
                if (sInstance == null) {
                    sInstance = new HwSecurityManager();
                }
            }
        }
        return sInstance;
    }

    @Nullable
    public Bundle getSecureAbility(@NonNull String moduleName, @NonNull String methodName) {
        try {
            ISecurityCenterManager service = SecCenterServiceHolder.getSecurityCenterManager();
            if (service != null) {
                return service.getSecureAbility(moduleName, methodName);
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to call getSecureAbility");
            return null;
        }
    }

    @Nullable
    @RequiresPermission(SECURITY_CENTER_PERMISSION)
    public Bundle checkUninstalledApk(@NonNull String filePath, String sourcePkg, long timeOutMillis) {
        try {
            IHwSecService hwSecService = SecCenterServiceHolder.getHwSecService();
            if (hwSecService == null) {
                return null;
            }
            Bundle args = new Bundle();
            args.putString("filePath", filePath);
            args.putString("sourcePkg", sourcePkg);
            args.putLong("timeOutMillis", timeOutMillis);
            return hwSecService.call("checkUninstalledApk", args);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to call checkUninstalledApk");
            return null;
        }
    }

    @RequiresPermission(SECURITY_CENTER_PERMISSION)
    public int checkUrl(@NonNull Context context, @NonNull String url) {
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver != null) {
            try {
                Bundle retBundle = contentResolver.call(URL_RECOGNITION_PROVIDER, "checkUrlSecurity", url, (Bundle) null);
                if (retBundle == null) {
                    return -2;
                }
                return retBundle.getInt("result");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "checkUrlSecurity IllegalArgumentException");
            }
        }
        return -2;
    }

    @RequiresPermission(SECURITY_CENTER_PERMISSION)
    public void syncSecureAbility(Bundle abilities) {
        try {
            ISecurityCenterManager service = SecCenterServiceHolder.getSecurityCenterManager();
            if (service != null) {
                service.syncSecureAbility(abilities);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "failed to call syncSecureAbility");
        }
    }
}
