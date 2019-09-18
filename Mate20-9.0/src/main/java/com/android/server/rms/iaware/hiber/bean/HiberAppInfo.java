package com.android.server.rms.iaware.hiber.bean;

import android.os.SystemClock;
import com.android.server.rms.iaware.hiber.util.AppHiberUtil;

public class HiberAppInfo extends AbsAppInfo {
    public int mPid;
    public String mProcessName;
    public long mReclaimTime = SystemClock.uptimeMillis();

    public HiberAppInfo(int uid, String pkgName, int pid, String processName) {
        super(uid, pkgName);
        this.mPid = pid;
        this.mProcessName = processName;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        HiberAppInfo hiberAppInfo = (HiberAppInfo) obj;
        if (hiberAppInfo.mUid != this.mUid || AppHiberUtil.isStrEmpty(hiberAppInfo.mPkgName) || AppHiberUtil.isStrEmpty(this.mPkgName) || AppHiberUtil.isStrEmpty(hiberAppInfo.mProcessName) || AppHiberUtil.isStrEmpty(this.mProcessName)) {
            return false;
        }
        if (hiberAppInfo.mPkgName.equals(this.mPkgName) && hiberAppInfo.mProcessName.equals(this.mProcessName)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return AppHiberUtil.isStrEmpty(this.mProcessName) ? this.mPid : this.mProcessName.hashCode();
    }
}
