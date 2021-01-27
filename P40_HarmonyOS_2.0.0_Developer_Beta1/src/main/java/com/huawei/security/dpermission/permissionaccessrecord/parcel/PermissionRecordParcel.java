package com.huawei.security.dpermission.permissionaccessrecord.parcel;

import android.os.Parcel;
import android.os.Parcelable;

public class PermissionRecordParcel implements Parcelable {
    public static final Parcelable.Creator<PermissionRecordParcel> CREATOR = new Parcelable.Creator<PermissionRecordParcel>() {
        /* class com.huawei.security.dpermission.permissionaccessrecord.parcel.PermissionRecordParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PermissionRecordParcel createFromParcel(Parcel in) {
            return new PermissionRecordParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public PermissionRecordParcel[] newArray(int size) {
            return new PermissionRecordParcel[size];
        }
    };
    private int accessCountBg;
    private int accessCountFg;
    private long lastAccessTime;
    private long lastRejectTime;
    private String permissionName;
    private int rejectCountBg;
    private int rejectCountFg;

    public PermissionRecordParcel() {
    }

    public PermissionRecordParcel(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.permissionName = in.readString();
        this.accessCountFg = in.readInt();
        this.rejectCountFg = in.readInt();
        this.accessCountBg = in.readInt();
        this.rejectCountBg = in.readInt();
        this.lastAccessTime = in.readLong();
        this.lastRejectTime = in.readLong();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.permissionName);
        dest.writeInt(this.accessCountFg);
        dest.writeInt(this.rejectCountFg);
        dest.writeInt(this.accessCountBg);
        dest.writeInt(this.rejectCountBg);
        dest.writeLong(this.lastAccessTime);
        dest.writeLong(this.lastRejectTime);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getPermissionName() {
        return this.permissionName;
    }

    public void setPermissionName(String permissionName2) {
        this.permissionName = permissionName2;
    }

    public int getAccessCountFg() {
        return this.accessCountFg;
    }

    public void setAccessCountFg(int accessCountFg2) {
        this.accessCountFg = accessCountFg2;
    }

    public int getRejectCountFg() {
        return this.rejectCountFg;
    }

    public void setRejectCountFg(int rejectCountFg2) {
        this.rejectCountFg = rejectCountFg2;
    }

    public int getAccessCountBg() {
        return this.accessCountBg;
    }

    public void setAccessCountBg(int accessCountBg2) {
        this.accessCountBg = accessCountBg2;
    }

    public int getRejectCountBg() {
        return this.rejectCountBg;
    }

    public void setRejectCountBg(int rejectCountBg2) {
        this.rejectCountBg = rejectCountBg2;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime2) {
        this.lastAccessTime = lastAccessTime2;
    }

    public long getLastRejectTime() {
        return this.lastRejectTime;
    }

    public void setLastRejectTime(long lastRejectTime2) {
        this.lastRejectTime = lastRejectTime2;
    }

    @Override // java.lang.Object
    public String toString() {
        return "PermissionRecordParcel{permissionName='" + this.permissionName + "', accessCountFg=" + this.accessCountFg + ", rejectCountFg=" + this.rejectCountFg + ", accessCountBg=" + this.accessCountBg + ", rejectCountBg=" + this.rejectCountBg + ", lastAccessTime=" + this.lastAccessTime + ", lastRejectTime=" + this.lastRejectTime + '}';
    }
}
