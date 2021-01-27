package com.android.server.backup;

import android.app.IWallpaperManager;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupHelper;
import android.app.backup.FullBackup;
import android.app.backup.FullBackupDataOutput;
import android.app.backup.WallpaperBackupHelper;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Slog;
import com.google.android.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Set;

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
    private static final Set<String> sEligibleForMultiUser = Sets.newArraySet(PERMISSION_HELPER, NOTIFICATION_HELPER, SYNC_SETTINGS_HELPER);
    private int mUserId = 0;

    @Override // android.app.backup.BackupAgent
    public void onCreate(UserHandle user) {
        super.onCreate(user);
        this.mUserId = user.getIdentifier();
        addHelper(SYNC_SETTINGS_HELPER, new AccountSyncSettingsBackupHelper(this, this.mUserId));
        addHelper(PREFERRED_HELPER, new PreferredActivityBackupHelper());
        addHelper(NOTIFICATION_HELPER, new NotificationBackupHelper(this.mUserId));
        addHelper(PERMISSION_HELPER, new PermissionBackupHelper(this.mUserId));
        addHelper(USAGE_STATS_HELPER, new UsageStatsBackupHelper(this));
        addHelper(SHORTCUT_MANAGER_HELPER, new ShortcutBackupHelper());
        addHelper(ACCOUNT_MANAGER_HELPER, new AccountManagerBackupHelper());
        addHelper(SLICES_HELPER, new SliceBackupHelper(this));
    }

    @Override // android.app.backup.BackupAgent
    public void onFullBackup(FullBackupDataOutput data) throws IOException {
    }

    @Override // android.app.backup.BackupAgentHelper, android.app.backup.BackupAgent
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        addHelper("wallpaper", new WallpaperBackupHelper(this, new String[]{"/data/data/com.android.settings/files/wallpaper"}));
        addHelper("system_files", new WallpaperBackupHelper(this, new String[]{"/data/data/com.android.settings/files/wallpaper"}));
        super.onRestore(data, appVersionCode, newState);
    }

    @Override // android.app.backup.BackupAgentHelper
    public void addHelper(String keyPrefix, BackupHelper helper) {
        if (this.mUserId == 0 || sEligibleForMultiUser.contains(keyPrefix)) {
            super.addHelper(keyPrefix, helper);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0058 A[SYNTHETIC, Splitter:B:10:0x0058] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x008a A[Catch:{ IOException -> 0x00af }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:? A[RETURN, SYNTHETIC] */
    @Override // android.app.backup.BackupAgent
    public void onRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime) throws IOException {
        boolean restoredWallpaper;
        Slog.i(TAG, "Restoring file domain=" + domain + " path=" + path);
        File outFile = null;
        if (domain.equals("r")) {
            if (path.equals(WALLPAPER_INFO_FILENAME)) {
                outFile = new File(WALLPAPER_INFO);
                restoredWallpaper = true;
            } else if (path.equals("wallpaper")) {
                outFile = new File(WALLPAPER_IMAGE);
                restoredWallpaper = true;
            }
            if (outFile == null) {
                try {
                    Slog.w(TAG, "Skipping unrecognized system file: [ " + domain + " : " + path + " ]");
                } catch (IOException e) {
                    if (restoredWallpaper) {
                        new File(WALLPAPER_IMAGE).delete();
                        new File(WALLPAPER_INFO).delete();
                        return;
                    }
                    return;
                }
            }
            FullBackup.restoreFile(data, size, type, mode, mtime, outFile);
            if (!restoredWallpaper) {
                IWallpaperManager wallpaper = (IWallpaperManager) ServiceManager.getService("wallpaper");
                if (wallpaper != null) {
                    try {
                        wallpaper.settingsRestored();
                        return;
                    } catch (RemoteException re) {
                        Slog.e(TAG, "Couldn't restore settings\n" + re);
                        return;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        restoredWallpaper = false;
        if (outFile == null) {
        }
        FullBackup.restoreFile(data, size, type, mode, mtime, outFile);
        if (!restoredWallpaper) {
        }
    }
}
