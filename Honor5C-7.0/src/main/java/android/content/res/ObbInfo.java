package android.content.res;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ObbInfo implements Parcelable {
    public static final Creator<ObbInfo> CREATOR = null;
    public static final int OBB_OVERLAY = 1;
    public String filename;
    public int flags;
    public String packageName;
    public byte[] salt;
    public int version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.ObbInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.res.ObbInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.ObbInfo.<clinit>():void");
    }

    ObbInfo() {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ObbInfo{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" packageName=");
        sb.append(this.packageName);
        sb.append(",version=");
        sb.append(this.version);
        sb.append(",flags=");
        sb.append(this.flags);
        sb.append('}');
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.filename);
        dest.writeString(this.packageName);
        dest.writeInt(this.version);
        dest.writeInt(this.flags);
        dest.writeByteArray(this.salt);
    }

    private ObbInfo(Parcel source) {
        this.filename = source.readString();
        this.packageName = source.readString();
        this.version = source.readInt();
        this.flags = source.readInt();
        this.salt = source.createByteArray();
    }
}
