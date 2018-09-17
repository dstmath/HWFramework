package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class BluetoothMasInstance implements Parcelable {
    public static final Creator<BluetoothMasInstance> CREATOR = new Creator<BluetoothMasInstance>() {
        public BluetoothMasInstance createFromParcel(Parcel in) {
            return new BluetoothMasInstance(in.readInt(), in.readString(), in.readInt(), in.readInt());
        }

        public BluetoothMasInstance[] newArray(int size) {
            return new BluetoothMasInstance[size];
        }
    };
    private final int mChannel;
    private final int mId;
    private final int mMsgTypes;
    private final String mName;

    public static final class MessageType {
        public static final int EMAIL = 1;
        public static final int MMS = 8;
        public static final int SMS_CDMA = 4;
        public static final int SMS_GSM = 2;
    }

    public BluetoothMasInstance(int id, String name, int channel, int msgTypes) {
        this.mId = id;
        this.mName = name;
        this.mChannel = channel;
        this.mMsgTypes = msgTypes;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof BluetoothMasInstance)) {
            return false;
        }
        if (this.mId == ((BluetoothMasInstance) o).mId) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (this.mId + (this.mChannel << 8)) + (this.mMsgTypes << 16);
    }

    public String toString() {
        return Integer.toString(this.mId) + ":" + this.mName + ":" + this.mChannel + ":" + Integer.toHexString(this.mMsgTypes);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mId);
        out.writeString(this.mName);
        out.writeInt(this.mChannel);
        out.writeInt(this.mMsgTypes);
    }

    public int getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public int getChannel() {
        return this.mChannel;
    }

    public int getMsgTypes() {
        return this.mMsgTypes;
    }

    public boolean msgSupported(int msg) {
        return (this.mMsgTypes & msg) != 0;
    }
}
