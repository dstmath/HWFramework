package com.android.server.wm;

public class SessionAospEx {
    private Session mSession;

    public void setSession(Session session) {
        this.mSession = session;
    }

    public int getPid() {
        return this.mSession.mPid;
    }
}
