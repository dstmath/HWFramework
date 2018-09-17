package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import java.util.Objects;

public final class BluetoothCodecStatus implements Parcelable {
    public static final Creator<BluetoothCodecStatus> CREATOR = new Creator<BluetoothCodecStatus>() {
        public BluetoothCodecStatus createFromParcel(Parcel in) {
            return new BluetoothCodecStatus((BluetoothCodecConfig) in.readTypedObject(BluetoothCodecConfig.CREATOR), (BluetoothCodecConfig[]) in.createTypedArray(BluetoothCodecConfig.CREATOR), (BluetoothCodecConfig[]) in.createTypedArray(BluetoothCodecConfig.CREATOR));
        }

        public BluetoothCodecStatus[] newArray(int size) {
            return new BluetoothCodecStatus[size];
        }
    };
    public static final String EXTRA_CODEC_STATUS = "android.bluetooth.codec.extra.CODEC_STATUS";
    private final BluetoothCodecConfig mCodecConfig;
    private final BluetoothCodecConfig[] mCodecsLocalCapabilities;
    private final BluetoothCodecConfig[] mCodecsSelectableCapabilities;

    public BluetoothCodecStatus(BluetoothCodecConfig codecConfig, BluetoothCodecConfig[] codecsLocalCapabilities, BluetoothCodecConfig[] codecsSelectableCapabilities) {
        this.mCodecConfig = codecConfig;
        this.mCodecsLocalCapabilities = codecsLocalCapabilities;
        this.mCodecsSelectableCapabilities = codecsSelectableCapabilities;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof BluetoothCodecStatus)) {
            return false;
        }
        BluetoothCodecStatus other = (BluetoothCodecStatus) o;
        if (Objects.equals(other.mCodecConfig, this.mCodecConfig) && Objects.equals(other.mCodecsLocalCapabilities, this.mCodecsLocalCapabilities)) {
            z = Objects.equals(other.mCodecsSelectableCapabilities, this.mCodecsSelectableCapabilities);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mCodecConfig, this.mCodecsLocalCapabilities, this.mCodecsLocalCapabilities});
    }

    public String toString() {
        return "{mCodecConfig:" + this.mCodecConfig + ",mCodecsLocalCapabilities:" + Arrays.toString(this.mCodecsLocalCapabilities) + ",mCodecsSelectableCapabilities:" + Arrays.toString(this.mCodecsSelectableCapabilities) + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedObject(this.mCodecConfig, 0);
        out.writeTypedArray(this.mCodecsLocalCapabilities, 0);
        out.writeTypedArray(this.mCodecsSelectableCapabilities, 0);
    }

    public BluetoothCodecConfig getCodecConfig() {
        return this.mCodecConfig;
    }

    public BluetoothCodecConfig[] getCodecsLocalCapabilities() {
        return this.mCodecsLocalCapabilities;
    }

    public BluetoothCodecConfig[] getCodecsSelectableCapabilities() {
        return this.mCodecsSelectableCapabilities;
    }
}
