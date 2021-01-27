package com.huawei.dmsdpsdk2;

import android.os.Parcel;
import android.os.Parcelable;

public class DMSDPVirtualDevice implements Parcelable {
    public static final Parcelable.Creator<DMSDPVirtualDevice> CREATOR = new Parcelable.Creator<DMSDPVirtualDevice>() {
        /* class com.huawei.dmsdpsdk2.DMSDPVirtualDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DMSDPVirtualDevice createFromParcel(Parcel in) {
            return new DMSDPVirtualDevice(in);
        }

        @Override // android.os.Parcelable.Creator
        public DMSDPVirtualDevice[] newArray(int size) {
            return new DMSDPVirtualDevice[size];
        }
    };
    private static final String TAG = "DMSDPVirtualDevice";
    private String description;
    private String deviceId;
    private String deviceName;
    private int modemStatus;
    private String serviceId;
    private int serviceType;

    public DMSDPVirtualDevice(String deviceId2, String deviceName2, String serviceId2, int serviceType2) {
        this.deviceId = deviceId2;
        this.deviceName = deviceName2;
        this.serviceId = serviceId2;
        this.serviceType = serviceType2;
    }

    protected DMSDPVirtualDevice(Parcel in) {
        this.deviceId = in.readString();
        this.deviceName = in.readString();
        this.serviceId = in.readString();
        this.serviceType = in.readInt();
        this.modemStatus = in.readInt();
        this.description = in.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceId);
        dest.writeString(this.deviceName);
        dest.writeString(this.serviceId);
        dest.writeInt(this.serviceType);
        dest.writeInt(this.modemStatus);
        dest.writeString(this.description);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName2) {
        this.deviceName = deviceName2;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId2) {
        this.serviceId = serviceId2;
    }

    public int getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(int serviceType2) {
        this.serviceType = serviceType2;
    }

    public int getModemStatus() {
        return this.modemStatus;
    }

    public void setModemStatus(int modemStatus2) {
        this.modemStatus = modemStatus2;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description2) {
        this.description = description2;
    }
}
