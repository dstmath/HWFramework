package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public class ProjectionDevice implements Parcelable {
    public static final Parcelable.Creator<ProjectionDevice> CREATOR = new Parcelable.Creator<ProjectionDevice>() {
        /* class com.huawei.airsharing.api.ProjectionDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProjectionDevice[] newArray(int size) {
            return new ProjectionDevice[size];
        }

        @Override // android.os.Parcelable.Creator
        public ProjectionDevice createFromParcel(Parcel source) {
            ProjectionDevice device = new ProjectionDevice(source.readString(), source.readString(), source.readInt(), source.readInt(), source.readInt());
            device.setLocalIpAddress(source.readString());
            device.setRemoteIpAddress(source.readString());
            device.setConnectType(source.readInt());
            device.setSessionKey(source.createByteArray());
            device.setProjectionScene(source.readInt());
            device.setDeviceSubtype(source.readInt());
            return device;
        }
    };
    private static final int SESSION_KEY_MIN_LEN = 16;
    private int mCapability;
    private int mConnectType;
    private String mDeviceName;
    private int mDeviceSubtype;
    private int mDeviceType;
    private String mIndication;
    private String mLocalIpAddress;
    private int mPriority;
    private int mProjectionScene;
    private String mRemoteIpAddress;
    private byte[] mSessionKeys;

    public ProjectionDevice(String deviceName, String indication, int capability, int deviceType) {
        this(deviceName, indication, -1, capability, deviceType, 1);
    }

    public ProjectionDevice(String deviceName, String indication, int priority, int capability, int deviceType) {
        this(deviceName, indication, priority, capability, deviceType, 1);
    }

    public ProjectionDevice(String deviceName, String indication, int priority, int capability, int deviceType, int projectionScene) {
        this.mProjectionScene = 1;
        this.mSessionKeys = new byte[0];
        this.mDeviceName = deviceName;
        this.mIndication = indication;
        this.mPriority = priority;
        this.mCapability = capability;
        this.mDeviceType = deviceType;
        this.mProjectionScene = projectionScene;
        this.mConnectType = 0;
        this.mDeviceSubtype = 0;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    public String getIndication() {
        return this.mIndication;
    }

    public void setIndication(String indication) {
        this.mIndication = indication;
    }

    public int getPriority() {
        return this.mPriority;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
    }

    public int getCapability() {
        return this.mCapability;
    }

    public void setCapability(int capability) {
        this.mCapability = capability;
    }

    public void addCapability(int capability) {
        this.mCapability |= capability;
    }

    public void removeCapability(int capability) {
        this.mCapability &= ~capability;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public void setDeviceType(int deviceType) {
        this.mDeviceType = deviceType;
    }

    public int getDeviceSubtype() {
        return this.mDeviceSubtype;
    }

    public void setDeviceSubtype(int deviceType) {
        this.mDeviceSubtype = deviceType;
    }

    public boolean isHuaweiTv() {
        return this.mDeviceType == 2;
    }

    public void setLocalIpAddress(String localIpAddress) {
        this.mLocalIpAddress = localIpAddress;
    }

    public String getLocalIpAddress() {
        return this.mLocalIpAddress;
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        this.mRemoteIpAddress = remoteIpAddress;
    }

    public String getRemoteIpAddress() {
        return this.mRemoteIpAddress;
    }

    public void setConnectType(int connectType) {
        if (connectType != 1 || (this.mCapability & 32) == 0) {
            this.mConnectType = 0;
        } else {
            this.mConnectType = connectType;
        }
    }

    public int getConnectType() {
        return this.mConnectType;
    }

    public int getProjectionScene() {
        return this.mProjectionScene;
    }

    public void setProjectionScene(int projectionScene) {
        this.mProjectionScene = projectionScene;
    }

    public boolean setSessionKey(byte[] key) {
        if (key == null || key.length < 16) {
            return false;
        }
        byte[] bArr = this.mSessionKeys;
        if (bArr != null && bArr.length > 0) {
            System.arraycopy(new byte[bArr.length], 0, bArr, 0, bArr.length);
        }
        this.mSessionKeys = new byte[key.length];
        byte[] bArr2 = this.mSessionKeys;
        System.arraycopy(key, 0, bArr2, 0, bArr2.length);
        return true;
    }

    public byte[] getSessionKey() {
        return (byte[]) this.mSessionKeys.clone();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceName);
        dest.writeString(this.mIndication);
        dest.writeInt(this.mPriority);
        dest.writeInt(this.mCapability);
        dest.writeInt(this.mDeviceType);
        dest.writeString(this.mLocalIpAddress);
        dest.writeString(this.mRemoteIpAddress);
        dest.writeInt(this.mConnectType);
        dest.writeByteArray(this.mSessionKeys);
        dest.writeInt(this.mProjectionScene);
        dest.writeInt(this.mDeviceSubtype);
    }

    public String toString() {
        return "KitProjectionDevice[ priority:" + this.mPriority + ", capability:" + this.mCapability + ", deviceType:" + this.mDeviceType + ", subDevType:" + this.mDeviceSubtype + ",connectType: " + this.mConnectType + " ]";
    }
}
