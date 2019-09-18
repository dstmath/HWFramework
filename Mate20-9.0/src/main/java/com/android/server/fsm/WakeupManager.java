package com.android.server.fsm;

import android.content.Context;

public class WakeupManager {
    protected Context mContext;
    protected String mOpPackageName;
    protected String mReason;
    protected int mUid;

    WakeupManager(Context context) {
        this.mContext = context;
    }

    public void setFoldScreenReady() {
    }

    public void setFingerprintReady() {
    }

    public void setWakeUpInfo(int uid, String opPackageName, String reason) {
        this.mUid = uid;
        this.mOpPackageName = opPackageName;
        this.mReason = reason;
    }

    public void wakeup() {
    }
}
