package com.android.server.pm;

import android.content.Context;
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
    private static final String HWT_PATH_MAGAZINE = (Environment.getDataDirectory() + "/magazine");
    private static final int MAX_THEME_SIZE = 100000000;
    private static final String TAG = "HwThemeInstaller";
    private static volatile HwThemeInstaller mInstance;
    final Context mContext;
    private HwCustHwPackageManagerService mCustHwPms = ((HwCustHwPackageManagerService) HwCustUtils.createObj(HwCustHwPackageManagerService.class, new Object[0]));

    private HwThemeInstaller(Context context) {
        this.mContext = context;
    }

    public static HwThemeInstaller getInstance(Context context) {
        if (mInstance == null) {
            synchronized (HwThemeInstaller.class) {
                if (mInstance == null) {
                    mInstance = new HwThemeInstaller(context);
                }
            }
        }
        return mInstance;
    }

    /* access modifiers changed from: package-private */
    public boolean pmInstallHwTheme(String themePath, boolean setwallpaper, int userId) {
        Log.w(TAG, "pmInstallHwTheme, themePath:" + themePath + " ;setwallpaper:" + setwallpaper + " ;user:" + userId);
        if (TextUtils.isEmpty(themePath)) {
            Log.w(TAG, "themePath is null, themePath:" + themePath);
            return false;
        }
        if (themePath.startsWith("/data/themes")) {
            Log.w(TAG, "install online/download theme, themePath:" + themePath);
        } else if (themePath.startsWith("/data/hw_init")) {
            Log.w(TAG, "install local theme, themePath:" + themePath);
        } else {
            Log.w(TAG, "other message, themePath:" + themePath);
        }
        if (setwallpaper) {
            rmSysWallpaper();
        }
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_MEDIA_STORAGE") == 0) {
            File themeFile = new File(themePath);
            if (!themeFile.exists()) {
                Log.w(TAG, "install theme failed, " + themePath + " not found");
                try {
                    createThemeFolder(userId);
                    restoreThemeCon(userId);
                } catch (Exception e) {
                    Log.w(TAG, "create theme folder failed, ", e);
                    deleteThemeTempFolder();
                }
                return false;
            } else if (((int) themeFile.length()) > MAX_THEME_SIZE || ZipUtil.isZipError(themePath)) {
                return false;
            } else {
                try {
                    createThemeFolder(userId);
                    if (userId != 0 || !isDataSkinExists()) {
                        createThemeTempFolder();
                        unzipThemePackage(themeFile);
                        unzipCustThemePackage(getCustThemePath(themePath));
                        renameThemeTempFolder(userId);
                        renameKeyguardFile(userId);
                    } else {
                        renameDataSkinFolder(userId);
                    }
                    restoreThemeCon(userId);
                    deleteInstallFlag();
                    if (this.mCustHwPms != null && this.mCustHwPms.isSupportThemeRestore()) {
                        this.mCustHwPms.changeTheme(themePath, this.mContext);
                    }
                    return true;
                } catch (Exception e2) {
                    Log.w(TAG, "install theme failed, ", e2);
                    deleteThemeTempFolder();
                    return false;
                }
            }
        } else {
            throw new SecurityException("Permission Denial: can't install theme from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.WRITE_MEDIA_STORAGE");
        }
    }

    private void unzipCustThemePackage(File custThemeFile) {
        if (custThemeFile != null && custThemeFile.exists()) {
            unzipThemePackage(custThemeFile);
        }
    }

    private File getCustThemePath(String path) {
        File custDiffFile = null;
        if (path == null) {
            return null;
        }
        String[] paths = path.split("/");
        String themeName = paths[paths.length - 1];
        String diffThemePath = SystemProperties.get("ro.config.diff_themes");
        if (!TextUtils.isEmpty(diffThemePath)) {
            return new File(diffThemePath + "/" + themeName);
        }
        try {
            custDiffFile = HwCfgFilePolicy.getCfgFile("themes/diff/" + themeName, 0);
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        return custDiffFile;
    }

    private String getHwThemePathAsUser(int userId) {
        return HwThemeManager.HWT_PATH_THEME + "/" + userId;
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
        boolean restoreconResult = SELinux.restorecon(dir);
        Slog.i(TAG, "createMagazineFolder Magazine Folder restoreconResult " + restoreconResult);
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
            CommandLineUtil.mv("system", getHwThemePathAsUser(userId) + "/" + "com.huawei.android.hwlockscreen", getHwThemePathAsUser(userId) + "/" + "com.android.keyguard");
        }
    }

    private boolean rmSysWallpaper() {
        if (new File("/data/system/users/0/", "wallpaper").exists()) {
            CommandLineUtil.rm("system", "/data/system/users/0/wallpaper");
            if (this.mCustHwPms != null && this.mCustHwPms.isSupportThemeRestore()) {
                CommandLineUtil.rm("system", "/data/system/users/0/wallpaper_orig");
            }
        }
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
}
