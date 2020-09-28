package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigInfo implements Parcelable {
    public static final Parcelable.Creator<ConfigInfo> CREATOR = new Parcelable.Creator<ConfigInfo>() {
        /* class com.huawei.airsharing.api.ConfigInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConfigInfo createFromParcel(Parcel in) {
            return new ConfigInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public ConfigInfo[] newArray(int size) {
            return new ConfigInfo[size];
        }
    };
    private int mScanType = 48;

    public ConfigInfo() {
    }

    public ConfigInfo(int scanType) {
        this.mScanType = scanType;
    }

    protected ConfigInfo(Parcel in) {
        this.mScanType = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mScanType);
    }

    public int describeContents() {
        return 0;
    }

    public void setScanType(int scanType) {
        this.mScanType = scanType;
    }

    public int getScanType() {
        return this.mScanType;
    }
}
