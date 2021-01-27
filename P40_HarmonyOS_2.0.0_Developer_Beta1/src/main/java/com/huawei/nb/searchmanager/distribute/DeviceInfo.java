package com.huawei.nb.searchmanager.distribute;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class DeviceInfo implements Parcelable {
    public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {
        /* class com.huawei.nb.searchmanager.distribute.DeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceInfo createFromParcel(Parcel parcel) {
            return new DeviceInfo(parcel.readString(), parcel.readString(), parcel.readString());
        }

        @Override // android.os.Parcelable.Creator
        public DeviceInfo[] newArray(int i) {
            return new DeviceInfo[0];
        }
    };
    private String deviceId;
    private String deviceName;
    private String deviceType;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public DeviceInfo(String str, String str2, String str3) {
        this.deviceId = str;
        this.deviceName = str2;
        this.deviceType = str3;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.deviceId);
        parcel.writeString(this.deviceName);
        parcel.writeString(this.deviceType);
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DeviceInfo)) {
            return false;
        }
        DeviceInfo deviceInfo = (DeviceInfo) obj;
        return Objects.equals(this.deviceId, deviceInfo.deviceId) && Objects.equals(this.deviceName, deviceInfo.deviceName) && Objects.equals(this.deviceType, deviceInfo.deviceType);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.deviceId, this.deviceName, this.deviceType);
    }

    @Override // java.lang.Object
    public String toString() {
        return "Type: " + this.deviceType + ", Name: " + this.deviceName + ", Id: " + this.deviceId;
    }
}
