package com.android.internal.telephony;

public class LastCallFailCause {
    public int causeCode;
    public String vendorCause;

    public String toString() {
        return super.toString() + " causeCode: " + this.causeCode + " vendorCause: " + this.vendorCause;
    }
}
