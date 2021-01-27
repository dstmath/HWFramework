package com.android.server.rms.iaware.hiber.bean;

import android.os.SystemClock;
import com.android.server.rms.iaware.hiber.util.AppHiberUtil;

public class HiberAppInfo extends AbsAppInfo {
    public int pid;
    public String processName;
    public long reclaimTime = SystemClock.uptimeMillis();

    public HiberAppInfo(int uid, String pkgName, int pid2, String processName2) {
        super(uid, pkgName);
        this.pid = pid2;
        this.processName = processName2;
    }

    @Override // com.android.server.rms.iaware.hiber.bean.AbsAppInfo
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass() || !(obj instanceof HiberAppInfo)) {
            return false;
        }
        HiberAppInfo hiberAppInfo = (HiberAppInfo) obj;
        if (hiberAppInfo.uid == this.uid && !AppHiberUtil.isStrEmpty(hiberAppInfo.pkgName) && !AppHiberUtil.isStrEmpty(this.pkgName) && !AppHiberUtil.isStrEmpty(hiberAppInfo.processName) && !AppHiberUtil.isStrEmpty(this.processName) && hiberAppInfo.pkgName.equals(this.pkgName) && hiberAppInfo.processName.equals(this.processName)) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.rms.iaware.hiber.bean.AbsAppInfo
    public int hashCode() {
        return AppHiberUtil.isStrEmpty(this.processName) ? this.pid : this.processName.hashCode();
    }
}
