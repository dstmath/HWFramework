package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;

/* access modifiers changed from: package-private */
/* compiled from: CatService */
public class RilMessage {
    @UnsupportedAppUsage
    Object mData;
    @UnsupportedAppUsage
    int mId;
    ResultCode mResCode;

    @UnsupportedAppUsage
    RilMessage(int msgId, String rawData) {
        this.mId = msgId;
        this.mData = rawData;
    }

    RilMessage(RilMessage other) {
        this.mId = other.mId;
        this.mData = other.mData;
        this.mResCode = other.mResCode;
    }
}
