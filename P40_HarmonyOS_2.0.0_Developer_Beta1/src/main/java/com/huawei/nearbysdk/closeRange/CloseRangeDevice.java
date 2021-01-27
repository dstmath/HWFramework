package com.huawei.nearbysdk.closeRange;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.powerkit.PowerKitRequester;

public class CloseRangeDevice implements Parcelable, PowerKitRequester {
    public static final Parcelable.Creator<CloseRangeDevice> CREATOR = new Parcelable.Creator<CloseRangeDevice>() {
        /* class com.huawei.nearbysdk.closeRange.CloseRangeDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CloseRangeDevice createFromParcel(Parcel source) {
            return new CloseRangeDevice(source.readString(), source.readString());
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.localName);
        dest.writeString(this.MAC);
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloseRangeDevice)) {
            return false;
        }
        CloseRangeDevice device = (CloseRangeDevice) o;
        if (getMAC() != null) {
            return getMAC().equals(device.getMAC());
        }
        return device.getMAC() == null;
    }

    @Override // java.lang.Object
    public int hashCode() {
        if (getMAC() != null) {
            return getMAC().hashCode();
        }
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return "CloseRangeDevice{}";
    }
}
