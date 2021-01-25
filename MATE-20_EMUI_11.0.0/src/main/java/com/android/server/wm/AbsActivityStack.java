package com.android.server.wm;

import android.content.Intent;
import android.content.res.Configuration;

public abstract class AbsActivityStack extends ConfigurationContainer {
    public int getInvalidFlag(int changes, Configuration newConfig, Configuration naviConfig) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean isSplitActivity(Intent intent) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void resumeCustomActivity(ActivityRecord next) {
    }

    /* access modifiers changed from: protected */
    public void setKeepPortraitFR() {
    }

    public void makeStackVisible(boolean isVisible) {
    }
}
