package com.huawei.nb.notification;

import android.os.Parcel;
import android.os.Process;
import android.support.annotation.NonNull;

public abstract class ObserverInfo {
    private static final int HASHCODE_RANDOM = 31;
    private Integer pid;
    private String pkgName;
    private Integer proxyId = null;
    private ObserverType type;
    private Integer uid;

    public ObserverInfo(@NonNull ObserverType observerType, String str) {
        this.type = observerType;
        this.pid = Integer.valueOf(Process.myPid());
        this.uid = Integer.valueOf(Process.myUid());
        this.pkgName = str;
    }

    public ObserverInfo(Parcel parcel) {
        this.pid = Integer.valueOf(parcel.readInt());
        this.uid = Integer.valueOf(parcel.readInt());
        if (parcel.readInt() == 1) {
            this.pkgName = parcel.readString();
        } else {
            this.pkgName = null;
        }
        this.type = ObserverType.values()[parcel.readInt()];
        this.proxyId = Integer.valueOf(parcel.readInt());
    }

    public Integer getPid() {
        return this.pid;
    }

    public Integer getUid() {
        return this.uid;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setProxyId(Integer num) {
        this.proxyId = num;
    }

    public ObserverType getType() {
        return this.type;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ObserverInfo observerInfo = (ObserverInfo) obj;
        Integer num = this.pid;
        if (num == null ? observerInfo.pid != null : !num.equals(observerInfo.pid)) {
            return false;
        }
        Integer num2 = this.uid;
        if (num2 == null ? observerInfo.uid != null : !num2.equals(observerInfo.uid)) {
            return false;
        }
        Integer num3 = this.proxyId;
        if (num3 == null ? observerInfo.proxyId != null : !num3.equals(observerInfo.proxyId)) {
            return false;
        }
        String str = this.pkgName;
        if (str == null ? observerInfo.pkgName == null : str.equals(observerInfo.pkgName)) {
            return this.type == observerInfo.type;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        Integer num = this.pid;
        int i = 0;
        int hashCode = (num != null ? num.hashCode() : 0) * HASHCODE_RANDOM;
        Integer num2 = this.uid;
        int hashCode2 = (hashCode + (num2 != null ? num2.hashCode() : 0)) * HASHCODE_RANDOM;
        ObserverType observerType = this.type;
        int hashCode3 = (hashCode2 + (observerType != null ? observerType.hashCode() : 0)) * HASHCODE_RANDOM;
        Integer num3 = this.proxyId;
        int hashCode4 = (hashCode3 + (num3 != null ? num3.hashCode() : 0)) * HASHCODE_RANDOM;
        String str = this.pkgName;
        if (str != null) {
            i = str.hashCode();
        }
        return hashCode4 + i;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.pid.intValue());
        parcel.writeInt(this.uid.intValue());
        if (this.pkgName != null) {
            parcel.writeInt(1);
            parcel.writeString(this.pkgName);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.type.ordinal());
        parcel.writeInt(this.proxyId.intValue());
    }

    @Override // java.lang.Object
    public String toString() {
        return "ObserverInfo{pid=" + this.pid + ", uid=" + this.uid + ", type=" + this.type + ", proxyId=" + this.proxyId + '}';
    }
}
