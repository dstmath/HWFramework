package com.android.server.security.securityprofile;

public abstract class ScreenshotProtectorCallback {
    private boolean active = false;

    public abstract boolean isProtectedApp(String str);

    public abstract void notifyInfo(String str);

    public boolean isActive() {
        return this.active;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.active = activeStatus;
    }
}
