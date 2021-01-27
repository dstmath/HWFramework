package com.android.server.rms.iaware.hiber.bean;

import com.android.server.rms.iaware.hiber.util.AppHiberUtil;

public class AbsAppInfo {
    public String pkgName;
    public int uid;

    public AbsAppInfo(int uid2, String pkgName2) {
        this.uid = uid2;
        this.pkgName = pkgName2;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass() || !(obj instanceof AbsAppInfo)) {
            return false;
        }
        AbsAppInfo absAppInfo = (AbsAppInfo) obj;
        if (AppHiberUtil.isStrEmpty(absAppInfo.pkgName) || AppHiberUtil.isStrEmpty(this.pkgName)) {
            return false;
        }
        if (absAppInfo.uid != this.uid || !absAppInfo.pkgName.equals(this.pkgName)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return AppHiberUtil.isStrEmpty(this.pkgName) ? this.uid : this.pkgName.hashCode();
    }
}
