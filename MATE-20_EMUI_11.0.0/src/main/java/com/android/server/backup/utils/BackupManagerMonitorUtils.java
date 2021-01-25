package com.android.server.backup.utils;

import android.app.backup.IBackupManagerMonitor;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.backup.BackupManagerService;

public class BackupManagerMonitorUtils {
    public static IBackupManagerMonitor monitorEvent(IBackupManagerMonitor monitor, int id, PackageInfo pkg, int category, Bundle extras) {
        if (monitor == null) {
            return null;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("android.app.backup.extra.LOG_EVENT_ID", id);
            bundle.putInt("android.app.backup.extra.LOG_EVENT_CATEGORY", category);
            if (pkg != null) {
                bundle.putString("android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", pkg.packageName);
                bundle.putInt("android.app.backup.extra.LOG_EVENT_PACKAGE_VERSION", pkg.versionCode);
                bundle.putLong("android.app.backup.extra.LOG_EVENT_PACKAGE_FULL_VERSION", pkg.getLongVersionCode());
            }
            if (extras != null) {
                bundle.putAll(extras);
            }
            monitor.onEvent(bundle);
            return monitor;
        } catch (RemoteException e) {
            Slog.w(BackupManagerService.TAG, "backup manager monitor went away");
            return null;
        }
    }

    public static Bundle putMonitoringExtra(Bundle extras, String key, String value) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putString(key, value);
        return extras;
    }

    public static Bundle putMonitoringExtra(Bundle extras, String key, long value) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putLong(key, value);
        return extras;
    }

    public static Bundle putMonitoringExtra(Bundle extras, String key, boolean value) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putBoolean(key, value);
        return extras;
    }
}
