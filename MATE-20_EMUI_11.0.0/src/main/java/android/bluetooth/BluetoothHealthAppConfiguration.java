package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

public final class BluetoothHealthAppConfiguration implements Parcelable {
    public static final Parcelable.Creator<BluetoothHealthAppConfiguration> CREATOR = new Parcelable.Creator<BluetoothHealthAppConfiguration>() {
        /* class android.bluetooth.BluetoothHealthAppConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BluetoothHealthAppConfiguration createFromParcel(Parcel in) {
            return new BluetoothHealthAppConfiguration(in.readString(), in.readInt(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
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
        if (!(o instanceof BluetoothHealthAppConfiguration)) {
            return false;
        }
        BluetoothHealthAppConfiguration config = (BluetoothHealthAppConfiguration) o;
        String str = this.mName;
        if (str != null && str.equals(config.getName()) && this.mDataType == config.getDataType() && this.mRole == config.getRole() && this.mChannelType == config.getChannelType()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int i = 17 * 31;
        String str = this.mName;
        return ((((((i + (str != null ? str.hashCode() : 0)) * 31) + this.mDataType) * 31) + this.mRole) * 31) + this.mChannelType;
    }

    public String toString() {
        return "BluetoothHealthAppConfiguration [mName = " + this.mName + ",mDataType = " + this.mDataType + ", mRole = " + this.mRole + ",mChannelType = " + this.mChannelType + "]";
    }

    @Override // android.os.Parcelable
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mName);
        out.writeInt(this.mDataType);
        out.writeInt(this.mRole);
        out.writeInt(this.mChannelType);
    }
}
