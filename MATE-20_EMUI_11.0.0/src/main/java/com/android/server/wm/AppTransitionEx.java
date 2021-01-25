package com.android.server.wm;

public class AppTransitionEx {
    private AppTransition mAppTransition;

    public AppTransition getAppTransition() {
        return this.mAppTransition;
    }

    public void setAppTransition(AppTransition appTransition) {
        this.mAppTransition = appTransition;
    }

    public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        AppTransition appTransition = this.mAppTransition;
        if (appTransition != null) {
            appTransition.overridePendingAppTransitionScaleUp(startX, startY, startWidth, startHeight);
        }
    }
}
