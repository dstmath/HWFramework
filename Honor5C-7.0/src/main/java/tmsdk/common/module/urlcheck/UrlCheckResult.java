package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.et;

/* compiled from: Unknown */
public class UrlCheckResult implements Parcelable {
    public static Creator<UrlCheckResult> CREATOR = null;
    public static final int RESULT_HARM = 3;
    public static final int RESULT_REGULAR = 0;
    public static final int RESULT_SHADINESS = 2;
    public static final int RESULT_UNKNOWN = Integer.MAX_VALUE;
    public int mErrCode;
    public int mainHarmId;
    public int result;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.urlcheck.UrlCheckResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.urlcheck.UrlCheckResult.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.urlcheck.UrlCheckResult.<clinit>():void");
    }

    private UrlCheckResult() {
        this.mainHarmId = RESULT_UNKNOWN;
        this.result = RESULT_UNKNOWN;
        this.mErrCode = RESULT_REGULAR;
    }

    public UrlCheckResult(int i) {
        this.mainHarmId = RESULT_UNKNOWN;
        this.result = RESULT_UNKNOWN;
        this.mErrCode = RESULT_REGULAR;
        this.mErrCode = i;
    }

    public UrlCheckResult(et etVar) {
        this.mainHarmId = RESULT_UNKNOWN;
        this.result = RESULT_UNKNOWN;
        this.mErrCode = RESULT_REGULAR;
        this.mainHarmId = etVar.mainHarmId;
        if (this.mainHarmId == 13) {
            this.mainHarmId = RESULT_REGULAR;
        }
        this.result = etVar.ld;
        if (this.result == 1) {
            this.result = RESULT_REGULAR;
        }
    }

    public int describeContents() {
        return RESULT_REGULAR;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mainHarmId);
        parcel.writeInt(this.result);
        parcel.writeInt(this.mErrCode);
    }
}
