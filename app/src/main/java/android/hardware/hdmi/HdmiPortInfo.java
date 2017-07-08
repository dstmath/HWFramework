package android.hardware.hdmi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class HdmiPortInfo implements Parcelable {
    public static final Creator<HdmiPortInfo> CREATOR = null;
    public static final int PORT_INPUT = 0;
    public static final int PORT_OUTPUT = 1;
    private final int mAddress;
    private final boolean mArcSupported;
    private final boolean mCecSupported;
    private final int mId;
    private final boolean mMhlSupported;
    private final int mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.hdmi.HdmiPortInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.hdmi.HdmiPortInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiPortInfo.<clinit>():void");
    }

    public HdmiPortInfo(int id, int type, int address, boolean cec, boolean mhl, boolean arc) {
        this.mId = id;
        this.mType = type;
        this.mAddress = address;
        this.mCecSupported = cec;
        this.mArcSupported = arc;
        this.mMhlSupported = mhl;
    }

    public int getId() {
        return this.mId;
    }

    public int getType() {
        return this.mType;
    }

    public int getAddress() {
        return this.mAddress;
    }

    public boolean isCecSupported() {
        return this.mCecSupported;
    }

    public boolean isMhlSupported() {
        return this.mMhlSupported;
    }

    public boolean isArcSupported() {
        return this.mArcSupported;
    }

    public int describeContents() {
        return PORT_INPUT;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = PORT_OUTPUT;
        dest.writeInt(this.mId);
        dest.writeInt(this.mType);
        dest.writeInt(this.mAddress);
        if (this.mCecSupported) {
            i = PORT_OUTPUT;
        } else {
            i = PORT_INPUT;
        }
        dest.writeInt(i);
        if (this.mArcSupported) {
            i = PORT_OUTPUT;
        } else {
            i = PORT_INPUT;
        }
        dest.writeInt(i);
        if (!this.mMhlSupported) {
            i2 = PORT_INPUT;
        }
        dest.writeInt(i2);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("port_id: ").append(this.mId).append(", ");
        StringBuffer append = s.append("address: ");
        Object[] objArr = new Object[PORT_OUTPUT];
        objArr[PORT_INPUT] = Integer.valueOf(this.mAddress);
        append.append(String.format("0x%04x", objArr)).append(", ");
        s.append("cec: ").append(this.mCecSupported).append(", ");
        s.append("arc: ").append(this.mArcSupported).append(", ");
        s.append("mhl: ").append(this.mMhlSupported);
        return s.toString();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof HdmiPortInfo)) {
            return false;
        }
        HdmiPortInfo other = (HdmiPortInfo) o;
        if (this.mId == other.mId && this.mType == other.mType && this.mAddress == other.mAddress && this.mCecSupported == other.mCecSupported && this.mArcSupported == other.mArcSupported && this.mMhlSupported == other.mMhlSupported) {
            z = true;
        }
        return z;
    }
}
