package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PreciseCallState implements Parcelable {
    public static final Creator<PreciseCallState> CREATOR = new Creator<PreciseCallState>() {
        public PreciseCallState createFromParcel(Parcel in) {
            return new PreciseCallState(in, null);
        }

        public PreciseCallState[] newArray(int size) {
            return new PreciseCallState[size];
        }
    };
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

    /* synthetic */ PreciseCallState(Parcel in, PreciseCallState -this1) {
        this(in);
    }

    public PreciseCallState(int ringingCall, int foregroundCall, int backgroundCall, int disconnectCause, int preciseDisconnectCause) {
        this.mRingingCallState = -1;
        this.mForegroundCallState = -1;
        this.mBackgroundCallState = -1;
        this.mDisconnectCause = -1;
        this.mPreciseDisconnectCause = -1;
        this.mRingingCallState = ringingCall;
        this.mForegroundCallState = foregroundCall;
        this.mBackgroundCallState = backgroundCall;
        this.mDisconnectCause = disconnectCause;
        this.mPreciseDisconnectCause = preciseDisconnectCause;
    }

    public PreciseCallState() {
        this.mRingingCallState = -1;
        this.mForegroundCallState = -1;
        this.mBackgroundCallState = -1;
        this.mDisconnectCause = -1;
        this.mPreciseDisconnectCause = -1;
    }

    private PreciseCallState(Parcel in) {
        this.mRingingCallState = -1;
        this.mForegroundCallState = -1;
        this.mBackgroundCallState = -1;
        this.mDisconnectCause = -1;
        this.mPreciseDisconnectCause = -1;
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
        return 0;
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
