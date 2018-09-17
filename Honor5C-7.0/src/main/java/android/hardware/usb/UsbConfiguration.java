package android.hardware.usb;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UsbConfiguration implements Parcelable {
    private static final int ATTR_REMOTE_WAKEUP = 32;
    private static final int ATTR_SELF_POWERED = 64;
    public static final Creator<UsbConfiguration> CREATOR = null;
    private final int mAttributes;
    private final int mId;
    private Parcelable[] mInterfaces;
    private final int mMaxPower;
    private final String mName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.usb.UsbConfiguration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.usb.UsbConfiguration.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.usb.UsbConfiguration.<clinit>():void");
    }

    public UsbConfiguration(int id, String name, int attributes, int maxPower) {
        this.mId = id;
        this.mName = name;
        this.mAttributes = attributes;
        this.mMaxPower = maxPower;
    }

    public int getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public boolean isSelfPowered() {
        return (this.mAttributes & ATTR_SELF_POWERED) != 0;
    }

    public boolean isRemoteWakeup() {
        return (this.mAttributes & ATTR_REMOTE_WAKEUP) != 0;
    }

    public int getMaxPower() {
        return this.mMaxPower * 2;
    }

    public int getInterfaceCount() {
        return this.mInterfaces.length;
    }

    public UsbInterface getInterface(int index) {
        return (UsbInterface) this.mInterfaces[index];
    }

    public void setInterfaces(Parcelable[] interfaces) {
        this.mInterfaces = interfaces;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("UsbConfiguration[mId=" + this.mId + ",mName=" + this.mName + ",mAttributes=" + this.mAttributes + ",mMaxPower=" + this.mMaxPower + ",mInterfaces=[");
        for (Object obj : this.mInterfaces) {
            builder.append("\n");
            builder.append(obj.toString());
        }
        builder.append("]");
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mId);
        parcel.writeString(this.mName);
        parcel.writeInt(this.mAttributes);
        parcel.writeInt(this.mMaxPower);
        parcel.writeParcelableArray(this.mInterfaces, 0);
    }
}
