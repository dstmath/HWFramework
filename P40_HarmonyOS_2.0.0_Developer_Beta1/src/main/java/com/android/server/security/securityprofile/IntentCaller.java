package com.android.server.security.securityprofile;

public class IntentCaller {
    public String packageName;
    public int pid;
    public int uid;

    public IntentCaller(String packageName2, int uid2, int pid2) {
        this.packageName = packageName2;
        this.uid = uid2;
        this.pid = pid2;
    }
}
