package com.android.server.am;

import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.Set;

public final class HwActivityStackSupervisor extends ActivityStackSupervisor {
    private static final int CRASH_INTERVAL_THRESHOLD = 60000;
    private static final int CRASH_TIMES_THRESHOLD = 3;
    private static final boolean IS_TABLET;
    private int mCrashTimes;
    private long mFirstLaunchTime;
    private String mLastHomePkg;

    public HwActivityStackSupervisor(ActivityManagerService service) {
        super(service);
    }

    private boolean isUninstallableApk(String pkgName) {
        if (pkgName == null || "android".equals(pkgName)) {
            return false;
        }
        try {
            PackageInfo pInfo = this.mService.mContext.getPackageManager().getPackageInfo(pkgName, 0);
            if (pInfo == null || (pInfo.applicationInfo.flags & 1) == 0) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public void recognitionMaliciousApp(IApplicationThread caller, Intent intent) {
        if (caller == null) {
            Intent homeIntent = this.mService.getHomeIntent();
            String action = intent.getAction();
            Set<String> category = intent.getCategories();
            if (action != null && action.equals(homeIntent.getAction()) && category != null && category.containsAll(homeIntent.getCategories())) {
                ComponentName cmp = intent.getComponent();
                String strPkg = null;
                if (cmp != null) {
                    strPkg = cmp.getPackageName();
                }
                if (strPkg != null && isUninstallableApk(strPkg)) {
                    if (strPkg.equals(this.mLastHomePkg)) {
                        this.mCrashTimes++;
                        long now = SystemClock.uptimeMillis();
                        if (this.mCrashTimes >= CRASH_TIMES_THRESHOLD) {
                            if (now - this.mFirstLaunchTime < AppHibernateCst.DELAY_ONE_MINS) {
                                try {
                                    ActivityThread.getPackageManager().clearPackagePreferredActivities(strPkg);
                                } catch (RemoteException e) {
                                }
                                this.mService.showUninstallLauncherDialog(strPkg);
                                this.mLastHomePkg = null;
                                this.mCrashTimes = 0;
                            } else {
                                this.mCrashTimes = 1;
                                this.mFirstLaunchTime = now;
                            }
                        }
                    } else {
                        this.mLastHomePkg = strPkg;
                        this.mCrashTimes = 0;
                        this.mFirstLaunchTime = SystemClock.uptimeMillis();
                    }
                }
            }
        }
    }

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags) {
        if ((intent.getHwFlags() & 1) != 0) {
            flags |= 4194304;
        }
        return super.resolveIntent(intent, resolvedType, userId, flags);
    }

    static {
        IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics"));
    }

    protected boolean isInMultiWinBlackList(String pkgName) {
        if (!IS_TABLET && "com.huawei.systemmanager".equals(pkgName)) {
            return true;
        }
        return false;
    }
}
