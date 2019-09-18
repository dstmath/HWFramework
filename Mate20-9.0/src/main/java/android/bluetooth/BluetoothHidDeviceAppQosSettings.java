package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

public final class BluetoothHidDeviceAppQosSettings implements Parcelable {
    public static final Parcelable.Creator<BluetoothHidDeviceAppQosSettings> CREATOR = new Parcelable.Creator<BluetoothHidDeviceAppQosSettings>() {
        public BluetoothHidDeviceAppQosSettings createFromParcel(Parcel in) {
            BluetoothHidDeviceAppQosSettings bluetoothHidDeviceAppQosSettings = new BluetoothHidDeviceAppQosSettings(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
            return bluetoothHidDeviceAppQosSettings;
        }

        public BluetoothHidDeviceAppQosSettings[] newArray(int size) {
            return new BluetoothHidDeviceAppQosSettings[size];
        }
    };
    public static final int MAX = -1;
    public static final int SERVICE_BEST_EFFORT = 1;
    public static final int SERVICE_GUARANTEED = 2;
    public static final int SERVICE_NO_TRAFFIC = 0;
    private final int mDelayVariation;
    private final int mLatency;
    private final int mPeakBandwidth;
    private final int mServiceType;
    private final int mTokenBucketSize;
    private final int mTokenRate;

    public BluetoothHidDeviceAppQosSettings(int serviceType, int tokenRate, int tokenBucketSize, int peakBandwidth, int latency, int delayVariation) {
        this.mServiceType = serviceType;
        this.mTokenRate = tokenRate;
        this.mTokenBucketSize = tokenBucketSize;
        this.mPeakBandwidth = peakBandwidth;
        this.mLatency = latency;
        this.mDelayVariation = delayVariation;
    }

    public int getServiceType() {
        return this.mServiceType;
    }

    public int getTokenRate() {
        return this.mTokenRate;
    }

    public int getTokenBucketSize() {
        return this.mTokenBucketSize;
    }

    public int getPeakBandwidth() {
        return this.mPeakBandwidth;
    }

    public int getLatency() {
        return this.mLatency;
    }

    public int getDelayVariation() {
        return this.mDelayVariation;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mServiceType);
        out.writeInt(this.mTokenRate);
        out.writeInt(this.mTokenBucketSize);
        out.writeInt(this.mPeakBandwidth);
        out.writeInt(this.mLatency);
        out.writeInt(this.mDelayVariation);
    }
}
