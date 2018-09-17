package com.android.server.pm;

import android.app.ActivityManagerNative;
import android.app.ContentProviderHolder;
import android.app.IActivityManager;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.app.PackageManagerEx;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HwAdbManager {
    private static final String ADB_INSTALL_NEED_CONFIRM_KEY = "adb_install_need_confirm";
    private static final int SETTING_READ = 0;
    private static final int SETTING_WRITE = 1;
    private static final String TAG = "HwAdbManager";

    static boolean startHdbVerification(String[] args, int hdbArgIndex, String hdbEncode) {
        String randstr = PackageManagerEx.getHdbKey();
        if ("".equals(randstr)) {
            Log.e(TAG, "startHdbVerification default empty rand value");
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            StringBuilder key = new StringBuilder(randstr + "=");
            for (int i = hdbArgIndex; i < args.length - 1; i++) {
                key.append(args[i]).append(' ');
            }
            key.append(args[args.length - 1]);
            return hdbEncode.equals(String.format("%0" + (digest.digest(key.toString().getBytes()).length << 1) + "x", new Object[]{new BigInteger(1, encodeArray)}));
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "hdb verify, key check error!");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "hdb verify error!");
            return false;
        }
    }

    public static boolean startPackageInstallerForConfirm(Context context, int sessionId) {
        Intent intent = new Intent("android.content.pm.action.CONFIRM_PERMISSIONS");
        intent.setPackage("com.android.packageinstaller");
        intent.putExtra("android.content.pm.extra.SESSION_ID", sessionId);
        intent.putExtra("hw_adb_install", true);
        intent.setFlags(268468224);
        try {
            int j = ActivityManagerNative.getDefault().startActivity(null, "adb", intent, null, null, null, 0, 0, null, null);
            if (j == 0) {
                return true;
            }
            Log.i(TAG, "start PackageInstallerActivity failed [" + j + "]");
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Fail to PackageInstallerActivity, RemoteException!");
            return false;
        }
    }

    public static boolean autoPermitInstall() {
        String deviceRootValue = SystemProperties.get("ro.adb.secure");
        if ("".equals(deviceRootValue) || "0".equals(deviceRootValue)) {
            Log.v(TAG, "autoPermitInstall root device!");
            return true;
        } else if (checkSwitchIsOpen()) {
            return false;
        } else {
            Log.v(TAG, "autoPermitInstall switch is open!");
            return true;
        }
    }

    private static boolean checkSwitchIsOpen() {
        String value = secureSettingRW(ADB_INSTALL_NEED_CONFIRM_KEY, null, 0);
        return value == null || 1 == Integer.parseInt(value);
    }

    private static String secureSettingRW(String key, String value, int rw) {
        IActivityManager activityManager;
        IContentProvider provider;
        IBinder token;
        try {
            activityManager = ActivityManagerNative.getDefault();
            provider = null;
            token = new Binder();
            ContentProviderHolder holder = activityManager.getContentProviderExternal("settings", 0, token);
            if (holder == null) {
                throw new IllegalStateException("Could not find settings provider");
            }
            provider = holder.provider;
            switch (rw) {
                case 0:
                    value = getForUser(provider, 0, key);
                    break;
                case 1:
                    putForUser(provider, 0, key, value);
                    break;
                default:
                    Log.e(TAG, "Unspecified command");
                    break;
            }
            if (provider != null) {
                activityManager.removeContentProviderExternal("settings", token);
            }
            return value;
        } catch (Exception e) {
            Log.e(TAG, "Error while accessing settings provider");
        } catch (Throwable th) {
            if (provider != null) {
                activityManager.removeContentProviderExternal("settings", token);
            }
        }
    }

    private static String getForUser(IContentProvider provider, int userHandle, String key) {
        try {
            Bundle arg = new Bundle();
            arg.putInt("_user", userHandle);
            Bundle b = provider.call(null, "GET_secure", key, arg);
            if (b != null) {
                return b.getPairValue();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "Can't read key " + key + " in secure for user " + userHandle);
            return null;
        }
    }

    private static void putForUser(IContentProvider provider, int userHandle, String key, String value) {
        try {
            Bundle arg = new Bundle();
            arg.putString("value", value);
            arg.putInt("_user", userHandle);
            provider.call(null, "PUT_secure", key, arg);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't set key " + key + " in secure for user " + userHandle);
        }
    }
}
