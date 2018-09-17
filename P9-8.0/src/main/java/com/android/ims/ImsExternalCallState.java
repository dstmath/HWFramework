package com.android.ims;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telecom.Log;
import android.telephony.Rlog;

public class ImsExternalCallState implements Parcelable {
    public static final int CALL_STATE_CONFIRMED = 1;
    public static final int CALL_STATE_TERMINATED = 2;
    public static final Creator<ImsExternalCallState> CREATOR = new Creator<ImsExternalCallState>() {
        public ImsExternalCallState createFromParcel(Parcel in) {
            return new ImsExternalCallState(in);
        }

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

    public ImsExternalCallState(int callId, Uri address, boolean isPullable, int callState, int callType, boolean isCallheld) {
        this.mCallId = callId;
        this.mAddress = address;
        this.mIsPullable = isPullable;
        this.mCallState = callState;
        this.mCallType = callType;
        this.mIsHeld = isCallheld;
        Rlog.d(TAG, "ImsExternalCallState = " + this);
    }

    public ImsExternalCallState(Parcel in) {
        boolean z = true;
        this.mCallId = in.readInt();
        this.mAddress = (Uri) in.readParcelable(ImsExternalCallState.class.getClassLoader());
        this.mIsPullable = in.readInt() != 0;
        this.mCallState = in.readInt();
        this.mCallType = in.readInt();
        if (in.readInt() == 0) {
            z = false;
        }
        this.mIsHeld = z;
        Rlog.d(TAG, "ImsExternalCallState const = " + this);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 1;
        out.writeInt(this.mCallId);
        out.writeParcelable(this.mAddress, 0);
        out.writeInt(this.mIsPullable ? 1 : 0);
        out.writeInt(this.mCallState);
        out.writeInt(this.mCallType);
        if (!this.mIsHeld) {
            i = 0;
        }
        out.writeInt(i);
        Rlog.d(TAG, "ImsExternalCallState writeToParcel = " + out.toString());
    }

    public int getCallId() {
        return this.mCallId;
    }

    public Uri getAddress() {
        return this.mAddress;
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
        return "ImsExternalCallState { mCallId = " + this.mCallId + ", mAddress = " + Log.pii(this.mAddress) + ", mIsPullable = " + this.mIsPullable + ", mCallState = " + this.mCallState + ", mCallType = " + this.mCallType + ", mIsHeld = " + this.mIsHeld + "}";
    }
}
