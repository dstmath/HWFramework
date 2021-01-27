package ohos.dmsdp.sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class DMSDPVirtualDevice implements Parcelable {
    public static final Parcelable.Creator<DMSDPVirtualDevice> CREATOR = new Parcelable.Creator<DMSDPVirtualDevice>() {
        /* class ohos.dmsdp.sdk.DMSDPVirtualDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DMSDPVirtualDevice createFromParcel(Parcel parcel) {
            return new DMSDPVirtualDevice(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DMSDPVirtualDevice[] newArray(int i) {
            return new DMSDPVirtualDevice[i];
        }
    };
    private static final String TAG = "DMSDPVirtualDevice";
    private String description;
    private String deviceId;
    private String deviceName;
    private int modemStatus;
    private String serviceId;
    private int serviceType;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public DMSDPVirtualDevice(String str, String str2, String str3, int i) {
        this.deviceId = str;
        this.deviceName = str2;
        this.serviceId = str3;
        this.serviceType = i;
    }

    protected DMSDPVirtualDevice(Parcel parcel) {
        this.deviceId = parcel.readString();
        this.deviceName = parcel.readString();
        this.serviceId = parcel.readString();
        this.serviceType = parcel.readInt();
        this.modemStatus = parcel.readInt();
        this.description = parcel.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.deviceId);
        parcel.writeString(this.deviceName);
        parcel.writeString(this.serviceId);
        parcel.writeInt(this.serviceType);
        parcel.writeInt(this.modemStatus);
        parcel.writeString(this.description);
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String str) {
        this.deviceName = str;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String str) {
        this.serviceId = str;
    }

    public int getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(int i) {
        this.serviceType = i;
    }

    public int getModemStatus() {
        return this.modemStatus;
    }

    public void setModemStatus(int i) {
        this.modemStatus = i;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
    }
}
