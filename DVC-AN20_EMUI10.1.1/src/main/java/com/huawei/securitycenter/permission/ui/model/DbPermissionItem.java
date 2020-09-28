package com.huawei.securitycenter.permission.ui.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public class DbPermissionItem implements Parcelable {
    public static final Parcelable.Creator<DbPermissionItem> CREATOR = new Parcelable.Creator<DbPermissionItem>() {
        /* class com.huawei.securitycenter.permission.ui.model.DbPermissionItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DbPermissionItem createFromParcel(Parcel source) {
            return new DbPermissionItem(source.readString(), source.readLong(), source.readLong(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public DbPermissionItem[] newArray(int size) {
            return new DbPermissionItem[size];
        }
    };
    private String mPackageName;
    private long mPermissionCfg = -1;
    private long mPermissionCode = 0;
    private int mUid = -1;

    public DbPermissionItem(@NonNull String pkgName, long permissionCfg, long permissionCode) {
        this.mPackageName = pkgName;
        this.mPermissionCfg = permissionCfg;
        this.mPermissionCode = permissionCode;
    }

    public DbPermissionItem(@NonNull String pkgName, long permissionCfg, long permissionCode, int uid) {
        this.mPackageName = pkgName;
        this.mPermissionCfg = permissionCfg;
        this.mPermissionCode = permissionCode;
        this.mUid = uid;
    }

    public DbPermissionItem(@NonNull String pkgName) {
        this.mPackageName = pkgName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.mPackageName);
        dest.writeLong(this.mPermissionCfg);
        dest.writeLong(this.mPermissionCode);
        dest.writeInt(this.mUid);
    }

    public void setPackageName(@NonNull String packageName) {
        this.mPackageName = packageName;
    }

    public void setPermissionCfg(long permissionCfg) {
        this.mPermissionCfg = permissionCfg;
    }

    public void setPermissionCode(long permissionCode) {
        this.mPermissionCode = permissionCode;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public long getPermissionCfg() {
        return this.mPermissionCfg;
    }

    public long getPermissionCode() {
        return this.mPermissionCode;
    }

    public int getUid() {
        return this.mUid;
    }

    public String toString() {
        return "DbPermissionItem [pkgName=" + this.mPackageName + ", permCfg=" + this.mPermissionCfg + ", permCode=" + this.mPermissionCode + ", uid=" + this.mUid + "]";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DbPermissionItem that = (DbPermissionItem) obj;
        if (TextUtils.isEmpty(this.mPackageName)) {
            return false;
        }
        return this.mPackageName.equals(that.mPackageName);
    }

    public int hashCode() {
        if (TextUtils.isEmpty(this.mPackageName)) {
            return 0;
        }
        return this.mPackageName.hashCode();
    }
}
