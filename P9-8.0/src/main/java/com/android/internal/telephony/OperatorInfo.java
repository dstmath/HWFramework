package com.android.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Telephony.Carriers;
import android.util.LogException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperatorInfo implements Parcelable {
    public static final Creator<OperatorInfo> CREATOR = new Creator<OperatorInfo>() {
        public OperatorInfo createFromParcel(Parcel in) {
            return new OperatorInfo(in.readString(), in.readString(), in.readString(), (State) in.readSerializable());
        }

        public OperatorInfo[] newArray(int size) {
            return new OperatorInfo[size];
        }
    };
    private String mOperatorAlphaLong;
    private String mOperatorAlphaShort;
    private String mOperatorNumeric;
    private String mRadioTech;
    private State mState;

    public enum State {
        UNKNOWN,
        AVAILABLE,
        CURRENT,
        FORBIDDEN
    }

    public String getOperatorAlphaLong() {
        return this.mOperatorAlphaLong;
    }

    public String getOperatorAlphaShort() {
        return this.mOperatorAlphaShort;
    }

    public String getOperatorNumeric() {
        return this.mOperatorNumeric + this.mRadioTech;
    }

    public String getOperatorNumericWithoutAct() {
        return this.mOperatorNumeric;
    }

    public State getState() {
        return this.mState;
    }

    public String getRadioTech() {
        return this.mRadioTech;
    }

    public OperatorInfo(String operatorAlphaLong, String operatorAlphaShort, String operatorNumeric, State state) {
        this.mState = State.UNKNOWN;
        this.mOperatorAlphaLong = operatorAlphaLong;
        this.mOperatorAlphaShort = operatorAlphaShort;
        this.mOperatorNumeric = operatorNumeric;
        this.mState = state;
        this.mRadioTech = LogException.NO_VALUE;
        Matcher m = Pattern.compile("^(\\d{5,6})([\\+,]\\w+)$").matcher(this.mOperatorNumeric);
        if (m.matches()) {
            this.mOperatorNumeric = m.group(1);
            this.mRadioTech = m.group(2);
        }
    }

    public OperatorInfo(String operatorAlphaLong, String operatorAlphaShort, String operatorNumeric, String stateString) {
        this(operatorAlphaLong, operatorAlphaShort, operatorNumeric, rilStateToState(stateString));
    }

    public OperatorInfo(String operatorAlphaLong, String operatorAlphaShort, String operatorNumeric) {
        this(operatorAlphaLong, operatorAlphaShort, operatorNumeric, State.UNKNOWN);
    }

    private static State rilStateToState(String s) {
        if (s.equals(SmsConstants.FORMAT_UNKNOWN)) {
            return State.UNKNOWN;
        }
        if (s.equals("available")) {
            return State.AVAILABLE;
        }
        if (s.equals(Carriers.CURRENT)) {
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mOperatorAlphaLong);
        dest.writeString(this.mOperatorAlphaShort);
        dest.writeString(getOperatorNumeric());
        dest.writeSerializable(this.mState);
    }
}
