package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.SettingsStringUtil;

public final class BluetoothMasInstance implements Parcelable {
    public static final Parcelable.Creator<BluetoothMasInstance> CREATOR = new Parcelable.Creator<BluetoothMasInstance>() {
        /* class android.bluetooth.BluetoothMasInstance.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BluetoothMasInstance createFromParcel(Parcel in) {
            return new BluetoothMasInstance(in.readInt(), in.readString(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
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
        if (!(o instanceof BluetoothMasInstance) || this.mId != ((BluetoothMasInstance) o).mId) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.mId + (this.mChannel << 8) + (this.mMsgTypes << 16);
    }

    public String toString() {
        return Integer.toString(this.mId) + SettingsStringUtil.DELIMITER + this.mName + SettingsStringUtil.DELIMITER + this.mChannel + SettingsStringUtil.DELIMITER + Integer.toHexString(this.mMsgTypes);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
