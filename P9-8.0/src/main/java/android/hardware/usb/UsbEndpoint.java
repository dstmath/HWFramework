package android.hardware.usb;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UsbEndpoint implements Parcelable {
    public static final Creator<UsbEndpoint> CREATOR = new Creator<UsbEndpoint>() {
        public UsbEndpoint createFromParcel(Parcel in) {
            return new UsbEndpoint(in.readInt(), in.readInt(), in.readInt(), in.readInt());
        }

        public UsbEndpoint[] newArray(int size) {
            return new UsbEndpoint[size];
        }
    };
    private final int mAddress;
    private final int mAttributes;
    private final int mInterval;
    private final int mMaxPacketSize;

    public UsbEndpoint(int address, int attributes, int maxPacketSize, int interval) {
        this.mAddress = address;
        this.mAttributes = attributes;
        this.mMaxPacketSize = maxPacketSize;
        this.mInterval = interval;
    }

    public int getAddress() {
        return this.mAddress;
    }

    public int getEndpointNumber() {
        return this.mAddress & 15;
    }

    public int getDirection() {
        return this.mAddress & 128;
    }

    public int getAttributes() {
        return this.mAttributes;
    }

    public int getType() {
        return this.mAttributes & 3;
    }

    public int getMaxPacketSize() {
        return this.mMaxPacketSize;
    }

    public int getInterval() {
        return this.mInterval;
    }

    public String toString() {
        return "UsbEndpoint[mAddress=" + this.mAddress + ",mAttributes=" + this.mAttributes + ",mMaxPacketSize=" + this.mMaxPacketSize + ",mInterval=" + this.mInterval + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mAddress);
        parcel.writeInt(this.mAttributes);
        parcel.writeInt(this.mMaxPacketSize);
        parcel.writeInt(this.mInterval);
    }
}
