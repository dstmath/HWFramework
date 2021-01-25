package ohos.softnet.connect;

import java.util.Arrays;

public class DeviceDesc {
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

    private DeviceDesc() {
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

    public String toString() {
        return "{DeviceName=" + this.mDeviceName + ", DeviceId=" + this.mDeviceId + ", Ipv4=" + this.mIpv4 + ", Ipv6=" + this.mIpv6 + ", Port=" + this.mPort + ", WifiMac=" + this.mWifiMac + ", BtMac=" + this.mBtMac + ", DeviceType=" + this.mDeviceType + ", CapabilityBitmapNum=" + this.mCapabilityBitmapNum + ", CapabilityBitmap=" + Arrays.toString(this.mCapabilityBitmap) + ", ReservedInfo=" + this.mReservedInfo + "}";
    }

    public static class Builder {
        private DeviceDesc info = new DeviceDesc();

        public Builder deviceName(String str) {
            this.info.mDeviceName = str;
            return this;
        }

        public Builder deviceId(String str) {
            this.info.mDeviceId = str;
            return this;
        }

        public Builder deviceType(int i) {
            this.info.mDeviceType = i;
            return this;
        }

        public Builder wifiMac(String str) {
            this.info.mWifiMac = str;
            return this;
        }

        public Builder btMac(String str) {
            this.info.mBtMac = str;
            return this;
        }

        public Builder ipv4(String str) {
            this.info.mIpv4 = str;
            return this;
        }

        public Builder ipv6(String str) {
            this.info.mIpv6 = str;
            return this;
        }

        public Builder port(int i) {
            this.info.mPort = i;
            return this;
        }

        public Builder capabilityBitmapNum(int i) {
            this.info.mCapabilityBitmapNum = i;
            return this;
        }

        public Builder capabilityBitmap(int[] iArr) {
            this.info.mCapabilityBitmap = iArr;
            return this;
        }

        public Builder reservedInfo(String str) {
            this.info.mReservedInfo = str;
            return this;
        }

        public DeviceDesc build() {
            return this.info;
        }
    }
}
