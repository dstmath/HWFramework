package com.android.internal.telephony.uicc.asn1;

public class TagNotFoundException extends Exception {
    private final int mTag;

    public TagNotFoundException(int tag) {
        this.mTag = tag;
    }

    public int getTag() {
        return this.mTag;
    }

    public String getMessage() {
        return super.getMessage() + " (tag=" + this.mTag + ")";
    }
}
