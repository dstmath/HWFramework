package com.android.internal.telephony;

public abstract class AbstractIccSmsInterfaceManager {
    protected byte[] getNewbyte() {
        return new byte[]{(byte) 0};
    }

    protected int getRecordLength() {
        return -1;
    }

    protected boolean isHwMmsUid(int uid) {
        return false;
    }

    public String getSmscAddr() {
        return null;
    }

    public boolean setSmscAddr(String smscAddr) {
        return false;
    }
}
