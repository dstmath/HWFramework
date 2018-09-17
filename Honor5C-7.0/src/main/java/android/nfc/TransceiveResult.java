package android.nfc;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.IOException;

public final class TransceiveResult implements Parcelable {
    public static final Creator<TransceiveResult> CREATOR = null;
    public static final int RESULT_EXCEEDED_LENGTH = 3;
    public static final int RESULT_FAILURE = 1;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_TAGLOST = 2;
    final byte[] mResponseData;
    final int mResult;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.nfc.TransceiveResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.nfc.TransceiveResult.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.TransceiveResult.<clinit>():void");
    }

    public TransceiveResult(int result, byte[] data) {
        this.mResult = result;
        this.mResponseData = data;
    }

    public byte[] getResponseOrThrow() throws IOException {
        switch (this.mResult) {
            case RESULT_SUCCESS /*0*/:
                return this.mResponseData;
            case RESULT_TAGLOST /*2*/:
                throw new TagLostException("Tag was lost.");
            case RESULT_EXCEEDED_LENGTH /*3*/:
                throw new IOException("Transceive length exceeds supported maximum");
            default:
                throw new IOException("Transceive failed");
        }
    }

    public int describeContents() {
        return RESULT_SUCCESS;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResult);
        if (this.mResult == 0) {
            dest.writeInt(this.mResponseData.length);
            dest.writeByteArray(this.mResponseData);
        }
    }
}
