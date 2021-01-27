package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ContentProviderHolder;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HwAdbManager {
    private static final String ADB_INSTALL_NEED_CONFIRM_KEY = "adb_install_need_confirm";
    private static final int SETTING_READ = 0;
    private static final int SETTING_WRITE = 1;
    private static final String TAG = "HwAdbManager";
    private static String sHdbKey = "";

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
        if ("".equals(randstr)) {
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
            return hdbEncode.equals(String.format("%0" + (encodeArray.length << 1) + "x", new BigInteger(1, encodeArray)));
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "hdb verify, key check error!");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "hdb verify error!");
            return false;
        }
    }

    public static boolean startPackageInstallerForConfirm(Context context, int sessionId) {
        String str;
        int startResult;
        Intent intent = new Intent("android.content.pm.action.CONFIRM_INSTALL");
        intent.setPackage("com.android.packageinstaller");
        intent.putExtra("android.content.pm.extra.SESSION_ID", sessionId);
        intent.putExtra("hw_adb_install", true);
        intent.setFlags(268468224);
        IActivityManager nativeAM = ActivityManagerNative.getDefault();
        if (nativeAM == null) {
            try {
                Log.i(TAG, "start PackageInstallerActivity failed because nativeAM = null");
                return false;
            } catch (RemoteException e) {
                str = TAG;
                Log.e(str, "Fail to PackageInstallerActivity, RemoteException!");
                return false;
            }
        } else {
            UserInfo userInfo = nativeAM.getCurrentUser();
            if (userInfo != null) {
                int i = userInfo.id;
                str = TAG;
                try {
                    startResult = nativeAM.startActivityAsUser((IApplicationThread) null, "adb", intent, (String) null, (IBinder) null, (String) null, 0, 0, (ProfilerInfo) null, (Bundle) null, i);
                } catch (RemoteException e2) {
                    Log.e(str, "Fail to PackageInstallerActivity, RemoteException!");
                    return false;
                }
            } else {
                str = TAG;
                startResult = nativeAM.startActivity((IApplicationThread) null, "adb", intent, (String) null, (IBinder) null, (String) null, 0, 0, (ProfilerInfo) null, (Bundle) null);
            }
            if (startResult == 0) {
                return true;
            }
            Log.i(str, "start PackageInstallerActivity failed [" + startResult + "]");
            return false;
        }
    }

    private static boolean isRepairMode() {
        IActivityManager am = ActivityManager.getService();
        if (am != null) {
            try {
                UserInfo userInfo = am.getCurrentUser();
                if (userInfo == null || !userInfo.isRepairMode()) {
                    return false;
                }
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "check isRepairMode error:" + e.getMessage());
            }
        }
        return false;
    }

    public static boolean autoPermitInstall() {
        String deviceRootValue = SystemProperties.get("ro.adb.secure");
        String vendor = SystemProperties.get("ro.hw.vendor", "");
        String country = SystemProperties.get("ro.hw.country", "");
        if ("".equals(deviceRootValue) || "0".equals(deviceRootValue)) {
            if ("cn".equals(country) && ("allcta".equals(vendor) || "cmcccta".equals(vendor))) {
                return false;
            }
            Log.v(TAG, "autoPermitInstall root device!");
            return true;
        } else if (isRepairMode()) {
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
        if (value != null) {
            try {
                if (1 != Integer.parseInt(value)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error while parsing the value of secureSettingRW from string to int.");
            }
        }
        return true;
    }

    private static String secureSettingRW(String key, String value, int rw) {
        try {
            IActivityManager activityManager = ActivityManagerNative.getDefault();
            IContentProvider provider = null;
            IBinder token = new Binder();
            try {
                ContentProviderHolder holder = activityManager.getContentProviderExternal("settings", 0, token, (String) null);
                if (holder != null) {
                    provider = holder.provider;
                    if (rw == 0) {
                        value = getForUser(provider, 0, key);
                    } else if (rw != 1) {
                        Log.e(TAG, "Unspecified command");
                    } else {
                        putForUser(provider, 0, key, value);
                    }
                    return value;
                }
                throw new IllegalStateException("Could not find settings provider");
            } finally {
                if (provider != null) {
                    activityManager.removeContentProviderExternal("settings", token);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while accessing settings provider, " + e.getMessage());
        }
    }

    private static String getForUser(IContentProvider provider, int userHandle, String key) {
        try {
            Bundle arg = new Bundle();
            arg.putInt("_user", userHandle);
            Bundle b = provider.call((String) null, "settings", "GET_secure", key, arg);
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
            provider.call((String) null, "settings", "PUT_secure", key, arg);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't set key " + key + " in secure for user " + userHandle);
        }
    }
}
