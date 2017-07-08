package android.bluetooth;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
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
    public static final Creator<BluetoothHeadsetClientCall> CREATOR = null;
    private final BluetoothDevice mDevice;
    private final int mId;
    private boolean mMultiParty;
    private String mNumber;
    private final boolean mOutgoing;
    private int mState;
    private final UUID mUUID;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.BluetoothHeadsetClientCall.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.BluetoothHeadsetClientCall.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothHeadsetClientCall.<clinit>():void");
    }

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
            case CALL_STATE_ACTIVE /*0*/:
                builder.append("ACTIVE");
                break;
            case CALL_STATE_HELD /*1*/:
                builder.append("HELD");
                break;
            case CALL_STATE_DIALING /*2*/:
                builder.append("DIALING");
                break;
            case CALL_STATE_ALERTING /*3*/:
                builder.append("ALERTING");
                break;
            case CALL_STATE_INCOMING /*4*/:
                builder.append("INCOMING");
                break;
            case CALL_STATE_WAITING /*5*/:
                builder.append("WAITING");
                break;
            case CALL_STATE_HELD_BY_RESPONSE_AND_HOLD /*6*/:
                builder.append("HELD_BY_RESPONSE_AND_HOLD");
                break;
            case CALL_STATE_TERMINATED /*7*/:
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
        int i2 = CALL_STATE_HELD;
        out.writeParcelable(this.mDevice, CALL_STATE_ACTIVE);
        out.writeInt(this.mId);
        out.writeString(this.mUUID.toString());
        out.writeInt(this.mState);
        out.writeString(this.mNumber);
        if (this.mMultiParty) {
            i = CALL_STATE_HELD;
        } else {
            i = CALL_STATE_ACTIVE;
        }
        out.writeInt(i);
        if (!this.mOutgoing) {
            i2 = CALL_STATE_ACTIVE;
        }
        out.writeInt(i2);
    }

    public int describeContents() {
        return CALL_STATE_ACTIVE;
    }
}
