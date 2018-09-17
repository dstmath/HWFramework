package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class ContextHubMessage {
    public static final Creator<ContextHubMessage> CREATOR = null;
    private static final String TAG = "ContextHubMessage";
    private byte[] mData;
    private int mType;
    private int mVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.location.ContextHubMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.location.ContextHubMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubMessage.<clinit>():void");
    }

    public int getMsgType() {
        return this.mType;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public byte[] getData() {
        return Arrays.copyOf(this.mData, this.mData.length);
    }

    public void setMsgType(int msgType) {
        this.mType = msgType;
    }

    public void setVersion(int version) {
        this.mVersion = version;
    }

    public void setMsgData(byte[] data) {
        this.mData = Arrays.copyOf(data, data.length);
    }

    public ContextHubMessage(int msgType, int version, byte[] data) {
        this.mType = msgType;
        this.mVersion = version;
        this.mData = Arrays.copyOf(data, data.length);
    }

    public int describeContents() {
        return 0;
    }

    private ContextHubMessage(Parcel in) {
        this.mType = in.readInt();
        this.mVersion = in.readInt();
        this.mData = new byte[in.readInt()];
        in.readByteArray(this.mData);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mType);
        out.writeInt(this.mVersion);
        out.writeInt(this.mData.length);
        out.writeByteArray(this.mData);
    }
}
