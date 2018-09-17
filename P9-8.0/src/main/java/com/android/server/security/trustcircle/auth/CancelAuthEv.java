package com.android.server.security.trustcircle.auth;

import com.android.server.security.trustcircle.task.HwSecurityEvent;

public class CancelAuthEv extends HwSecurityEvent {
    private long mAuthID;

    public CancelAuthEv(int evID, long authID) {
        super(evID);
        this.mAuthID = authID;
    }

    long getAuthID() {
        return this.mAuthID;
    }
}
