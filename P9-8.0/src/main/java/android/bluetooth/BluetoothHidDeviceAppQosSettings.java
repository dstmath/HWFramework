package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class BluetoothHidDeviceAppQosSettings implements Parcelable {
    public static final Creator<BluetoothHidDeviceAppQosSettings> CREATOR = new Creator<BluetoothHidDeviceAppQosSettings>() {
        public BluetoothHidDeviceAppQosSettings createFromParcel(Parcel in) {
            return new BluetoothHidDeviceAppQosSettings(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
        }

        public BluetoothHidDeviceAppQosSettings[] newArray(int size) {
            return new BluetoothHidDeviceAppQosSettings[size];
        }
    };
    public static final int MAX = -1;
    public static final int SERVICE_BEST_EFFORT = 1;
    public static final int SERVICE_GUARANTEED = 2;
    public static final int SERVICE_NO_TRAFFIC = 0;
    public final int delayVariation;
    public final int latency;
    public final int peakBandwidth;
    public final int serviceType;
    public final int tokenBucketSize;
    public final int tokenRate;

    public BluetoothHidDeviceAppQosSettings(int serviceType, int tokenRate, int tokenBucketSize, int peakBandwidth, int latency, int delayVariation) {
        this.serviceType = serviceType;
        this.tokenRate = tokenRate;
        this.tokenBucketSize = tokenBucketSize;
        this.peakBandwidth = peakBandwidth;
        this.latency = latency;
        this.delayVariation = delayVariation;
    }

    public boolean equals(Object o) {
        if (!(o instanceof BluetoothHidDeviceAppQosSettings)) {
            return false;
        }
        BluetoothHidDeviceAppQosSettings qos = (BluetoothHidDeviceAppQosSettings) o;
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.serviceType);
        out.writeInt(this.tokenRate);
        out.writeInt(this.tokenBucketSize);
        out.writeInt(this.peakBandwidth);
        out.writeInt(this.latency);
        out.writeInt(this.delayVariation);
    }

    public int[] toArray() {
        return new int[]{this.serviceType, this.tokenRate, this.tokenBucketSize, this.peakBandwidth, this.latency, this.delayVariation};
    }
}
