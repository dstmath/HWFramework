package com.android.server.security.trustcircle.task;

public class HwSecurityEvent {
    private int mEventID;

    public HwSecurityEvent(int evID) {
        this.mEventID = evID;
    }

    public int getEvID() {
        return this.mEventID;
    }
}
