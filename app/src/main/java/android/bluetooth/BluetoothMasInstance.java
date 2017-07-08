package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class BluetoothMasInstance implements Parcelable {
    public static final Creator<BluetoothMasInstance> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.BluetoothMasInstance.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.BluetoothMasInstance.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothMasInstance.<clinit>():void");
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
