package com.android.server.location;

public class CallerIdentity {
    public final String mPackageName;
    public final int mPid;
    public final int mUid;

    public CallerIdentity(int uid, int pid, String packageName) {
        this.mUid = uid;
        this.mPid = pid;
        this.mPackageName = packageName;
    }
}
