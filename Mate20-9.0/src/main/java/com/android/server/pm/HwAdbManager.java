package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ContentProviderHolder;
import android.app.IActivityManager;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HwAdbManager {
    private static final String ADB_INSTALL_NEED_CONFIRM_KEY = "adb_install_need_confirm";
    private static final int SETTING_READ = 0;
    private static final int SETTING_WRITE = 1;
    private static final String TAG = "HwAdbManager";
    private static String sHdbKey = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;

    public static void setHdbKey(String key) {
        if (key == null) {
            Log.d(TAG, "set HDB KEY is null, return!");
        } else {
            sHdbKey = key;
        }
    }

    public static String getHdbKey() {
        return sHdbKey;
    }

    static boolean startHdbVerification(String[] args, int hdbArgIndex, String hdbEncode) {
        String randstr = getHdbKey();
        if (BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(randstr)) {
            Log.e(TAG, "startHdbVerification default empty hdb rand value");
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            StringBuilder key = new StringBuilder(randstr + "=");
            for (int i = hdbArgIndex; i < args.length - 1; i++) {
                key.append(args[i]);
                key.append(' ');
            }
            key.append(args[args.length - 1]);
            byte[] encodeArray = digest.digest(key.toString().getBytes());
            return hdbEncode.equals(String.format("%0" + (encodeArray.length << 1) + "x", new Object[]{new BigInteger(1, encodeArray)}));
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

    private static boolean isRepairMode() {
        IActivityManager am = ActivityManager.getService();
        boolean z = false;
        if (am != null) {
            try {
                UserInfo userInfo = am.getCurrentUser();
                if (userInfo != null && userInfo.isRepairMode()) {
                    z = true;
                }
                return z;
            } catch (RemoteException e) {
                Log.e(TAG, "check isRepairMode error:" + e.getMessage());
            }
        }
        return false;
    }

    public static boolean autoPermitInstall() {
        if (isRepairMode()) {
            Log.v(TAG, "autoPermitInstall in repair mode!");
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
        if (value == null || 1 == Integer.parseInt(value)) {
            return true;
        }
        return false;
    }

    private static String secureSettingRW(String key, String value, int rw) {
        IActivityManager activityManager;
        IBinder token;
        try {
            activityManager = ActivityManagerNative.getDefault();
            token = new Binder();
            ContentProviderHolder holder = activityManager.getContentProviderExternal("settings", 0, token);
            if (holder != null) {
                IContentProvider provider = holder.provider;
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
            }
            throw new IllegalStateException("Could not find settings provider");
        } catch (Exception e) {
            Log.e(TAG, "Error while accessing settings provider");
        } catch (Throwable holder2) {
            if (0 != 0) {
                activityManager.removeContentProviderExternal("settings", token);
            }
            throw holder2;
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
