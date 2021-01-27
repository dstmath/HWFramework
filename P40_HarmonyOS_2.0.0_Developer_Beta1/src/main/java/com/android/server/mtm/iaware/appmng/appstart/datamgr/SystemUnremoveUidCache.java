package com.android.server.mtm.iaware.appmng.appstart.datamgr;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import java.util.HashSet;
import java.util.Set;

public class SystemUnremoveUidCache {
    private static final Object INSTENCE_LOCK = new Object();
    private static final String TAG = "SystemUnremoveUidCache";
    private static volatile SystemUnremoveUidCache sInstance = null;
    private Set<Integer> mSysUnremoveUids = new HashSet();

    public static SystemUnremoveUidCache getInstance(Context context) {
        if (sInstance == null) {
            synchronized (INSTENCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new SystemUnremoveUidCache(context);
                }
            }
        }
        return sInstance;
    }

    private SystemUnremoveUidCache(Context context) {
        loadSystemUid(context);
    }

    public void loadSystemUid(Context ctx) {
        if (ctx != null) {
            Set<Integer> tempUidSet = new HashSet<>();
            PackageManager pm = ctx.getPackageManager();
            if (pm != null) {
                for (ApplicationInfo app : pm.getInstalledApplications(0)) {
                    if (app != null && AppStartupUtil.isSystemUnRemovablePkg(app)) {
                        tempUidSet.add(Integer.valueOf(app.uid));
                    }
                }
            }
            this.mSysUnremoveUids = tempUidSet;
        }
    }

    public boolean checkUidExist(int uid) {
        return this.mSysUnremoveUids.contains(Integer.valueOf(uid));
    }

    public String toString() {
        return this.mSysUnremoveUids.toString();
    }
}
