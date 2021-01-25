package com.android.server.pm;

import android.apex.HepInfo;
import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.huawei.android.content.pm.HwHepPackageInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwHepApplicationManager {
    public static final int DEFAULT_ERROR_STATE = -1;
    private static final boolean IS_DEBUG = SystemProperties.get("ro.dbg.pms_log", "0").equals("on");
    private static final String TAG = "HwHepApplicationManager";
    private final ApexManager mApexManager;
    private final Context mContext;

    public HwHepApplicationManager(Context context) {
        this.mContext = context;
        this.mApexManager = new ApexManager(context);
    }

    private String getHepFileName(File stageDir) {
        File[] files = stageDir.listFiles();
        if (files == null) {
            return null;
        }
        for (File getFile : files) {
            if (getFile.isFile() && HwPackageManagerUtils.isHepFileName(getFile.getName())) {
                return getFile.getPath();
            }
        }
        return null;
    }

    public int installHepInStageDir(File stageDir) {
        if (stageDir == null) {
            Slog.i(TAG, "installHepInStageDir stageDir is null!");
            return -1;
        }
        String hepFileName = getHepFileName(stageDir);
        Slog.i(TAG, "installHepInStageDir :" + hepFileName);
        if (hepFileName == null) {
            Slog.i(TAG, "installHepInStageDir hepFileName is null!");
            return -1;
        }
        int installResult = this.mApexManager.installHep(hepFileName);
        Slog.i(TAG, "ApexManager install result :" + installResult);
        return installResult;
    }

    public List<HwHepPackageInfo> getInstalledHep(int flags) {
        List<HepInfo> hepInfos = this.mApexManager.getInstalledHep(flags);
        if (hepInfos == null) {
            Slog.i(TAG, "Apex manager query result is null!");
            return Collections.emptyList();
        }
        List<HwHepPackageInfo> results = new ArrayList<>(hepInfos.size());
        for (HepInfo hepInfo : hepInfos) {
            HwHepPackageInfo hwHepPackageInfo = new HwHepPackageInfo();
            hwHepPackageInfo.setPackageName(hepInfo.packageName);
            hwHepPackageInfo.setPackagePath(hepInfo.packagePath);
            hwHepPackageInfo.setVersionCode(hepInfo.versionCode);
            hwHepPackageInfo.setStatus(hepInfo.status);
            results.add(hwHepPackageInfo);
        }
        if (IS_DEBUG) {
            Slog.i(TAG, "getInstalledHep result:" + results);
        }
        return results;
    }

    public int uninstallHep(String packageName, int flags) {
        if (TextUtils.isEmpty(packageName)) {
            Slog.e(TAG, "uninstallHep illegal packageName!");
            return -1;
        }
        int result = this.mApexManager.uninstallHep(packageName, flags);
        Slog.i(TAG, "uninstallHep:,flags:" + flags + ",result:" + result);
        return result;
    }
}
