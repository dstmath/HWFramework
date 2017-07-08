package com.android.server.rms.iaware.hiber.bean;

import com.android.server.rms.iaware.hiber.util.AppHiberUtil;

public class AbsAppInfo {
    public String mPkgName;
    public int mUid;

    public AbsAppInfo(int uid, String pkgName) {
        this.mUid = uid;
        this.mPkgName = pkgName;
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
        AbsAppInfo absAppInfo = (AbsAppInfo) obj;
        if (AppHiberUtil.isStrEmpty(absAppInfo.mPkgName) || AppHiberUtil.isStrEmpty(this.mPkgName)) {
            return false;
        }
        if (absAppInfo.mUid == this.mUid) {
            z = absAppInfo.mPkgName.equals(this.mPkgName);
        }
        return z;
    }

    public int hashCode() {
        return AppHiberUtil.isStrEmpty(this.mPkgName) ? this.mUid : this.mPkgName.hashCode();
    }
}
