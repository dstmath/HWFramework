package android.telephony.ims;

import android.annotation.SystemApi;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.telecom.Log;
import android.telephony.Rlog;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class ImsExternalCallState implements Parcelable {
    public static final int CALL_STATE_CONFIRMED = 1;
    public static final int CALL_STATE_TERMINATED = 2;
    public static final Parcelable.Creator<ImsExternalCallState> CREATOR = new Parcelable.Creator<ImsExternalCallState>() {
        /* class android.telephony.ims.ImsExternalCallState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImsExternalCallState createFromParcel(Parcel in) {
            return new ImsExternalCallState(in);
        }

        @Override // android.os.Parcelable.Creator
        public ImsExternalCallState[] newArray(int size) {
            return new ImsExternalCallState[size];
        }
    };
    private static final String TAG = "ImsExternalCallState";
    private Uri mAddress;
    private int mCallId;
    private int mCallState;
    private int mCallType;
    private boolean mIsHeld;
    private boolean mIsPullable;
    private Uri mLocalAddress;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ExternalCallState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ExternalCallType {
    }

    public ImsExternalCallState() {
    }

    public ImsExternalCallState(int callId, Uri address, boolean isPullable, int callState, int callType, boolean isCallheld) {
        this.mCallId = callId;
        this.mAddress = address;
        this.mIsPullable = isPullable;
        this.mCallState = callState;
        this.mCallType = callType;
        this.mIsHeld = isCallheld;
        Rlog.d(TAG, "ImsExternalCallState = " + this);
    }

    public ImsExternalCallState(int callId, Uri address, Uri localAddress, boolean isPullable, int callState, int callType, boolean isCallheld) {
        this.mCallId = callId;
        this.mAddress = address;
        this.mLocalAddress = localAddress;
        this.mIsPullable = isPullable;
        this.mCallState = callState;
        this.mCallType = callType;
        this.mIsHeld = isCallheld;
        Rlog.d(TAG, "ImsExternalCallState = " + this);
    }

    public ImsExternalCallState(String callId, Uri address, Uri localAddress, boolean isPullable, int callState, int callType, boolean isCallheld) {
        this.mCallId = getIdForString(callId);
        this.mAddress = address;
        this.mLocalAddress = localAddress;
        this.mIsPullable = isPullable;
        this.mCallState = callState;
        this.mCallType = callType;
        this.mIsHeld = isCallheld;
        Rlog.d(TAG, "ImsExternalCallState = " + this);
    }

    public ImsExternalCallState(Parcel in) {
        this.mCallId = in.readInt();
        ClassLoader classLoader = ImsExternalCallState.class.getClassLoader();
        this.mAddress = (Uri) in.readParcelable(classLoader);
        this.mLocalAddress = (Uri) in.readParcelable(classLoader);
        boolean z = true;
        this.mIsPullable = in.readInt() != 0;
        this.mCallState = in.readInt();
        this.mCallType = in.readInt();
        this.mIsHeld = in.readInt() == 0 ? false : z;
        Rlog.d(TAG, "ImsExternalCallState const = " + this);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCallId);
        out.writeParcelable(this.mAddress, 0);
        out.writeParcelable(this.mLocalAddress, 0);
        out.writeInt(this.mIsPullable ? 1 : 0);
        out.writeInt(this.mCallState);
        out.writeInt(this.mCallType);
        out.writeInt(this.mIsHeld ? 1 : 0);
        Rlog.d(TAG, "ImsExternalCallState writeToParcel = " + out.toString());
    }

    public int getCallId() {
        return this.mCallId;
    }

    public Uri getAddress() {
        return this.mAddress;
    }

    public Uri getLocalAddress() {
        return this.mLocalAddress;
    }

    public boolean isCallPullable() {
        return this.mIsPullable;
    }

    public int getCallState() {
        return this.mCallState;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public boolean isCallHeld() {
        return this.mIsHeld;
    }

    public String toString() {
        return "ImsExternalCallState { mCallId = " + this.mCallId + ", mAddress = " + Log.pii(this.mAddress) + ", mLocalAddress = " + Log.pii(this.mLocalAddress) + ", mIsPullable = " + this.mIsPullable + ", mCallState = " + this.mCallState + ", mCallType = " + this.mCallType + ", mIsHeld = " + this.mIsHeld + "}";
    }

    private int getIdForString(String idString) {
        try {
            return Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            return idString.hashCode();
        }
    }
}
