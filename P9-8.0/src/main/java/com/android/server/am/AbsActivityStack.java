package com.android.server.am;

import android.content.Intent;
import android.content.res.Configuration;

public abstract class AbsActivityStack extends ConfigurationContainer {
    protected int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        return 0;
    }

    public int getInvalidFlag(int changes, Configuration newConfig, Configuration naviConfig) {
        return 0;
    }

    protected boolean isSplitActivity(Intent intent) {
        return false;
    }

    protected void resumeCustomActivity(ActivityRecord next) {
    }

    protected void setKeepPortraitFR() {
    }
}
