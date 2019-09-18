package com.android.server.mtm.iaware.appmng.policy;

public class BrFilterPolicy extends Policy {
    private int mProcessState;

    public BrFilterPolicy(String packageName, int policy, int state) {
        this.mPackageName = packageName;
        this.mPolicy = policy;
        this.mProcessState = state;
    }

    public int getPolicy() {
        return this.mPolicy;
    }

    public void setPolicy(int policy) {
        this.mPolicy = policy;
    }

    public void setStae(int state) {
        this.mProcessState = state;
    }

    public int getProcessState() {
        return this.mProcessState;
    }
}
