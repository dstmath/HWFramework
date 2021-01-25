package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hdm.HwDeviceManager;
import android.hwtheme.HwThemeManager;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.SELinux;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.huawei.android.hwutil.CommandLineUtil;
import com.huawei.cust.HwCustUtils;
import huawei.android.hwutil.ZipUtil;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;

public class HwThemeInstaller {
    private static final int FLAG_LEAVE_UNCHANGED = -1;
    private static final String FLAG_SPLIT = "/";
    public static final String HWT_NEW_CONTACT = "com.huawei.contacts";
    public static final String HWT_OLD_CONTACT = "com.android.contacts";
    private static final String HWT_PATH_MAGAZINE = (Environment.getDataDirectory() + "/magazine");
    private static final long MAX_THEME_SIZE = 100000000;
    private static final String TAG = "HwThemeInstaller";
    private static volatile HwThemeInstaller sInstance;
    final Context mContext;
    private HwCustHwPackageManagerService mCustHwPms = ((HwCustHwPackageManagerService) HwCustUtils.createObj(HwCustHwPackageManagerService.class, new Object[0]));

    private HwThemeInstaller(Context context) {
        this.mContext = context;
    }

    public static synchronized HwThemeInstaller getInstance(Context context) {
        HwThemeInstaller hwThemeInstaller;
        synchronized (HwThemeInstaller.class) {
            if (sInstance == null) {
                sInstance = new HwThemeInstaller(context);
            }
            hwThemeInstaller = sInstance;
        }
        return hwThemeInstaller;
    }

    /* access modifiers changed from: package-private */
    public boolean pmInstallHwTheme(String themePath, boolean isSetwallpaper, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_MEDIA_STORAGE") == 0) {
            Log.w(TAG, "pmInstallHwTheme, themePath:" + themePath + " ;isSetwallpaper:" + isSetwallpaper + " ;user:" + userId);
            if (!checkInstallThemePath(themePath)) {
                return false;
            }
            if (isSetwallpaper) {
                rmSysWallpaper();
            }
            File themeFile = new File(themePath);
            if (!checkThemeFile(themeFile, themePath, userId)) {
                return false;
            }
            try {
                createThemeFolder(userId);
                if (userId != 0 || !isDataSkinExists()) {
                    createThemeTempFolder();
                    unzipThemePackage(themeFile);
                    if (HwDeviceManager.disallowOp(35)) {
                        moveOldHomeWallpaper();
                    }
                    unzipCustThemePackage(getCustThemePath(themePath));
                    renameThemeTempFolder(userId);
                    renameKeyguardFile(userId);
                    renameContactFile(userId);
                } else {
                    renameDataSkinFolder(userId);
                }
                restoreThemeCon(userId);
                deleteInstallFlag();
                if (this.mCustHwPms == null || !this.mCustHwPms.isSupportThemeRestore()) {
                    return true;
                }
                this.mCustHwPms.changeTheme(themePath, this.mContext);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "install theme failed. Exception!");
                deleteThemeTempFolder();
                return false;
            }
        } else {
            throw new SecurityException("Permission Denial: can't install theme from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission android.permission.WRITE_MEDIA_STORAGE");
        }
    }

    private boolean checkInstallThemePath(String themePath) {
        if (TextUtils.isEmpty(themePath)) {
            Log.w(TAG, "themePath is null, themePath:" + themePath);
            return false;
        } else if (themePath.startsWith("/data/themes")) {
            Log.w(TAG, "install online/download theme, themePath:" + themePath);
            return true;
        } else if (themePath.startsWith("/data/hw_init")) {
            Log.w(TAG, "install local theme, themePath:" + themePath);
            return true;
        } else {
            Log.w(TAG, "other message, themePath:" + themePath);
            return true;
        }
    }

    private boolean checkThemeFile(File themeFile, String themePath, int userId) {
        if (themeFile == null || !themeFile.exists()) {
            Log.w(TAG, "install theme failed, " + themePath + " not found");
            try {
                createThemeFolder(userId);
                restoreThemeCon(userId);
            } catch (Exception e) {
                Log.w(TAG, "create theme folder failed, Exception.");
                deleteThemeTempFolder();
            }
            return false;
        } else if (themeFile.length() <= MAX_THEME_SIZE && !ZipUtil.isZipError(themePath)) {
            return true;
        } else {
            return false;
        }
    }

    private void unzipCustThemePackage(File custThemeFile) {
        if (custThemeFile != null && custThemeFile.exists()) {
            unzipThemePackage(custThemeFile);
        }
    }

    private File getCustThemePath(String path) {
        if (path == null) {
            return null;
        }
        String[] paths = path.split(FLAG_SPLIT);
        String themeName = paths[paths.length - 1];
        String diffThemePath = SystemProperties.get("ro.config.diff_themes");
        if (!TextUtils.isEmpty(diffThemePath)) {
            return new File(diffThemePath + FLAG_SPLIT + themeName);
        }
        try {
            return HwCfgFilePolicy.getCfgFile("themes/diff/" + themeName, 0);
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return null;
        }
    }

    private String getHwThemePathAsUser(int userId) {
        return HwThemeManager.HWT_PATH_THEME + FLAG_SPLIT + userId;
    }

    private boolean isDataSkinExists() {
        File file = new File(HwThemeManager.HWT_PATH_SKIN);
        if (!file.exists()) {
            return false;
        }
        try {
            return file.getCanonicalPath().equals(new File(file.getParentFile().getCanonicalFile(), file.getName()).getPath());
        } catch (IOException e) {
            return false;
        }
    }

    private void renameDataSkinFolder(int userId) {
        CommandLineUtil.rm("system", getHwThemePathAsUser(userId));
        CommandLineUtil.mv("system", HwThemeManager.HWT_PATH_SKIN, getHwThemePathAsUser(userId));
        CommandLineUtil.chmod("system", "0775", getHwThemePathAsUser(userId));
    }

    private void createFolder(String dir) {
        CommandLineUtil.mkdir("system", dir);
        CommandLineUtil.chmod("system", "0775", dir);
    }

    private void restoreThemeCon(int userId) {
        File themePath = new File(getHwThemePathAsUser(userId));
        if (themePath.exists() && !SELinux.restoreconRecursive(themePath)) {
            Log.w(TAG, "restoreconRecursive HWT_PATH_SKIN failed!");
        }
    }

    private void createThemeFolder(int userId) {
        createFolder(HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG);
        createFolder(HwThemeManager.HWT_PATH_THEME);
        createFolder(getHwThemePathAsUser(userId));
    }

    /* access modifiers changed from: package-private */
    public void createMagazineFolder() {
        String dir = HWT_PATH_MAGAZINE;
        if (new File(dir).exists()) {
            Slog.i(TAG, " Magazine Folder already exist return");
            return;
        }
        Slog.i(TAG, "createMagazineFolder Magazine Folder ing");
        CommandLineUtil.mkdir("system", dir);
        CommandLineUtil.chown("system", "system", "media_rw", dir);
        FileUtils.setPermissions(dir, 504, -1, -1);
        boolean isRestoreconResult = SELinux.restorecon(dir);
        Slog.i(TAG, "createMagazineFolder Magazine Folder isRestoreconResult " + isRestoreconResult);
    }

    private void createThemeTempFolder() {
        CommandLineUtil.rm("system", "/data/skin.tmp");
        CommandLineUtil.mkdir("system", "/data/skin.tmp");
        CommandLineUtil.chmod("system", "0775", "/data/skin.tmp");
    }

    private void deleteThemeTempFolder() {
        CommandLineUtil.rm("system", "/data/skin.tmp");
        deleteInstallFlag();
    }

    private void deleteInstallFlag() {
        if (new File(HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG).exists()) {
            CommandLineUtil.rm("system", HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG);
        }
    }

    private void renameThemeTempFolder(int userId) {
        CommandLineUtil.rm("system", getHwThemePathAsUser(userId));
        CommandLineUtil.mv("system", "/data/skin.tmp", getHwThemePathAsUser(userId));
    }

    private void unzipThemePackage(File themeFile) {
        ZipUtil.unZipFile(themeFile, "/data/skin.tmp");
        CommandLineUtil.chmod("system", "0775", "/data/skin.tmp");
    }

    private void renameKeyguardFile(int userId) {
        if (!new File(getHwThemePathAsUser(userId), "com.android.keyguard").exists() && new File(getHwThemePathAsUser(userId), "com.huawei.android.hwlockscreen").exists()) {
            CommandLineUtil.mv("system", getHwThemePathAsUser(userId) + FLAG_SPLIT + "com.huawei.android.hwlockscreen", getHwThemePathAsUser(userId) + FLAG_SPLIT + "com.android.keyguard");
        }
    }

    private void renameContactFile(int userId) {
        PackageInfo pi = null;
        try {
            pi = this.mContext.getPackageManager().getPackageInfo(HWT_NEW_CONTACT, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "renameContactFile NameNotFoundException.");
        }
        if (pi != null) {
            if (new File(getHwThemePathAsUser(userId), HWT_OLD_CONTACT).exists()) {
                CommandLineUtil.mv("system", getHwThemePathAsUser(userId) + FLAG_SPLIT + HWT_OLD_CONTACT, getHwThemePathAsUser(userId) + FLAG_SPLIT + HWT_NEW_CONTACT);
            }
        } else if (new File(getHwThemePathAsUser(userId), HWT_NEW_CONTACT).exists()) {
            CommandLineUtil.mv("system", getHwThemePathAsUser(userId) + FLAG_SPLIT + HWT_NEW_CONTACT, getHwThemePathAsUser(userId) + FLAG_SPLIT + HWT_OLD_CONTACT);
        }
    }

    private boolean rmSysWallpaper() {
        if (!new File("/data/system/users/0/", "wallpaper").exists()) {
            return true;
        }
        CommandLineUtil.rm("system", "/data/system/users/0/wallpaper");
        HwCustHwPackageManagerService hwCustHwPackageManagerService = this.mCustHwPms;
        if (hwCustHwPackageManagerService == null || !hwCustHwPackageManagerService.isSupportThemeRestore()) {
            return true;
        }
        CommandLineUtil.rm("system", "/data/system/users/0/wallpaper_orig");
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onUserRemoved(int userId) {
        if (userId >= 1) {
            deleteThemeUserFolder(userId);
        }
    }

    private void deleteThemeUserFolder(int userId) {
        CommandLineUtil.rm("system", getHwThemePathAsUser(userId));
    }

    private void moveOldHomeWallpaper() {
        File newWallpaperFile = new File("/data/skin.tmp/wallpaper/");
        if (newWallpaperFile.exists() || newWallpaperFile.mkdirs()) {
            CommandLineUtil.copyFolder("system", "/data/skin/wallpaper/", "/data/skin.tmp/wallpaper/");
            CommandLineUtil.chmod("system", "0775", "/data/skin.tmp/wallpaper/");
            return;
        }
        Log.w(TAG, "file mkdirs failed");
    }
}
