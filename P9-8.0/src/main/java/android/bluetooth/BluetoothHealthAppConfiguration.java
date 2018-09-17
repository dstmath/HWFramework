package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class BluetoothHealthAppConfiguration implements Parcelable {
    public static final Creator<BluetoothHealthAppConfiguration> CREATOR = new Creator<BluetoothHealthAppConfiguration>() {
        public BluetoothHealthAppConfiguration createFromParcel(Parcel in) {
            return new BluetoothHealthAppConfiguration(in.readString(), in.readInt(), in.readInt(), in.readInt());
        }

        public BluetoothHealthAppConfiguration[] newArray(int size) {
            return new BluetoothHealthAppConfiguration[size];
        }
    };
    private final int mChannelType;
    private final int mDataType;
    private final String mName;
    private final int mRole;

    BluetoothHealthAppConfiguration(String name, int dataType) {
        this.mName = name;
        this.mDataType = dataType;
        this.mRole = 2;
        this.mChannelType = 12;
    }

    BluetoothHealthAppConfiguration(String name, int dataType, int role, int channelType) {
        this.mName = name;
        this.mDataType = dataType;
        this.mRole = role;
        this.mChannelType = channelType;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof BluetoothHealthAppConfiguration)) {
            return false;
        }
        BluetoothHealthAppConfiguration config = (BluetoothHealthAppConfiguration) o;
        if (this.mName == null) {
            return false;
        }
        if (this.mName.equals(config.getName()) && this.mDataType == config.getDataType() && this.mRole == config.getRole() && this.mChannelType == config.getChannelType()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (((((((this.mName != null ? this.mName.hashCode() : 0) + 527) * 31) + this.mDataType) * 31) + this.mRole) * 31) + this.mChannelType;
    }

    public String toString() {
        return "BluetoothHealthAppConfiguration [mName = " + this.mName + ",mDataType = " + this.mDataType + ", mRole = " + this.mRole + ",mChannelType = " + this.mChannelType + "]";
    }

    public int describeContents() {
        return 0;
    }

    public int getDataType() {
        return this.mDataType;
    }

    public String getName() {
        return this.mName;
    }

    public int getRole() {
        return this.mRole;
    }

    public int getChannelType() {
        return this.mChannelType;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mName);
        out.writeInt(this.mDataType);
        out.writeInt(this.mRole);
        out.writeInt(this.mChannelType);
    }
}
