package com.android.server.pfw.autostartup.datamgr;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import com.android.server.HwConnectivityService;
import com.android.server.pfw.autostartup.comm.AutoStartupUtil;
import com.android.server.pfw.autostartup.comm.WallpaperHelper;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopPackagesThread extends Thread {
    private static final String TAG = "StopPackagesThread";
    private Context mContext;
    private AutoStartupDataMgr mDataMgr;

    public StopPackagesThread(Context context, AutoStartupDataMgr dataMgr) {
        super(TAG);
        this.mContext = null;
        this.mDataMgr = null;
        this.mContext = context;
        this.mDataMgr = dataMgr;
    }

    public void run() {
        HwPFWLogger.d(TAG, "stop packages thread begin");
        setAppToStopState(getExemptPackageList());
        this.mDataMgr.setStopAfterBootOpFinish();
        HwPFWLogger.d(TAG, "stop packages thread end");
    }

    private Set<String> getExemptPackageList() {
        Set<String> exemptPkgs = new HashSet();
        appendDefaultSms(exemptPkgs);
        appendDefaultIms(exemptPkgs);
        appendCurWallpaper(exemptPkgs);
        appendCurLauncher(exemptPkgs);
        appendTopRunningPackage(exemptPkgs);
        return exemptPkgs;
    }

    private void setAppToStopState(Set<String> exemptPkgs) {
        PackageManager mPM = this.mContext.getPackageManager();
        ActivityManager mAM = (ActivityManager) this.mContext.getSystemService("activity");
        for (PackageInfo pkgInfo : mPM.getInstalledPackages(0)) {
            ApplicationInfo applicationInfo = pkgInfo.applicationInfo;
            if (applicationInfo.uid >= LifeCycleStateMachine.TIME_OUT_TIME && !AutoStartupUtil.isSystemUnRemovablePkg(applicationInfo)) {
                if (this.mDataMgr.isAllowBootStartup(applicationInfo.packageName)) {
                    HwPFWLogger.d(TAG, "skip stop isAllowBootStartup pkg: " + applicationInfo.packageName);
                } else if ((exemptPkgs == null || !exemptPkgs.contains(applicationInfo.packageName)) && (applicationInfo.flags & 2097152) == 0) {
                    if (HwConnectivityService.MM_PKG_NAME.equals(applicationInfo.packageName) && mPM.isUpgrade()) {
                        HwPFWLogger.i(TAG, "we donnot forceStop com.tencent.mm when ota");
                    } else {
                        HwPFWLogger.i(TAG, "forceStopPkg " + applicationInfo.packageName);
                        mAM.forceStopPackage(applicationInfo.packageName);
                    }
                }
            }
        }
    }

    private void appendDefaultSms(Set<String> exemptPkgs) {
        Object defaultPkgName = null;
        try {
            defaultPkgName = Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!TextUtils.isEmpty(defaultPkgName)) {
            HwPFWLogger.d(TAG, "appendDefaultSms package = " + defaultPkgName);
            exemptPkgs.add(defaultPkgName);
        }
    }

    private void appendDefaultIms(Set<String> exemptPkgs) {
        String inputMethod = Secure.getString(this.mContext.getContentResolver(), "default_input_method");
        if (inputMethod != null) {
            String[] defaultIms = inputMethod.split("/");
            if (defaultIms[0] != null) {
                HwPFWLogger.d(TAG, "appendDefaultIms IMs = " + defaultIms[0]);
                exemptPkgs.add(defaultIms[0]);
            }
        }
    }

    private void appendCurWallpaper(Set<String> exemptPkgs) {
        if (exemptPkgs != null) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.mContext);
            if (wallpaperManager != null) {
                String curWallpaper;
                WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
                if (wallpaperInfo != null) {
                    curWallpaper = wallpaperInfo.getPackageName();
                } else {
                    curWallpaper = WallpaperHelper.getWallpaperPkgName(((UserManager) this.mContext.getSystemService("user")).getUserHandle());
                }
                if (curWallpaper != null) {
                    HwPFWLogger.d(TAG, "appendCurWallpaper add wallpaper: " + curWallpaper);
                    exemptPkgs.add(curWallpaper);
                }
            }
        }
    }

    private void appendCurLauncher(Set<String> execptPkgs) {
        String launcher = getDefaultLauncher();
        if (TextUtils.isEmpty(launcher)) {
            HwPFWLogger.d(TAG, "appendCurLauncher get default launcher is null, set hwlauncher");
            launcher = "com.huawei.android.launcher";
        }
        HwPFWLogger.d(TAG, "appendCurLauncher: " + launcher);
        execptPkgs.add(launcher);
    }

    private void appendTopRunningPackage(Set<String> exemptPkgs) {
        String pkgName = getTopRunningPkg();
        if (pkgName != null) {
            HwPFWLogger.d(TAG, "appendTopRunningPackage append package: " + pkgName);
            exemptPkgs.add(pkgName);
            return;
        }
        HwPFWLogger.d(TAG, "appendTopRunningPackage get null of top running package");
    }

    private String getDefaultLauncher() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo res = this.mContext.getPackageManager().resolveActivity(intent, 0);
        if (res == null || res.activityInfo == null) {
            return null;
        }
        String pkgName = res.activityInfo.packageName;
        if ("android".equals(pkgName)) {
            return null;
        }
        return pkgName;
    }

    private String getTopRunningPkg() {
        List<RunningTaskInfo> taskInfos = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (taskInfos == null || 1 != taskInfos.size()) {
            return null;
        }
        return ((RunningTaskInfo) taskInfos.get(0)).baseActivity.getPackageName();
    }
}
