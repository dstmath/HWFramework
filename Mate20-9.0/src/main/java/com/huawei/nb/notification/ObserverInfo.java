package com.huawei.nb.notification;

import android.os.Parcel;
import android.os.Process;
import android.support.annotation.NonNull;

public abstract class ObserverInfo {
    private Integer pid = null;
    private String pkgName = null;
    private Integer proxyId = null;
    private ObserverType type = null;
    private Integer uid = null;

    public Integer getPid() {
        return this.pid;
    }

    public Integer getUid() {
        return this.uid;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setProxyId(Integer proxyId2) {
        this.proxyId = proxyId2;
    }

    public ObserverType getType() {
        return this.type;
    }

    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            if (obj1 == obj2) {
                return true;
            }
            return false;
        } else if (!(obj1 instanceof ObserverInfo) || !(obj2 instanceof ObserverInfo)) {
            return false;
        } else {
            return ((ObserverInfo) obj1).equals(obj2);
        }
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ObserverInfo that = (ObserverInfo) o;
        if (this.pid != null) {
            if (!this.pid.equals(that.pid)) {
                return false;
            }
        } else if (that.pid != null) {
            return false;
        }
        if (this.uid != null) {
            if (!this.uid.equals(that.uid)) {
                return false;
            }
        } else if (that.uid != null) {
            return false;
        }
        if (this.proxyId != null) {
            if (!this.proxyId.equals(that.proxyId)) {
                return false;
            }
        } else if (that.proxyId != null) {
            return false;
        }
        if (this.type != that.type) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int result;
        int i;
        int i2;
        int i3 = 0;
        if (this.pid != null) {
            result = this.pid.hashCode();
        } else {
            result = 0;
        }
        int i4 = result * 31;
        if (this.uid != null) {
            i = this.uid.hashCode();
        } else {
            i = 0;
        }
        int i5 = (i4 + i) * 31;
        if (this.type != null) {
            i2 = this.type.hashCode();
        } else {
            i2 = 0;
        }
        int i6 = (i5 + i2) * 31;
        if (this.proxyId != null) {
            i3 = this.proxyId.hashCode();
        }
        return i6 + i3;
    }

    public ObserverInfo(@NonNull ObserverType type2, String pkgName2) {
        this.type = type2;
        this.pid = Integer.valueOf(Process.myPid());
        this.uid = Integer.valueOf(Process.myUid());
        this.pkgName = pkgName2;
    }

    public ObserverInfo(Parcel in) {
        this.pid = Integer.valueOf(in.readInt());
        this.uid = Integer.valueOf(in.readInt());
        if (in.readInt() == 1) {
            this.pkgName = in.readString();
        } else {
            this.pkgName = null;
        }
        this.type = ObserverType.values()[in.readInt()];
        this.proxyId = Integer.valueOf(in.readInt());
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.pid.intValue());
        dest.writeInt(this.uid.intValue());
        if (this.pkgName != null) {
            dest.writeInt(1);
            dest.writeString(this.pkgName);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.type.ordinal());
        dest.writeInt(this.proxyId.intValue());
    }

    public String toString() {
        return "ObserverInfo{pid=" + this.pid + ", uid=" + this.uid + ", type=" + this.type + ", proxyId=" + this.proxyId + '}';
    }
}
