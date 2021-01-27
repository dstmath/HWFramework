package com.huawei.android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class HwBindDevice implements Parcelable {
    public static final Parcelable.Creator<HwBindDevice> CREATOR = new Parcelable.Creator<HwBindDevice>() {
        /* class com.huawei.android.bluetooth.HwBindDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwBindDevice createFromParcel(Parcel in) {
            return new HwBindDevice(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwBindDevice[] newArray(int size) {
            return new HwBindDevice[size];
        }
    };
    private static final String TAG = "HwBindDevice";
    private String mBellType;
    private String mConnectivity;
    private String mDeviceBtMac;
    private String mDeviceId;
    private String mDeviceName;
    private String mDeviceType;
    private String mFindCapability;
    private String mFindSupport;
    private String mHbkP;
    private String mIrk;
    private String mModelId;
    private String mSubDeviceType;
    private String mSubModelId;
    private String mTimeStamp;
    private String mWearDetect;

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mDeviceBtMac);
        out.writeString(this.mDeviceId);
        out.writeString(this.mDeviceType);
        out.writeString(this.mModelId);
        out.writeString(this.mSubModelId);
        out.writeString(this.mDeviceName);
        out.writeString(this.mSubDeviceType);
        out.writeString(this.mTimeStamp);
        out.writeString(this.mWearDetect);
        out.writeString(this.mFindSupport);
        out.writeString(this.mFindCapability);
        out.writeString(this.mBellType);
        out.writeString(this.mConnectivity);
        out.writeString(this.mIrk);
        out.writeString(this.mHbkP);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private HwBindDevice(Parcel in) {
        this.mDeviceBtMac = in.readString();
        this.mDeviceId = in.readString();
        this.mDeviceType = in.readString();
        this.mModelId = in.readString();
        this.mSubModelId = in.readString();
        this.mDeviceName = in.readString();
        this.mSubDeviceType = in.readString();
        this.mTimeStamp = in.readString();
        this.mWearDetect = in.readString();
        this.mFindSupport = in.readString();
        this.mFindCapability = in.readString();
        this.mBellType = in.readString();
        this.mConnectivity = in.readString();
        this.mIrk = in.readString();
        this.mHbkP = in.readString();
    }

    public HwBindDevice(String address) {
        this.mDeviceBtMac = address;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj instanceof HwBindDevice) {
            return Objects.equals(this.mDeviceBtMac, ((HwBindDevice) obj).getDeviceBtMac());
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        String str = this.mDeviceBtMac;
        if (str == null) {
            return 0;
        }
        return str.hashCode();
    }

    public String getDeviceBtMac() {
        return this.mDeviceBtMac;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public String getDeviceType() {
        return this.mDeviceType;
    }

    public void setDeviceType(String deviceType) {
        this.mDeviceType = deviceType;
    }

    public String getModelId() {
        return this.mModelId;
    }

    public void setModelId(String modelId) {
        this.mModelId = modelId;
    }

    public String getSubModelId() {
        return this.mSubModelId;
    }

    public void setSubModelId(String subModelId) {
        this.mSubModelId = subModelId;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    public String getSubDeviceType() {
        return this.mSubDeviceType;
    }

    public void setSubDeviceType(String subDeviceType) {
        this.mSubDeviceType = subDeviceType;
    }

    public String getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public String getWearDetect() {
        return this.mWearDetect;
    }

    public void setWearDetect(String wearDetect) {
        this.mWearDetect = wearDetect;
    }

    public String getFindSupport() {
        return this.mFindSupport;
    }

    public void setFindSupport(String findSupport) {
        this.mFindSupport = findSupport;
    }

    public String getFindCapability() {
        return this.mFindCapability;
    }

    public void setFindCapability(String findCapability) {
        this.mFindCapability = findCapability;
    }

    public String getBellType() {
        return this.mBellType;
    }

    public void setBellType(String bellType) {
        this.mBellType = bellType;
    }

    public String getConnectivity() {
        return this.mConnectivity;
    }

    public void setConnectivity(String connectivity) {
        this.mConnectivity = connectivity;
    }

    public String getIrk() {
        return this.mIrk;
    }

    public void setIrk(String irk) {
        this.mIrk = irk;
    }

    public String getHbkP() {
        return this.mHbkP;
    }

    public void setHbkP(String hbkP) {
        this.mHbkP = hbkP;
    }
}
