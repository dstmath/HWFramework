package tmsdk.common.module.aresengine;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.jf;

/* compiled from: Unknown */
public class MmsHeader extends jf implements Parcelable {
    public static final Creator<MmsHeader> CREATOR = null;
    public int messageType;
    public byte[] messageclass;
    public int mmsVersion;
    public int phonenumCharset;
    public String subject;
    public int subjectCharset;
    public byte[] transactionId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.aresengine.MmsHeader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.aresengine.MmsHeader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.aresengine.MmsHeader.<clinit>():void");
    }

    public MmsHeader(Parcel parcel) {
        this.phonenumCharset = parcel.readInt();
        this.subject = parcel.readString();
        this.subjectCharset = parcel.readInt();
        this.messageclass = parcel.createByteArray();
        this.messageType = parcel.readInt();
        this.transactionId = parcel.createByteArray();
        this.mmsVersion = parcel.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.phonenumCharset);
        parcel.writeString(this.subject);
        parcel.writeInt(this.subjectCharset);
        parcel.writeByteArray(this.messageclass);
        parcel.writeInt(this.messageType);
        parcel.writeByteArray(this.transactionId);
        parcel.writeInt(this.mmsVersion);
    }
}
