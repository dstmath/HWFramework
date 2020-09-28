package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Telephony;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperatorInfo implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<OperatorInfo> CREATOR = new Parcelable.Creator<OperatorInfo>() {
        /* class com.android.internal.telephony.OperatorInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OperatorInfo createFromParcel(Parcel in) {
            return new OperatorInfo(in.readString(), in.readString(), in.readString(), (State) in.readSerializable());
        }

        @Override // android.os.Parcelable.Creator
        public OperatorInfo[] newArray(int size) {
            return new OperatorInfo[size];
        }
    };
    @UnsupportedAppUsage
    private String mOperatorAlphaLong;
    @UnsupportedAppUsage
    private String mOperatorAlphaShort;
    @UnsupportedAppUsage
    private String mOperatorNumeric;
    private String mRadioTech;
    @UnsupportedAppUsage
    private State mState;

    public enum State {
        UNKNOWN,
        AVAILABLE,
        CURRENT,
        FORBIDDEN
    }

    @UnsupportedAppUsage
    public String getOperatorAlphaLong() {
        return this.mOperatorAlphaLong;
    }

    @UnsupportedAppUsage
    public String getOperatorAlphaShort() {
        return this.mOperatorAlphaShort;
    }

    @UnsupportedAppUsage
    public String getOperatorNumeric() {
        return this.mOperatorNumeric + this.mRadioTech;
    }

    public String getOperatorNumericWithoutAct() {
        return this.mOperatorNumeric;
    }

    @UnsupportedAppUsage
    public State getState() {
        return this.mState;
    }

    public String getRadioTech() {
        return this.mRadioTech;
    }

    @UnsupportedAppUsage
    public OperatorInfo(String operatorAlphaLong, String operatorAlphaShort, String operatorNumeric, State state) {
        this.mState = State.UNKNOWN;
        this.mOperatorAlphaLong = operatorAlphaLong;
        this.mOperatorAlphaShort = operatorAlphaShort;
        this.mOperatorNumeric = operatorNumeric;
        this.mState = state;
        this.mRadioTech = "";
        Matcher m = Pattern.compile("^(\\d{5,6})([\\+,]\\w+)$").matcher(this.mOperatorNumeric);
        if (m.matches()) {
            this.mOperatorNumeric = m.group(1);
            this.mRadioTech = m.group(2);
        }
    }

    @UnsupportedAppUsage
    public OperatorInfo(String operatorAlphaLong, String operatorAlphaShort, String operatorNumeric, String stateString) {
        this(operatorAlphaLong, operatorAlphaShort, operatorNumeric, rilStateToState(stateString));
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public OperatorInfo(String operatorAlphaLong, String operatorAlphaShort, String operatorNumeric) {
        this(operatorAlphaLong, operatorAlphaShort, operatorNumeric, State.UNKNOWN);
    }

    @UnsupportedAppUsage
    private static State rilStateToState(String s) {
        if (s.equals("unknown")) {
            return State.UNKNOWN;
        }
        if (s.equals("available")) {
            return State.AVAILABLE;
        }
        if (s.equals(Telephony.Carriers.CURRENT)) {
            return State.CURRENT;
        }
        if (s.equals("forbidden")) {
            return State.FORBIDDEN;
        }
        throw new RuntimeException("RIL impl error: Invalid network state '" + s + "'");
    }

    public String toString() {
        return "OperatorInfo " + this.mOperatorAlphaLong + "/" + this.mOperatorAlphaShort + "/" + this.mOperatorNumeric + "/" + this.mRadioTech + "/" + this.mState;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mOperatorAlphaLong);
        dest.writeString(this.mOperatorAlphaShort);
        dest.writeString(getOperatorNumeric());
        dest.writeSerializable(this.mState);
    }
}
