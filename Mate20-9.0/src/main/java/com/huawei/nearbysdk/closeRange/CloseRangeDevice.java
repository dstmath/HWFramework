package com.huawei.nearbysdk.closeRange;

import android.os.Parcel;
import android.os.Parcelable;

public class CloseRangeDevice implements Parcelable {
    public static final Parcelable.Creator<CloseRangeDevice> CREATOR = new Parcelable.Creator<CloseRangeDevice>() {
        public CloseRangeDevice createFromParcel(Parcel source) {
            return new CloseRangeDevice(source.readString(), source.readString());
        }

        public CloseRangeDevice[] newArray(int size) {
            return new CloseRangeDevice[size];
        }
    };
    private static final int MAC_MASK = 5;
    private final String MAC;
    private final String localName;

    public CloseRangeDevice(String MAC2) {
        this.localName = null;
        this.MAC = MAC2;
    }

    public CloseRangeDevice(String localName2, String MAC2) {
        this.localName = localName2;
        this.MAC = MAC2;
    }

    public String getLocalName() {
        return this.localName;
    }

    public String getMAC() {
        return this.MAC;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.localName);
        dest.writeString(this.MAC);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloseRangeDevice)) {
            return false;
        }
        CloseRangeDevice device = (CloseRangeDevice) o;
        if (getMAC() != null) {
            z = getMAC().equals(device.getMAC());
        } else if (device.getMAC() != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (getMAC() != null) {
            return getMAC().hashCode();
        }
        return 0;
    }

    public String toString() {
        return "CloseRangeDevice{}";
    }
}
