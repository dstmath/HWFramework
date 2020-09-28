package com.huawei.android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class BleAgentRegisterInformation implements Parcelable {
    public static final Parcelable.Creator<BleAgentRegisterInformation> CREATOR = new Parcelable.Creator<BleAgentRegisterInformation>() {
        /* class com.huawei.android.bluetooth.BleAgentRegisterInformation.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BleAgentRegisterInformation[] newArray(int size) {
            return new BleAgentRegisterInformation[size];
        }

        @Override // android.os.Parcelable.Creator
        public BleAgentRegisterInformation createFromParcel(Parcel in) {
            return new BleAgentRegisterInformation(in);
        }
    };
    private static final String TAG = "BleAgentRegisterInformation";
    String mAppName;
    String mBrand;
    String mMac;
    long mScanTime;

    private BleAgentRegisterInformation(Parcel in) {
        this.mMac = in.readString();
        this.mAppName = in.readString();
        this.mBrand = in.readString();
        this.mScanTime = in.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mMac);
        dest.writeString(this.mAppName);
        dest.writeString(this.mBrand);
        dest.writeLong(this.mScanTime);
    }

    public int describeContents() {
        return 0;
    }

    public BleAgentRegisterInformation(String mac, String app, String brand, long scanTime) {
        this.mMac = mac;
        this.mAppName = app;
        this.mBrand = brand;
        this.mScanTime = scanTime;
    }

    public String getDeviceMac() {
        Log.d(TAG, "getDeviceMac");
        return this.mMac;
    }

    public String getAppName() {
        Log.d(TAG, "getAppName");
        return this.mAppName;
    }

    public String getBrand() {
        Log.d(TAG, "getBrand");
        return this.mBrand;
    }

    public long getScanTime() {
        Log.d(TAG, "getScanTime");
        return this.mScanTime;
    }
}
