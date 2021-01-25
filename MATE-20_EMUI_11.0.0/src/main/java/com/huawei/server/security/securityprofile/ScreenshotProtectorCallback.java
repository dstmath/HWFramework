package com.huawei.server.security.securityprofile;

public abstract class ScreenshotProtectorCallback {
    private boolean mActive = false;

    public abstract boolean isProtectedApp(String str);

    public abstract void notifyInfo(String str);

    public boolean isActive() {
        return this.mActive;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.mActive = activeStatus;
    }
}
