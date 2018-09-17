package tmsdk.common.module.intelli_sms;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.common.tcc.SmsCheckerContentTypes;
import tmsdk.common.tcc.SmsCheckerSuggestions;

/* compiled from: Unknown */
public final class IntelliSmsCheckResult implements Parcelable, SmsCheckerContentTypes, SmsCheckerSuggestions {
    public static final Creator<IntelliSmsCheckResult> CREATOR = null;
    private MMatchSysResult Cq;
    public int suggestion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.intelli_sms.IntelliSmsCheckResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.intelli_sms.IntelliSmsCheckResult.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.intelli_sms.IntelliSmsCheckResult.<clinit>():void");
    }

    public IntelliSmsCheckResult(int i, MMatchSysResult mMatchSysResult) {
        this.suggestion = i;
        this.Cq = mMatchSysResult;
    }

    public IntelliSmsCheckResult(Parcel parcel) {
        this.suggestion = parcel.readInt();
        this.Cq = (MMatchSysResult) parcel.readParcelable(MMatchSysResult.class.getClassLoader());
    }

    public static boolean shouldBeBlockedOrNot(IntelliSmsCheckResult intelliSmsCheckResult) {
        if (intelliSmsCheckResult != null) {
            if (intelliSmsCheckResult.suggestion == 3 || intelliSmsCheckResult.suggestion == 2) {
                return true;
            }
        }
        return false;
    }

    public int contentType() {
        return this.Cq != null ? this.Cq.contentType : 1;
    }

    public int describeContents() {
        return 0;
    }

    public Object getSysResult() {
        return this.Cq;
    }

    public boolean isCheatSMS() {
        return contentType() == 4;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.suggestion);
        parcel.writeParcelable(this.Cq, 0);
    }
}
