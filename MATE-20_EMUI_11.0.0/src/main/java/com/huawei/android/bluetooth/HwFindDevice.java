package com.huawei.android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class HwFindDevice implements Parcelable {
    public static final Parcelable.Creator<HwFindDevice> CREATOR = new Parcelable.Creator<HwFindDevice>() {
        /* class com.huawei.android.bluetooth.HwFindDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwFindDevice createFromParcel(Parcel in) {
            return new HwFindDevice(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwFindDevice[] newArray(int size) {
            return new HwFindDevice[size];
        }
    };
    private static final String TAG = "HwFindDevice";
    private String mConnectivity;
    private String mDeviceBtMac;
    private String mDeviceId;
    private String mHbkP;
    private String mIrk;

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mDeviceBtMac);
        out.writeString(this.mDeviceId);
        out.writeString(this.mConnectivity);
        out.writeString(this.mIrk);
        out.writeString(this.mHbkP);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private HwFindDevice(Parcel in) {
        this.mDeviceBtMac = in.readString();
        this.mDeviceId = in.readString();
        this.mConnectivity = in.readString();
        this.mIrk = in.readString();
        this.mHbkP = in.readString();
    }

    public HwFindDevice(String address) {
        this.mDeviceBtMac = address;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj instanceof HwFindDevice) {
            return Objects.equals(this.mDeviceBtMac, ((HwFindDevice) obj).getDeviceBtMac());
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
