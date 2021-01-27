package com.android.server.rms.iaware.appmng;

public final class AwareAppLruBase {
    public long inactiveTime;
    public int procPid;
    public int procUid;

    public AwareAppLruBase(int pid, int uid, long time) {
        this.procPid = 0;
        this.procUid = 0;
        this.inactiveTime = 0;
        this.procPid = pid;
        this.procUid = uid;
        this.inactiveTime = time;
    }

    public AwareAppLruBase() {
        this(0, 0, 0);
    }

    public void setValue(int pid, int uid, long time) {
        this.procPid = pid;
        this.procUid = uid;
        this.inactiveTime = time;
    }

    public static void copyLruBaseInfo(AwareAppLruBase src, AwareAppLruBase dst) {
        if (dst != null) {
            if (src == null) {
                dst.setInitValue();
                return;
            }
            dst.procPid = src.procPid;
            dst.procUid = src.procUid;
            dst.inactiveTime = src.inactiveTime;
        }
    }

    public void setInitValue() {
        this.procPid = 0;
        this.procUid = 0;
        this.inactiveTime = 0;
    }
}
