package com.android.server.forcerotation;

public class AppToastInfo {
    private String mPackageName;
    private int mPid;
    private String mProcessName;

    public AppToastInfo(String packageName, String mProcessName, int pid) {
        this.mPid = pid;
        this.mPackageName = packageName;
        this.mProcessName = mProcessName;
    }

    public String getmPackageName() {
        return this.mPackageName;
    }

    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getmProcessName() {
        return this.mProcessName;
    }

    public void setmProcessName(String mProcessName) {
        this.mProcessName = mProcessName;
    }

    public int getmPid() {
        return this.mPid;
    }

    public void setmPid(int mPid) {
        this.mPid = mPid;
    }
}
