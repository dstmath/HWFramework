package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.EventLog;

public final class BluetoothHidDeviceAppSdpSettings implements Parcelable {
    public static final Parcelable.Creator<BluetoothHidDeviceAppSdpSettings> CREATOR = new Parcelable.Creator<BluetoothHidDeviceAppSdpSettings>() {
        /* class android.bluetooth.BluetoothHidDeviceAppSdpSettings.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BluetoothHidDeviceAppSdpSettings createFromParcel(Parcel in) {
            return new BluetoothHidDeviceAppSdpSettings(in.readString(), in.readString(), in.readString(), in.readByte(), in.createByteArray());
        }

        @Override // android.os.Parcelable.Creator
        public BluetoothHidDeviceAppSdpSettings[] newArray(int size) {
            return new BluetoothHidDeviceAppSdpSettings[size];
        }
    };
    private static final int MAX_DESCRIPTOR_SIZE = 2048;
    private final String mDescription;
    private final byte[] mDescriptors;
    private final String mName;
    private final String mProvider;
    private final byte mSubclass;

    public BluetoothHidDeviceAppSdpSettings(String name, String description, String provider, byte subclass, byte[] descriptors) {
        this.mName = name;
        this.mDescription = description;
        this.mProvider = provider;
        this.mSubclass = subclass;
        if (descriptors == null || descriptors.length > 2048) {
            EventLog.writeEvent(1397638484, "119819889", -1, "");
            throw new IllegalArgumentException("descriptors must be not null and shorter than 2048");
        }
        this.mDescriptors = (byte[]) descriptors.clone();
    }

    public String getName() {
        return this.mName;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public String getProvider() {
        return this.mProvider;
    }

    public byte getSubclass() {
        return this.mSubclass;
    }

    public byte[] getDescriptors() {
        return this.mDescriptors;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mName);
        out.writeString(this.mDescription);
        out.writeString(this.mProvider);
        out.writeByte(this.mSubclass);
        out.writeByteArray(this.mDescriptors);
    }
}
