package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Random;

public final class BluetoothHidDeviceAppConfiguration implements Parcelable {
    public static final Creator<BluetoothHidDeviceAppConfiguration> CREATOR = new Creator<BluetoothHidDeviceAppConfiguration>() {
        public BluetoothHidDeviceAppConfiguration createFromParcel(Parcel in) {
            return new BluetoothHidDeviceAppConfiguration(in.readLong());
        }

        public BluetoothHidDeviceAppConfiguration[] newArray(int size) {
            return new BluetoothHidDeviceAppConfiguration[size];
        }
    };
    private final long mHash;

    BluetoothHidDeviceAppConfiguration() {
        this.mHash = new Random().nextLong();
    }

    BluetoothHidDeviceAppConfiguration(long hash) {
        this.mHash = hash;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof BluetoothHidDeviceAppConfiguration)) {
            return false;
        }
        if (this.mHash == ((BluetoothHidDeviceAppConfiguration) o).mHash) {
            z = true;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mHash);
    }
}
