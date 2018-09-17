package com.android.commands.pm;

import android.app.ActivityManagerNative;
import android.app.ContentProviderHolder;
import android.app.IActivityManager;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver.Stub;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class HwPm {
    private static final String ADB_INSTALL_LAST_TIME_KEY = "adb_install_last_time";
    private static final String ADB_INSTALL_NEED_CONFIRM_KEY = "adb_install_need_confirm";
    private static final int FIVE_MINUTES_IN_MS = 300000;
    private static final String HDB_EMPTY_RAND_VALUE = "0000000000000000000000000000000000000000000000000000000000000000";
    private static final String HDB_RAND_KEY = "service.hdb.rand";
    private static final int SETTING_READ = 0;
    private static final int SETTING_WRITE = 1;
    private static final String TAG = "HwPm";

    static class PackageInstallObserver extends Stub {
        boolean finished;
        int result;

        PackageInstallObserver() {
        }

        public void packageInstalled(String name, int status) {
            synchronized (this) {
                this.finished = true;
                this.result = status;
                notifyAll();
            }
        }
    }

    HwPm() {
    }

    static boolean startHdbVerification(String[] args, int hdbArgIndex, String hdbEncode) {
        String randstr = SystemProperties.get(HDB_RAND_KEY, HDB_EMPTY_RAND_VALUE);
        SystemProperties.set(HDB_RAND_KEY, HDB_EMPTY_RAND_VALUE);
        if (HDB_EMPTY_RAND_VALUE.equals(randstr)) {
            Log.e(TAG, "startHdbVerification default empty rand value");
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            StringBuilder key = new StringBuilder(randstr + "=");
            for (int i = hdbArgIndex; i < args.length - 1; i += SETTING_WRITE) {
                key.append(args[i]).append(' ');
            }
            key.append(args[args.length - 1]);
            byte[] encodeArray = digest.digest(key.toString().getBytes());
            String str = "%0" + (encodeArray.length << SETTING_WRITE) + "x";
            Object[] objArr = new Object[SETTING_WRITE];
            objArr[SETTING_READ] = new BigInteger(SETTING_WRITE, encodeArray);
            if (hdbEncode.equals(String.format(str, objArr))) {
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return false;
    }

    static boolean startPackageInstallerForConfirm(Uri apkURI, String installerPackageName, int installFlags) {
        if (SystemProperties.getBoolean("ro.config.hwRemoveADBMonitor", false) || autoPermitInstall()) {
            return true;
        }
        IActivityManager nativeAM = ActivityManagerNative.getDefault();
        Intent packageInstallIntent = new Intent("android.intent.action.VIEW");
        packageInstallIntent.setClassName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity");
        packageInstallIntent.setDataAndType(apkURI, "application/vnd.android.package-archive");
        Log.v(TAG, "startPackageInstallerForConfirm apkURI:" + apkURI + ", installerPackageName:" + installerPackageName + ", installFlags:" + installFlags);
        packageInstallIntent.putExtra("adb_install", true);
        PackageInstallObserver obs = new PackageInstallObserver();
        packageInstallIntent.putExtra("observer", obs);
        packageInstallIntent.setFlags(268468224);
        packageInstallIntent.putExtra("installerPackageName", installerPackageName);
        packageInstallIntent.putExtra("installFlags", installFlags);
        try {
            int j = nativeAM.startActivity(null, "pm", packageInstallIntent, null, null, null, SETTING_READ, SETTING_READ, null, null);
            if (j != 0) {
                Log.i(TAG, "start PackageInstallerActivity failed [" + j + "]");
                return true;
            }
            synchronized (obs) {
                while (!obs.finished) {
                    try {
                        obs.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (obs.result == 0) {
                    writeLastPermitTime(System.currentTimeMillis());
                    return true;
                }
                return false;
            }
        } catch (RemoteException remoteEx) {
            remoteEx.printStackTrace();
            return true;
        }
    }

    private static boolean autoPermitInstall() {
        if (!"0".equals(SystemProperties.get("ro.adb.secure")) || Process.myUid() != 0) {
            return !checkSwitchIsOpen() || autoPermitTimeCheck();
        } else {
            Log.v(TAG, "autoPermitInstall root device!");
            return true;
        }
    }

    private static boolean checkSwitchIsOpen() {
        String value = secureSettingRW(ADB_INSTALL_NEED_CONFIRM_KEY, null, SETTING_READ);
        return value == null || SETTING_WRITE == Integer.parseInt(value);
    }

    private static boolean autoPermitTimeCheck() {
        long curTime = System.currentTimeMillis();
        long diff = curTime - readLastPermitTime();
        if (0 >= diff || 300000 <= diff) {
            return false;
        }
        writeLastPermitTime(curTime);
        return true;
    }

    private static long readLastPermitTime() {
        String time = secureSettingRW(ADB_INSTALL_LAST_TIME_KEY, null, SETTING_READ);
        if (time != null) {
            try {
                return Long.parseLong(time);
            } catch (Exception e) {
                Log.e(TAG, "Error while parse ADB_INSTALL_LAST_TIME_KEY value");
                e.printStackTrace();
            }
        }
        return 0;
    }

    private static void writeLastPermitTime(long time) {
        secureSettingRW(ADB_INSTALL_LAST_TIME_KEY, String.valueOf(time), SETTING_WRITE);
    }

    private static String secureSettingRW(String key, String value, int rw) {
        IActivityManager activityManager;
        IContentProvider provider;
        IBinder token;
        try {
            activityManager = ActivityManagerNative.getDefault();
            provider = null;
            token = new Binder();
            ContentProviderHolder holder = activityManager.getContentProviderExternal("settings", SETTING_READ, token);
            if (holder == null) {
                throw new IllegalStateException("Could not find settings provider");
            }
            provider = holder.provider;
            switch (rw) {
                case SETTING_READ /*0*/:
                    value = getForUser(provider, SETTING_READ, key);
                    break;
                case SETTING_WRITE /*1*/:
                    putForUser(provider, SETTING_READ, key, value);
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
