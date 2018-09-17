package android.telephony;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class VoLteServiceState implements Parcelable {
    public static final Creator<VoLteServiceState> CREATOR = null;
    private static final boolean DBG = false;
    public static final int HANDOVER_CANCELED = 3;
    public static final int HANDOVER_COMPLETED = 1;
    public static final int HANDOVER_FAILED = 2;
    public static final int HANDOVER_STARTED = 0;
    public static final int INVALID = Integer.MAX_VALUE;
    private static final String LOG_TAG = "VoLteServiceState";
    public static final int NOT_SUPPORTED = 0;
    public static final int SUPPORTED = 1;
    private int mSrvccState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.VoLteServiceState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.VoLteServiceState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.VoLteServiceState.<clinit>():void");
    }

    public static VoLteServiceState newFromBundle(Bundle m) {
        VoLteServiceState ret = new VoLteServiceState();
        ret.setFromNotifierBundle(m);
        return ret;
    }

    public VoLteServiceState() {
        initialize();
    }

    public VoLteServiceState(int srvccState) {
        initialize();
        this.mSrvccState = srvccState;
    }

    public VoLteServiceState(VoLteServiceState s) {
        copyFrom(s);
    }

    private void initialize() {
        this.mSrvccState = INVALID;
    }

    protected void copyFrom(VoLteServiceState s) {
        this.mSrvccState = s.mSrvccState;
    }

    public VoLteServiceState(Parcel in) {
        this.mSrvccState = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mSrvccState);
    }

    public int describeContents() {
        return NOT_SUPPORTED;
    }

    public void validateInput() {
    }

    public int hashCode() {
        return this.mSrvccState * 31;
    }

    public boolean equals(Object o) {
        boolean z = DBG;
        try {
            VoLteServiceState s = (VoLteServiceState) o;
            if (o == null) {
                return DBG;
            }
            if (this.mSrvccState == s.mSrvccState) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return DBG;
        }
    }

    public String toString() {
        return "VoLteServiceState: " + this.mSrvccState;
    }

    private void setFromNotifierBundle(Bundle m) {
        this.mSrvccState = m.getInt("mSrvccState");
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putInt("mSrvccState", this.mSrvccState);
    }

    public int getSrvccState() {
        return this.mSrvccState;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
