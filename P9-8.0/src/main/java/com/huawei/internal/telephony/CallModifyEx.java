package com.huawei.internal.telephony;

import com.huawei.android.util.NoExtAPIException;

public class CallModifyEx {
    public static final int E_CANCELLED = 7;
    public static final int E_SUCCESS = 0;
    public static final int E_UNUSED = 16;
    public CallDetailsEx call_details;
    public int call_index;
    public int error;

    public CallModifyEx() {
        this.call_index = 0;
        this.call_details = new CallDetailsEx();
    }

    public CallModifyEx(CallDetailsEx callDetails, int callIndex) {
        this(callDetails, callIndex, 0);
    }

    public CallModifyEx(CallDetailsEx callDetails, int callIndex, int err) {
        this.call_details = callDetails;
        this.call_index = callIndex;
        this.error = err;
    }

    public void setCallDetails(CallDetailsEx calldetails) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean error() {
        return (this.error == 16 || this.error == 0) ? false : true;
    }

    public String toString() {
        return " " + this.call_index + " " + this.call_details + " " + this.error;
    }
}
