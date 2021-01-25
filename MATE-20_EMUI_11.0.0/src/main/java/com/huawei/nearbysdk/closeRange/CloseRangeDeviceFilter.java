package com.huawei.nearbysdk.closeRange;

import android.os.Parcel;
import android.os.Parcelable;

public class CloseRangeDeviceFilter implements Parcelable {
    public static final Parcelable.Creator<CloseRangeDeviceFilter> CREATOR = new Parcelable.Creator<CloseRangeDeviceFilter>() {
        /* class com.huawei.nearbysdk.closeRange.CloseRangeDeviceFilter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CloseRangeDeviceFilter createFromParcel(Parcel source) {
            return new CloseRangeDeviceFilter((CloseRangeBusinessType) source.readParcelable(CloseRangeDeviceFilter.class.getClassLoader()), (CloseRangeDevice) source.readParcelable(CloseRangeDevice.class.getClassLoader()));
        }

        @Override // android.os.Parcelable.Creator
        public CloseRangeDeviceFilter[] newArray(int size) {
            return new CloseRangeDeviceFilter[size];
        }
    };
    private CloseRangeBusinessType businessType;
    private CloseRangeDevice device;

    private CloseRangeDeviceFilter(CloseRangeBusinessType businessType2, CloseRangeDevice device2) {
        this.businessType = businessType2;
        this.device = device2;
    }

    public static CloseRangeDeviceFilter buildFilter(CloseRangeBusinessType businessType2, String deviceMAC) {
        return new CloseRangeDeviceFilter(businessType2, new CloseRangeDevice(deviceMAC));
    }

    public CloseRangeDevice getDevice() {
        return this.device;
    }

    public String getDeviceMAC() {
        return this.device.getMAC();
    }

    public CloseRangeBusinessType getBusinessType() {
        return this.businessType;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.businessType, 0);
        dest.writeParcelable(this.device, 0);
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloseRangeDeviceFilter)) {
            return false;
        }
        CloseRangeDeviceFilter that = (CloseRangeDeviceFilter) o;
        if (!getBusinessType().equals(that.getBusinessType()) || !getDeviceMAC().equals(that.getDeviceMAC())) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return getBusinessType().getTag();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CloseRangeDeviceFilter{businessType=" + this.businessType + ", device=" + this.device + '}';
    }
}
