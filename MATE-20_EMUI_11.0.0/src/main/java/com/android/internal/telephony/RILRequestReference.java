package com.android.internal.telephony;

import android.os.Message;
import android.os.Parcel;
import android.os.WorkSource;

public class RILRequestReference {
    RILRequest mRilRequest;

    public RILRequestReference(RILRequest request) {
        this.mRilRequest = request;
    }

    public static RILRequestReference obtain(int request, Message result) {
        return new RILRequestReference(RILRequest.obtain(request, result, null));
    }

    public static RILRequestReference obtain(int request, Message result, WorkSource ws) {
        return new RILRequestReference(RILRequest.obtain(request, result, ws));
    }

    public Parcel getParcel() {
        return Parcel.obtain();
    }

    public int getRequest() {
        return this.mRilRequest.mRequest;
    }

    public String serialString() {
        return this.mRilRequest.serialString();
    }
}
