package android.bluetooth;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import java.util.UUID;

public final class BluetoothHeadsetClientCall implements Parcelable {
    public static final int CALL_STATE_ACTIVE = 0;
    public static final int CALL_STATE_ALERTING = 3;
    public static final int CALL_STATE_DIALING = 2;
    public static final int CALL_STATE_HELD = 1;
    public static final int CALL_STATE_HELD_BY_RESPONSE_AND_HOLD = 6;
    public static final int CALL_STATE_INCOMING = 4;
    public static final int CALL_STATE_TERMINATED = 7;
    public static final int CALL_STATE_WAITING = 5;
    public static final Creator<BluetoothHeadsetClientCall> CREATOR = new Creator<BluetoothHeadsetClientCall>() {
        public BluetoothHeadsetClientCall createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            BluetoothDevice bluetoothDevice = (BluetoothDevice) in.readParcelable(null);
            int readInt = in.readInt();
            UUID fromString = UUID.fromString(in.readString());
            int readInt2 = in.readInt();
            String readString = in.readString();
            if (in.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            if (in.readInt() != 1) {
                z2 = false;
            }
            return new BluetoothHeadsetClientCall(bluetoothDevice, readInt, fromString, readInt2, readString, z, z2);
        }

        public BluetoothHeadsetClientCall[] newArray(int size) {
            return new BluetoothHeadsetClientCall[size];
        }
    };
    private final long mCreationElapsedMilli;
    private final BluetoothDevice mDevice;
    private final int mId;
    private boolean mMultiParty;
    private String mNumber;
    private final boolean mOutgoing;
    private int mState;
    private final UUID mUUID;

    public BluetoothHeadsetClientCall(BluetoothDevice device, int id, int state, String number, boolean multiParty, boolean outgoing) {
        this(device, id, UUID.randomUUID(), state, number, multiParty, outgoing);
    }

    public BluetoothHeadsetClientCall(BluetoothDevice device, int id, UUID uuid, int state, String number, boolean multiParty, boolean outgoing) {
        this.mDevice = device;
        this.mId = id;
        this.mUUID = uuid;
        this.mState = state;
        if (number == null) {
            number = ProxyInfo.LOCAL_EXCL_LIST;
        }
        this.mNumber = number;
        this.mMultiParty = multiParty;
        this.mOutgoing = outgoing;
        this.mCreationElapsedMilli = SystemClock.elapsedRealtime();
    }

    public void setState(int state) {
        this.mState = state;
    }

    public void setNumber(String number) {
        this.mNumber = number;
    }

    public void setMultiParty(boolean multiParty) {
        this.mMultiParty = multiParty;
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public int getId() {
        return this.mId;
    }

    public UUID getUUID() {
        return this.mUUID;
    }

    public int getState() {
        return this.mState;
    }

    public String getNumber() {
        return this.mNumber;
    }

    public long getCreationElapsedMilli() {
        return this.mCreationElapsedMilli;
    }

    public boolean isMultiParty() {
        return this.mMultiParty;
    }

    public boolean isOutgoing() {
        return this.mOutgoing;
    }

    public String toString() {
        return toString(false);
    }

    public String toString(boolean loggable) {
        Object obj;
        StringBuilder builder = new StringBuilder("BluetoothHeadsetClientCall{mDevice: ");
        builder.append(loggable ? this.mDevice : Integer.valueOf(this.mDevice.hashCode()));
        builder.append(", mId: ");
        builder.append(this.mId);
        builder.append(", mUUID: ");
        builder.append(this.mUUID);
        builder.append(", mState: ");
        switch (this.mState) {
            case 0:
                builder.append("ACTIVE");
                break;
            case 1:
                builder.append("HELD");
                break;
            case 2:
                builder.append("DIALING");
                break;
            case 3:
                builder.append("ALERTING");
                break;
            case 4:
                builder.append("INCOMING");
                break;
            case 5:
                builder.append("WAITING");
                break;
            case 6:
                builder.append("HELD_BY_RESPONSE_AND_HOLD");
                break;
            case 7:
                builder.append("TERMINATED");
                break;
            default:
                builder.append(this.mState);
                break;
        }
        builder.append(", mNumber: ");
        if (loggable) {
            obj = this.mNumber;
        } else {
            obj = Integer.valueOf(this.mNumber.hashCode());
        }
        builder.append(obj);
        builder.append(", mMultiParty: ");
        builder.append(this.mMultiParty);
        builder.append(", mOutgoing: ");
        builder.append(this.mOutgoing);
        builder.append("}");
        return builder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeParcelable(this.mDevice, 0);
        out.writeInt(this.mId);
        out.writeString(this.mUUID.toString());
        out.writeInt(this.mState);
        out.writeString(this.mNumber);
        if (this.mMultiParty) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.mOutgoing) {
            i2 = 0;
        }
        out.writeInt(i2);
    }

    public int describeContents() {
        return 0;
    }
}
