package com.google.android.mms.util;

import com.google.android.mms.pdu.GenericPdu;

public final class PduCacheEntry {
    private final int mMessageBox;
    private final GenericPdu mPdu;
    private final long mThreadId;

    public PduCacheEntry(GenericPdu pdu, int msgBox, long threadId) {
        this.mPdu = pdu;
        this.mMessageBox = msgBox;
        this.mThreadId = threadId;
    }

    public GenericPdu getPdu() {
        return this.mPdu;
    }

    public int getMessageBox() {
        return this.mMessageBox;
    }

    public long getThreadId() {
        return this.mThreadId;
    }
}
