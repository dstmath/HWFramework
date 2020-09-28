package com.android.internal.telephony.cat;

public class CatEventMessage {
    private byte[] mAdditionalInfo = null;
    private int mDestId = 129;
    private int mEvent = 0;
    private boolean mOneShot = false;
    private int mSourceId = 130;

    public CatEventMessage(int event, int sourceId, int destId, byte[] additionalInfo, boolean oneShot) {
        this.mEvent = event;
        this.mSourceId = sourceId;
        this.mDestId = destId;
        this.mAdditionalInfo = (byte[]) additionalInfo.clone();
        this.mOneShot = oneShot;
    }

    public CatEventMessage(int event, byte[] additionalInfo, boolean oneShot) {
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
