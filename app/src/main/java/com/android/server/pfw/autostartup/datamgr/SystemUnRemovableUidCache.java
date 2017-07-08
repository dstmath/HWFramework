package com.android.server.pfw.autostartup.datamgr;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.android.server.pfw.autostartup.comm.AutoStartupUtil;
import com.android.server.pfw.log.HwPFWLogger;
import java.util.HashSet;
import java.util.Set;

public class SystemUnRemovableUidCache {
    private static final String TAG = "SystemUnRemovableUidCache";
    private Set<Integer> mSysUnremovableUids;

    public SystemUnRemovableUidCache() {
        this.mSysUnremovableUids = new HashSet();
    }

    public void loadSystemUid(Context ctx) {
        Set<Integer> tempUidSet = new HashSet();
        for (ApplicationInfo app : ctx.getPackageManager().getInstalledApplications(0)) {
            if (AutoStartupUtil.isSystemUnRemovablePkg(app)) {
                tempUidSet.add(Integer.valueOf(app.uid));
            }
        }
        synchronized (this.mSysUnremovableUids) {
            this.mSysUnremovableUids.clear();
            this.mSysUnremovableUids.addAll(tempUidSet);
            HwPFWLogger.d(TAG, "loadSystemUid: " + this.mSysUnremovableUids);
        }
    }

    public boolean checkUidExist(int uid) {
        boolean contains;
        synchronized (this.mSysUnremovableUids) {
            contains = this.mSysUnremovableUids.contains(Integer.valueOf(uid));
        }
        return contains;
    }
}
