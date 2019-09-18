package com.android.server.backup;

import android.app.IWallpaperManager;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FullBackup;
import android.app.backup.FullBackupDataOutput;
import android.app.backup.WallpaperBackupHelper;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import java.io.File;
import java.io.IOException;

public class SystemBackupAgent extends BackupAgentHelper {
    private static final String ACCOUNT_MANAGER_HELPER = "account_manager";
    private static final String NOTIFICATION_HELPER = "notifications";
    private static final String PERMISSION_HELPER = "permissions";
    private static final String PREFERRED_HELPER = "preferred_activities";
    private static final String SHORTCUT_MANAGER_HELPER = "shortcut_manager";
    private static final String SLICES_HELPER = "slices";
    private static final String SYNC_SETTINGS_HELPER = "account_sync_settings";
    private static final String TAG = "SystemBackupAgent";
    private static final String USAGE_STATS_HELPER = "usage_stats";
    private static final String WALLPAPER_HELPER = "wallpaper";
    public static final String WALLPAPER_IMAGE = new File(Environment.getUserSystemDirectory(0), "wallpaper").getAbsolutePath();
    private static final String WALLPAPER_IMAGE_DIR = Environment.getUserSystemDirectory(0).getAbsolutePath();
    private static final String WALLPAPER_IMAGE_FILENAME = "wallpaper";
    private static final String WALLPAPER_IMAGE_KEY = "/data/data/com.android.settings/files/wallpaper";
    public static final String WALLPAPER_INFO = new File(Environment.getUserSystemDirectory(0), WALLPAPER_INFO_FILENAME).getAbsolutePath();
    private static final String WALLPAPER_INFO_DIR = Environment.getUserSystemDirectory(0).getAbsolutePath();
    private static final String WALLPAPER_INFO_FILENAME = "wallpaper_info.xml";
    private WallpaperBackupHelper mWallpaperHelper = null;

    /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.backup.PreferredActivityBackupHelper, android.app.backup.BackupHelper] */
    /* JADX WARNING: type inference failed for: r1v2, types: [com.android.server.backup.NotificationBackupHelper, android.app.backup.BackupHelper] */
    /* JADX WARNING: type inference failed for: r1v3, types: [com.android.server.backup.PermissionBackupHelper, android.app.backup.BackupHelper] */
    /* JADX WARNING: type inference failed for: r1v4, types: [android.app.backup.BackupHelper, com.android.server.backup.UsageStatsBackupHelper] */
    /* JADX WARNING: type inference failed for: r1v5, types: [android.app.backup.BackupHelper, com.android.server.backup.ShortcutBackupHelper] */
    /* JADX WARNING: type inference failed for: r1v6, types: [android.app.backup.BackupHelper, com.android.server.backup.AccountManagerBackupHelper] */
    /* JADX WARNING: type inference failed for: r1v7, types: [com.android.server.backup.SliceBackupHelper, android.app.backup.BackupHelper] */
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        addHelper(SYNC_SETTINGS_HELPER, new AccountSyncSettingsBackupHelper(this));
        addHelper(PREFERRED_HELPER, new PreferredActivityBackupHelper());
        addHelper(NOTIFICATION_HELPER, new NotificationBackupHelper(this));
        addHelper(PERMISSION_HELPER, new PermissionBackupHelper());
        addHelper(USAGE_STATS_HELPER, new UsageStatsBackupHelper(this));
        addHelper(SHORTCUT_MANAGER_HELPER, new ShortcutBackupHelper());
        addHelper(ACCOUNT_MANAGER_HELPER, new AccountManagerBackupHelper());
        addHelper(SLICES_HELPER, new SliceBackupHelper(this));
        super.onBackup(oldState, data, newState);
    }

    public void onFullBackup(FullBackupDataOutput data) throws IOException {
    }

    /* JADX WARNING: type inference failed for: r1v5, types: [com.android.server.backup.PreferredActivityBackupHelper, android.app.backup.BackupHelper] */
    /* JADX WARNING: type inference failed for: r1v6, types: [com.android.server.backup.NotificationBackupHelper, android.app.backup.BackupHelper] */
    /* JADX WARNING: type inference failed for: r1v7, types: [com.android.server.backup.PermissionBackupHelper, android.app.backup.BackupHelper] */
    /* JADX WARNING: type inference failed for: r1v8, types: [android.app.backup.BackupHelper, com.android.server.backup.UsageStatsBackupHelper] */
    /* JADX WARNING: type inference failed for: r1v9, types: [android.app.backup.BackupHelper, com.android.server.backup.ShortcutBackupHelper] */
    /* JADX WARNING: type inference failed for: r1v10, types: [android.app.backup.BackupHelper, com.android.server.backup.AccountManagerBackupHelper] */
    /* JADX WARNING: type inference failed for: r1v11, types: [com.android.server.backup.SliceBackupHelper, android.app.backup.BackupHelper] */
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        this.mWallpaperHelper = new WallpaperBackupHelper(this, new String[]{WALLPAPER_IMAGE_KEY});
        addHelper("wallpaper", this.mWallpaperHelper);
        addHelper("system_files", new WallpaperBackupHelper(this, new String[]{WALLPAPER_IMAGE_KEY}));
        addHelper(SYNC_SETTINGS_HELPER, new AccountSyncSettingsBackupHelper(this));
        addHelper(PREFERRED_HELPER, new PreferredActivityBackupHelper());
        addHelper(NOTIFICATION_HELPER, new NotificationBackupHelper(this));
        addHelper(PERMISSION_HELPER, new PermissionBackupHelper());
        addHelper(USAGE_STATS_HELPER, new UsageStatsBackupHelper(this));
        addHelper(SHORTCUT_MANAGER_HELPER, new ShortcutBackupHelper());
        addHelper(ACCOUNT_MANAGER_HELPER, new AccountManagerBackupHelper());
        addHelper(SLICES_HELPER, new SliceBackupHelper(this));
        super.onRestore(data, appVersionCode, newState);
    }

    public void onRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime) throws IOException {
        String str = domain;
        String str2 = path;
        Slog.i(TAG, "Restoring file domain=" + str + " path=" + str2);
        boolean restoredWallpaper = false;
        File outFile = null;
        if (str.equals("r")) {
            if (str2.equals(WALLPAPER_INFO_FILENAME)) {
                outFile = new File(WALLPAPER_INFO);
                restoredWallpaper = true;
            } else if (str2.equals("wallpaper")) {
                outFile = new File(WALLPAPER_IMAGE);
                restoredWallpaper = true;
            }
        }
        boolean restoredWallpaper2 = restoredWallpaper;
        if (outFile == null) {
            try {
                Slog.w(TAG, "Skipping unrecognized system file: [ " + str + " : " + str2 + " ]");
            } catch (IOException e) {
                if (restoredWallpaper2) {
                    new File(WALLPAPER_IMAGE).delete();
                    new File(WALLPAPER_INFO).delete();
                    return;
                }
                return;
            }
        }
        FullBackup.restoreFile(data, size, type, mode, mtime, outFile);
        if (restoredWallpaper2) {
            IWallpaperManager wallpaper = ServiceManager.getService("wallpaper");
            if (wallpaper != null) {
                try {
                    wallpaper.settingsRestored();
                } catch (RemoteException re) {
                    RemoteException remoteException = re;
                    Slog.e(TAG, "Couldn't restore settings\n" + re);
                }
            }
        }
    }
}
