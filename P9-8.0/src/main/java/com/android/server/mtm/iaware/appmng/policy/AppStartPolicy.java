package com.android.server.mtm.iaware.appmng.policy;

public class AppStartPolicy extends Policy {
    public AppStartPolicy(String packageName, int policy, String reason) {
        this.mPackageName = packageName;
        this.mPolicy = policy;
        this.mReason = reason;
    }
}
