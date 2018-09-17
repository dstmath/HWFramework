package com.android.internal.telephony.cat;

/* compiled from: CatService */
class RilMessage {
    Object mData;
    int mId;
    ResultCode mResCode;

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
