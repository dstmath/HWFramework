package com.android.server.pm;

import android.os.FileUtils;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.pm.Installer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HwBindApkFileManager {
    public static final String BIND_FILE_MAGIC_TXT = "B_i_n_D";
    private static final boolean IS_DEBUG = "on".equals(SystemProperties.get("ro.dbg.pms_log", "0"));
    private static final String TAG = "HwBindApkFileManager";
    private List<String> mNeedBindFiles;
    private IHwPackageManagerInner mPms = null;

    public HwBindApkFileManager(PackageManagerServiceEx pmsEx) {
        this.mPms = pmsEx.getPackageManagerSerivce();
    }

    @GuardedBy({"mPackages"})
    public void rebuildApkBindFile() {
        if (this.mPms != null) {
            Slog.i(TAG, "rebuildApkBindFile start");
            for (PackageSetting tmpPkg : this.mPms.getSettings().mPackages.values()) {
                String pkgId = tmpPkg.name;
                String disabledSysPackagesPath = this.mPms.getSettings().getDisabledSysPackagesPath(pkgId);
                if (!TextUtils.isEmpty(disabledSysPackagesPath) && !TextUtils.isEmpty(tmpPkg.codePathString)) {
                    doBindFileForPkg(pkgId, tmpPkg.codePathString, disabledSysPackagesPath);
                }
            }
            Slog.i(TAG, "rebuildApkBindFile end");
        }
    }

    private void doBindFileForPkg(String pkgId, String dataAppPath, String disabledSysPackagesPath) {
        Slog.w(TAG, pkgId + " Bind file handle dir: " + dataAppPath + ", link to " + disabledSysPackagesPath);
        this.mNeedBindFiles = new ArrayList();
        getAllFileNeedBind(dataAppPath, disabledSysPackagesPath, dataAppPath);
        for (String relativePath : this.mNeedBindFiles) {
            try {
                boolean isBindFileSuccess = this.mPms.getInstallerInner().bindFile(relativePath, disabledSysPackagesPath, dataAppPath);
                Slog.w(TAG, "Bind file " + relativePath + ": " + isBindFileSuccess);
            } catch (Installer.InstallerException e) {
                Slog.e(TAG, "Bind file: " + relativePath + " failed");
            }
        }
    }

    private void getAllFileNeedBind(String dataBase, String roBase, String findPath) {
        File[] allFiles = new File(findPath).listFiles();
        if (allFiles == null) {
            Slog.e(TAG, "list file return null: " + findPath);
            return;
        }
        for (File file : allFiles) {
            if (file.isDirectory()) {
                getAllFileNeedBind(dataBase, roBase, file.getPath());
            } else if (isBindFile(file)) {
                this.mNeedBindFiles.add(file.getPath().substring(dataBase.length()));
            }
        }
    }

    private static boolean isBindFile(File file) {
        try {
            if (file.length() != ((long) BIND_FILE_MAGIC_TXT.length()) || !BIND_FILE_MAGIC_TXT.equals(FileUtils.readTextFile(file, 0, null))) {
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
