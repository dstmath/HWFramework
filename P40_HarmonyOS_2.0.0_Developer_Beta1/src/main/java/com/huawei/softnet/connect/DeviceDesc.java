package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceDesc implements Parcelable {
    public static final Parcelable.Creator<DeviceDesc> CREATOR = new Parcelable.Creator<DeviceDesc>() {
        /* class com.huawei.softnet.connect.DeviceDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceDesc createFromParcel(Parcel in) {
            return new DeviceDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public DeviceDesc[] newArray(int size) {
            return new DeviceDesc[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private String mBtMac;
    private int[] mCapabilityBitmap;
    private int mCapabilityBitmapNum;
    private String mDeviceId;
    private String mDeviceName;
    private int mDeviceType;
    private String mIpv4;
    private String mIpv6;
    private int mPort;
    private String mReservedInfo;
    private String mWifiMac;

    protected DeviceDesc(Parcel in) {
        this.mDeviceName = in.readString();
        this.mDeviceId = in.readString();
        this.mIpv4 = in.readString();
        this.mIpv6 = in.readString();
        this.mPort = in.readInt();
        this.mWifiMac = in.readString();
        this.mBtMac = in.readString();
        this.mDeviceType = in.readInt();
        this.mCapabilityBitmapNum = in.readInt();
        this.mCapabilityBitmap = in.createIntArray();
        this.mReservedInfo = in.readString();
    }

    private DeviceDesc() {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceName);
        dest.writeString(this.mDeviceId);
        dest.writeString(this.mIpv4);
        dest.writeString(this.mIpv6);
        dest.writeInt(this.mPort);
        dest.writeString(this.mWifiMac);
        dest.writeString(this.mBtMac);
        dest.writeInt(this.mDeviceType);
        dest.writeInt(this.mCapabilityBitmapNum);
        dest.writeIntArray(this.mCapabilityBitmap);
        dest.writeString(this.mReservedInfo);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public String getIpv4() {
        return this.mIpv4;
    }

    public String getIpv6() {
        return this.mIpv6;
    }

    public int getPort() {
        return this.mPort;
    }

    public String getBtMac() {
        return this.mBtMac;
    }

    public String getWifiMac() {
        return this.mWifiMac;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public int getCapabilityBitmapNum() {
        return this.mCapabilityBitmapNum;
    }

    public int[] getCapabilityBitmap() {
        return this.mCapabilityBitmap;
    }

    public String getReservedInfo() {
        return this.mReservedInfo;
    }

    public static class Builder {
        private DeviceDesc info = new DeviceDesc();

        public Builder deviceName(String deviceName) {
            this.info.mDeviceName = deviceName;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.info.mDeviceId = deviceId;
            return this;
        }

        public Builder deviceType(int deviceType) {
            this.info.mDeviceType = deviceType;
            return this;
        }

        public Builder wifiMac(String wifiMac) {
            this.info.mWifiMac = wifiMac;
            return this;
        }

        public Builder btMac(String btMac) {
            this.info.mBtMac = btMac;
            return this;
        }

        public Builder ipv4(String ipv4) {
            this.info.mIpv4 = ipv4;
            return this;
        }

        public Builder ipv6(String ipv6) {
            this.info.mIpv6 = ipv6;
            return this;
        }

        public Builder port(int port) {
            this.info.mPort = port;
            return this;
        }

        public Builder capabilityBitmapNum(int capabilityBitmapNum) {
            this.info.mCapabilityBitmapNum = capabilityBitmapNum;
            return this;
        }

        public Builder capabilityBitmap(int[] capabilityBitmap) {
            this.info.mCapabilityBitmap = capabilityBitmap;
            return this;
        }

        public Builder reservedInfo(String reservedInfo) {
            this.info.mReservedInfo = reservedInfo;
            return this;
        }

        public DeviceDesc build() {
            return this.info;
        }
    }
}
