package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PreciseCallState implements Parcelable {
    public static final Creator<PreciseCallState> CREATOR = null;
    public static final int PRECISE_CALL_STATE_ACTIVE = 1;
    public static final int PRECISE_CALL_STATE_ALERTING = 4;
    public static final int PRECISE_CALL_STATE_DIALING = 3;
    public static final int PRECISE_CALL_STATE_DISCONNECTED = 7;
    public static final int PRECISE_CALL_STATE_DISCONNECTING = 8;
    public static final int PRECISE_CALL_STATE_HOLDING = 2;
    public static final int PRECISE_CALL_STATE_IDLE = 0;
    public static final int PRECISE_CALL_STATE_INCOMING = 5;
    public static final int PRECISE_CALL_STATE_NOT_VALID = -1;
    public static final int PRECISE_CALL_STATE_WAITING = 6;
    private int mBackgroundCallState;
    private int mDisconnectCause;
    private int mForegroundCallState;
    private int mPreciseDisconnectCause;
    private int mRingingCallState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.PreciseCallState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.PreciseCallState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.PreciseCallState.<clinit>():void");
    }

    public PreciseCallState(int ringingCall, int foregroundCall, int backgroundCall, int disconnectCause, int preciseDisconnectCause) {
        this.mRingingCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mForegroundCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mBackgroundCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mDisconnectCause = PRECISE_CALL_STATE_NOT_VALID;
        this.mPreciseDisconnectCause = PRECISE_CALL_STATE_NOT_VALID;
        this.mRingingCallState = ringingCall;
        this.mForegroundCallState = foregroundCall;
        this.mBackgroundCallState = backgroundCall;
        this.mDisconnectCause = disconnectCause;
        this.mPreciseDisconnectCause = preciseDisconnectCause;
    }

    public PreciseCallState() {
        this.mRingingCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mForegroundCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mBackgroundCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mDisconnectCause = PRECISE_CALL_STATE_NOT_VALID;
        this.mPreciseDisconnectCause = PRECISE_CALL_STATE_NOT_VALID;
    }

    private PreciseCallState(Parcel in) {
        this.mRingingCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mForegroundCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mBackgroundCallState = PRECISE_CALL_STATE_NOT_VALID;
        this.mDisconnectCause = PRECISE_CALL_STATE_NOT_VALID;
        this.mPreciseDisconnectCause = PRECISE_CALL_STATE_NOT_VALID;
        this.mRingingCallState = in.readInt();
        this.mForegroundCallState = in.readInt();
        this.mBackgroundCallState = in.readInt();
        this.mDisconnectCause = in.readInt();
        this.mPreciseDisconnectCause = in.readInt();
    }

    public int getRingingCallState() {
        return this.mRingingCallState;
    }

    public int getForegroundCallState() {
        return this.mForegroundCallState;
    }

    public int getBackgroundCallState() {
        return this.mBackgroundCallState;
    }

    public int getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public int getPreciseDisconnectCause() {
        return this.mPreciseDisconnectCause;
    }

    public int describeContents() {
        return PRECISE_CALL_STATE_IDLE;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mRingingCallState);
        out.writeInt(this.mForegroundCallState);
        out.writeInt(this.mBackgroundCallState);
        out.writeInt(this.mDisconnectCause);
        out.writeInt(this.mPreciseDisconnectCause);
    }

    public int hashCode() {
        return ((((((((this.mRingingCallState + 31) * 31) + this.mForegroundCallState) * 31) + this.mBackgroundCallState) * 31) + this.mDisconnectCause) * 31) + this.mPreciseDisconnectCause;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PreciseCallState other = (PreciseCallState) obj;
        if (this.mRingingCallState == other.mRingingCallState || this.mForegroundCallState == other.mForegroundCallState || this.mBackgroundCallState == other.mBackgroundCallState || this.mDisconnectCause == other.mDisconnectCause) {
            z = false;
        } else if (this.mPreciseDisconnectCause == other.mPreciseDisconnectCause) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Ringing call state: " + this.mRingingCallState);
        sb.append(", Foreground call state: " + this.mForegroundCallState);
        sb.append(", Background call state: " + this.mBackgroundCallState);
        sb.append(", Disconnect cause: " + this.mDisconnectCause);
        sb.append(", Precise disconnect cause: " + this.mPreciseDisconnectCause);
        return sb.toString();
    }
}
