package com.huawei.android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

public final class HwHepPackageInfo implements Parcelable {
    public static final Parcelable.Creator<HwHepPackageInfo> CREATOR = new Parcelable.Creator<HwHepPackageInfo>() {
        /* class com.huawei.android.content.pm.HwHepPackageInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwHepPackageInfo createFromParcel(Parcel source) {
            return new HwHepPackageInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public HwHepPackageInfo[] newArray(int size) {
            return new HwHepPackageInfo[size];
        }
    };
    private static final String TAG = "HwHepPackageInfo";
    private String packageName;
    private String packagePath;
    private long status;
    private long versionCode;

    public HwHepPackageInfo() {
    }

    private HwHepPackageInfo(Parcel source) {
        this.packageName = source.readString();
        this.packagePath = source.readString();
        this.versionCode = source.readLong();
        this.status = source.readLong();
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setVersionCode(long versionCode2) {
        this.versionCode = versionCode2;
    }

    public long getVersionCode() {
        return this.versionCode;
    }

    public void setPackagePath(String packagePath2) {
        this.packagePath = packagePath2;
    }

    public String getPackagePath() {
        return this.packagePath;
    }

    public long getStatus() {
        return this.status;
    }

    public void setStatus(long status2) {
        this.status = status2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.packageName);
        dest.writeString(this.packagePath);
        dest.writeLong(this.versionCode);
        dest.writeLong(this.status);
    }

    public String toString() {
        return "HwHepPackageInfo: packageName:" + this.packageName + " packagePath:" + this.packagePath + " versionCode:" + this.versionCode;
    }
}
