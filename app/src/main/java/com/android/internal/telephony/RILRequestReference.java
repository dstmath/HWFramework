package com.android.internal.telephony;

import android.os.Message;
import android.os.Parcel;

public class RILRequestReference {
    RILRequest mRilRequest;

    public RILRequestReference(RILRequest request) {
        this.mRilRequest = request;
    }

    public static RILRequestReference obtain(int request, Message result) {
        return new RILRequestReference(RILRequest.obtain(request, result));
    }

    public Parcel getParcel() {
        return this.mRilRequest.mParcel;
    }

    public int getRequest() {
        return this.mRilRequest.mRequest;
    }

    public String serialString() {
        return this.mRilRequest.serialString();
    }
}
