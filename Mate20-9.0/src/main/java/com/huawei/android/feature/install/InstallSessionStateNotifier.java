package com.huawei.android.feature.install;

public abstract class InstallSessionStateNotifier {
    protected InstallSessionState mSessionState;

    public InstallSessionStateNotifier(InstallSessionState installSessionState) {
        this.mSessionState = installSessionState;
    }

    public abstract void notifySessionState(int i);
}
