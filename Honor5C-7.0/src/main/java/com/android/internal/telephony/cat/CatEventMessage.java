package com.android.internal.telephony.cat;

import com.google.android.mms.pdu.PduPart;

public class CatEventMessage {
    private byte[] mAdditionalInfo;
    private int mDestId;
    private int mEvent;
    private boolean mOneShot;
    private int mSourceId;

    public CatEventMessage(int event, int sourceId, int destId, byte[] additionalInfo, boolean oneShot) {
        this.mEvent = 0;
        this.mSourceId = PduPart.P_LEVEL;
        this.mDestId = PduPart.P_DISPOSITION_ATTACHMENT;
        this.mAdditionalInfo = null;
        this.mOneShot = false;
        this.mEvent = event;
        this.mSourceId = sourceId;
        this.mDestId = destId;
        this.mAdditionalInfo = (byte[]) additionalInfo.clone();
        this.mOneShot = oneShot;
    }

    public CatEventMessage(int event, byte[] additionalInfo, boolean oneShot) {
        this.mEvent = 0;
        this.mSourceId = PduPart.P_LEVEL;
        this.mDestId = PduPart.P_DISPOSITION_ATTACHMENT;
        this.mAdditionalInfo = null;
        this.mOneShot = false;
        this.mEvent = event;
        this.mAdditionalInfo = (byte[]) additionalInfo.clone();
        this.mOneShot = oneShot;
    }

    public int getEvent() {
        return this.mEvent;
    }

    public int getSourceId() {
        return this.mSourceId;
    }

    public int getDestId() {
        return this.mDestId;
    }

    public byte[] getAdditionalInfo() {
        return this.mAdditionalInfo;
    }

    public boolean isOneShot() {
        return this.mOneShot;
    }
}
