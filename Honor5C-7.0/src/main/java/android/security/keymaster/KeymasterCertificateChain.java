package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class KeymasterCertificateChain implements Parcelable {
    public static final Creator<KeymasterCertificateChain> CREATOR = null;
    private List<byte[]> mCertificates;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.keymaster.KeymasterCertificateChain.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.keymaster.KeymasterCertificateChain.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.security.keymaster.KeymasterCertificateChain.<clinit>():void");
    }

    public KeymasterCertificateChain() {
        this.mCertificates = null;
    }

    public KeymasterCertificateChain(List<byte[]> mCertificates) {
        this.mCertificates = mCertificates;
    }

    private KeymasterCertificateChain(Parcel in) {
        readFromParcel(in);
    }

    public List<byte[]> getCertificates() {
        return this.mCertificates;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mCertificates == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.mCertificates.size());
        for (byte[] arg : this.mCertificates) {
            out.writeByteArray(arg);
        }
    }

    public void readFromParcel(Parcel in) {
        int length = in.readInt();
        this.mCertificates = new ArrayList(length);
        for (int i = 0; i < length; i++) {
            this.mCertificates.add(in.createByteArray());
        }
    }

    public int describeContents() {
        return 0;
    }
}
