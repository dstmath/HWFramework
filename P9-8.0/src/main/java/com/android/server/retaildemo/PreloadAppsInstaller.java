package com.android.server.retaildemo;

import android.app.AppGlobals;
import android.app.PackageInstallObserver;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

class PreloadAppsInstaller {
    private static boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String PRELOAD_APK_EXT = ".apk.preload";
    private static final String SYSTEM_SERVER_PACKAGE_NAME = "android";
    private static String TAG = PreloadAppsInstaller.class.getSimpleName();
    private final Map<String, String> mApkToPackageMap;
    private final Context mContext;
    private final IPackageManager mPackageManager;
    private final File preloadsAppsDirectory;

    private static class AppInstallCounter {
        private int expectedCount = -1;
        private int finishedCount;
        private final Context mContext;
        private final int userId;

        AppInstallCounter(Context context, int userId) {
            this.mContext = context;
            this.userId = userId;
        }

        synchronized void appInstallFinished() {
            this.finishedCount++;
            checkIfAllFinished();
        }

        synchronized void setExpectedAppsCount(int expectedCount) {
            this.expectedCount = expectedCount;
            checkIfAllFinished();
        }

        private void checkIfAllFinished() {
            if (this.expectedCount == this.finishedCount) {
                Log.i(PreloadAppsInstaller.TAG, "All preloads finished installing for user " + this.userId);
                Secure.putStringForUser(this.mContext.getContentResolver(), "demo_user_setup_complete", "1", this.userId);
            }
        }
    }

    PreloadAppsInstaller(Context context) {
        this(context, AppGlobals.getPackageManager(), Environment.getDataPreloadsAppsDirectory());
    }

    PreloadAppsInstaller(Context context, IPackageManager packageManager, File preloadsAppsDirectory) {
        this.mContext = context;
        this.mPackageManager = packageManager;
        this.mApkToPackageMap = Collections.synchronizedMap(new ArrayMap());
        this.preloadsAppsDirectory = preloadsAppsDirectory;
    }

    void installApps(int userId) {
        int i = 0;
        File[] files = this.preloadsAppsDirectory.listFiles();
        AppInstallCounter counter = new AppInstallCounter(this.mContext, userId);
        if (ArrayUtils.isEmpty(files)) {
            counter.setExpectedAppsCount(0);
            return;
        }
        int expectedCount = 0;
        int length = files.length;
        while (i < length) {
            File file = files[i];
            String apkName = file.getName();
            if (apkName.endsWith(PRELOAD_APK_EXT) && file.isFile()) {
                String packageName = (String) this.mApkToPackageMap.get(apkName);
                if (packageName != null) {
                    expectedCount++;
                    try {
                        installExistingPackage(packageName, userId, counter);
                    } catch (Exception e) {
                        Slog.e(TAG, "Failed to install existing package " + packageName, e);
                    }
                } else {
                    try {
                        installPackage(file, userId, counter);
                        expectedCount++;
                    } catch (Exception e2) {
                        Slog.e(TAG, "Failed to install package from " + file, e2);
                    }
                }
            }
            i++;
        }
        counter.setExpectedAppsCount(expectedCount);
    }

    private void installExistingPackage(String packageName, int userId, AppInstallCounter counter) {
        if (DEBUG) {
            Log.d(TAG, "installExistingPackage " + packageName + " u" + userId);
        }
        try {
            this.mPackageManager.installExistingPackageAsUser(packageName, userId, 0, 0);
            counter.appInstallFinished();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            counter.appInstallFinished();
        }
    }

    private void installPackage(File file, final int userId, final AppInstallCounter counter) throws IOException, RemoteException {
        final String apkName = file.getName();
        if (DEBUG) {
            Log.d(TAG, "installPackage " + apkName + " u" + userId);
        }
        this.mPackageManager.installPackageAsUser(file.getPath(), new PackageInstallObserver() {
            public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
                if (PreloadAppsInstaller.DEBUG) {
                    Log.d(PreloadAppsInstaller.TAG, "Package " + basePackageName + " installed u" + userId + " returnCode: " + returnCode + " msg: " + msg);
                }
                if (returnCode == 1) {
                    PreloadAppsInstaller.this.mApkToPackageMap.put(apkName, basePackageName);
                    PreloadAppsInstaller.this.installExistingPackage(basePackageName, 0, counter);
                } else if (returnCode == -1) {
                    if (!PreloadAppsInstaller.this.mApkToPackageMap.containsKey(apkName)) {
                        PreloadAppsInstaller.this.mApkToPackageMap.put(apkName, basePackageName);
                    }
                    PreloadAppsInstaller.this.installExistingPackage(basePackageName, userId, counter);
                } else {
                    Log.e(PreloadAppsInstaller.TAG, "Package " + basePackageName + " cannot be installed from " + apkName + ": " + msg + " (returnCode " + returnCode + ")");
                    counter.appInstallFinished();
                }
            }
        }.getBinder(), 0, SYSTEM_SERVER_PACKAGE_NAME, userId);
    }
}
