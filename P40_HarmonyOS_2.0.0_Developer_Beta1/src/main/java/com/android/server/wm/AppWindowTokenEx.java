package com.android.server.wm;

public class AppWindowTokenEx {
    private AppWindowToken appWindowToken;

    public void setAppWindowToken(AppWindowToken appWindowToken2) {
        this.appWindowToken = appWindowToken2;
    }

    public void setAppWindowTokenObject(Object object) {
        if (object instanceof AppWindowToken) {
            this.appWindowToken = (AppWindowToken) object;
        }
    }

    public String getAppPackageName() {
        return this.appWindowToken.appPackageName;
    }

    public String getAppComponentName() {
        return this.appWindowToken.appComponentName;
    }

    public boolean isAppWindowNull() {
        return this.appWindowToken == null;
    }
}
