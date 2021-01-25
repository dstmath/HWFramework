package com.huawei.permission.cloud;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

public class PackageInstallerPermissionInfo implements Parcelable {
    public static final Parcelable.Creator<PackageInstallerPermissionInfo> CREATOR = new Parcelable.Creator<PackageInstallerPermissionInfo>() {
        /* class com.huawei.permission.cloud.PackageInstallerPermissionInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PackageInstallerPermissionInfo createFromParcel(Parcel source) {
            return new PackageInstallerPermissionInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public PackageInstallerPermissionInfo[] newArray(int size) {
            return new PackageInstallerPermissionInfo[size];
        }
    };
    private static final int DEFAULT_VALUE = 0;
    public static final int PERM_RESTRICTED = -1;
    public static final int PERM_UNRESTRICTED = 0;
    private static final String TAG = "PackageInstallerPermissionInfo";
    private int mIconRes;
    private int mIsRestricted = 0;
    private int mLabelRes;
    private String mName;
    private String mPermissionGroup;
    private String mResPackage;
    private int mStatus;

    public PackageInstallerPermissionInfo() {
        Log.d(TAG, "create PackageInstallerPermissionInfo");
    }

    public PackageInstallerPermissionInfo(Parcel in) {
        this.mName = in.readString();
        this.mStatus = in.readInt();
        this.mLabelRes = in.readInt();
        this.mIconRes = in.readInt();
        this.mResPackage = in.readString();
        this.mIsRestricted = in.readInt();
        this.mPermissionGroup = in.readString();
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getLabelRes() {
        return this.mLabelRes;
    }

    public void setLabelRes(int labelRes) {
        this.mLabelRes = labelRes;
    }

    public int getIconRes() {
        return this.mIconRes;
    }

    public void setIconRes(int iconRes) {
        this.mIconRes = iconRes;
    }

    public String getResPackage() {
        return this.mResPackage;
    }

    public void setResPackage(String resPackage) {
        this.mResPackage = resPackage;
    }

    public int getIsRestricted() {
        return this.mIsRestricted;
    }

    public void setIsRestricted(int isRestricted) {
        this.mIsRestricted = isRestricted;
    }

    public String getPermissionGroup() {
        return this.mPermissionGroup;
    }

    public void setPermissionGroup(String permissionGroup) {
        this.mPermissionGroup = permissionGroup;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(this.mName);
        parcel.writeInt(this.mStatus);
        parcel.writeInt(this.mLabelRes);
        parcel.writeInt(this.mIconRes);
        parcel.writeString(this.mResPackage);
        parcel.writeInt(this.mIsRestricted);
        parcel.writeString(this.mPermissionGroup);
    }

    @Override // java.lang.Object
    @NonNull
    public String toString() {
        return "PackageInstallerPermissionInfo: name=" + this.mName + " status=" + this.mStatus + " isRestrict=" + this.mIsRestricted + " group=" + this.mPermissionGroup;
    }
}
