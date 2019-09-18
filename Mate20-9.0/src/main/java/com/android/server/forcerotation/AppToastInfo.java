package com.android.server.forcerotation;

public class AppToastInfo {
    private String mPackageName;
    private int mPid;
    private String mProcessName;

    public AppToastInfo() {
    }

    public AppToastInfo(String packageName, String mProcessName2, int pid) {
        this.mPid = pid;
        this.mPackageName = packageName;
        this.mProcessName = mProcessName2;
    }

    public String getmPackageName() {
        return this.mPackageName;
    }

    public void setmPackageName(String mPackageName2) {
        this.mPackageName = mPackageName2;
    }

    public String getmProcessName() {
        return this.mProcessName;
    }

    public void setmProcessName(String mProcessName2) {
        this.mProcessName = mProcessName2;
    }

    public int getmPid() {
        return this.mPid;
    }

    public void setmPid(int mPid2) {
        this.mPid = mPid2;
    }
}
