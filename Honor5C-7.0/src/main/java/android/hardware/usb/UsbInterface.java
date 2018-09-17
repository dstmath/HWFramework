package android.hardware.usb;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UsbInterface implements Parcelable {
    public static final Creator<UsbInterface> CREATOR = null;
    private final int mAlternateSetting;
    private final int mClass;
    private Parcelable[] mEndpoints;
    private final int mId;
    private final String mName;
    private final int mProtocol;
    private final int mSubclass;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.usb.UsbInterface.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.usb.UsbInterface.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.usb.UsbInterface.<clinit>():void");
    }

    public UsbInterface(int id, int alternateSetting, String name, int Class, int subClass, int protocol) {
        this.mId = id;
        this.mAlternateSetting = alternateSetting;
        this.mName = name;
        this.mClass = Class;
        this.mSubclass = subClass;
        this.mProtocol = protocol;
    }

    public int getId() {
        return this.mId;
    }

    public int getAlternateSetting() {
        return this.mAlternateSetting;
    }

    public String getName() {
        return this.mName;
    }

    public int getInterfaceClass() {
        return this.mClass;
    }

    public int getInterfaceSubclass() {
        return this.mSubclass;
    }

    public int getInterfaceProtocol() {
        return this.mProtocol;
    }

    public int getEndpointCount() {
        return this.mEndpoints.length;
    }

    public UsbEndpoint getEndpoint(int index) {
        return (UsbEndpoint) this.mEndpoints[index];
    }

    public void setEndpoints(Parcelable[] endpoints) {
        this.mEndpoints = endpoints;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("UsbInterface[mId=" + this.mId + ",mAlternateSetting=" + this.mAlternateSetting + ",mName=" + this.mName + ",mClass=" + this.mClass + ",mSubclass=" + this.mSubclass + ",mProtocol=" + this.mProtocol + ",mEndpoints=[");
        for (Object obj : this.mEndpoints) {
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
        parcel.writeInt(this.mAlternateSetting);
        parcel.writeString(this.mName);
        parcel.writeInt(this.mClass);
        parcel.writeInt(this.mSubclass);
        parcel.writeInt(this.mProtocol);
        parcel.writeParcelableArray(this.mEndpoints, 0);
    }
}
