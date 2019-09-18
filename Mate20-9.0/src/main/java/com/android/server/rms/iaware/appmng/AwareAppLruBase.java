package com.android.server.rms.iaware.appmng;

public final class AwareAppLruBase {
    public int mPid;
    public long mTime;
    public int mUid;

    public AwareAppLruBase(int pid, int uid, long time) {
        this.mPid = 0;
        this.mUid = 0;
        this.mTime = 0;
        this.mPid = pid;
        this.mUid = uid;
        this.mTime = time;
    }

    public AwareAppLruBase() {
        this.mPid = 0;
        this.mUid = 0;
        this.mTime = 0;
        this.mPid = 0;
        this.mUid = 0;
        this.mTime = 0;
    }

    public void setValue(int pid, int uid, long time) {
        this.mPid = pid;
        this.mUid = uid;
        this.mTime = time;
    }

    public static void copyLruBaseInfo(AwareAppLruBase src, AwareAppLruBase dst) {
        if (dst != null) {
            if (src == null) {
                dst.setInitValue();
                return;
            }
            dst.mPid = src.mPid;
            dst.mUid = src.mUid;
            dst.mTime = src.mTime;
        }
    }

    public void setInitValue() {
        this.mPid = 0;
        this.mUid = 0;
        this.mTime = 0;
    }
}
