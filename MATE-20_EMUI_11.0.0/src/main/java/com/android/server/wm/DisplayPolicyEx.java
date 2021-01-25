package com.android.server.wm;

import android.view.DisplayCutout;

public class DisplayPolicyEx {
    private DisplayPolicy mDisplayPolicy;

    public void setDisplayPolicy(DisplayPolicy displayPolicy) {
        this.mDisplayPolicy = displayPolicy;
    }

    public DisplayPolicy getDisplayPolicy() {
        return this.mDisplayPolicy;
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        return this.mDisplayPolicy.getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode, displayCutout);
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        return this.mDisplayPolicy.getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayCutout);
    }

    public void resetSystemUiVisibilityLw() {
        this.mDisplayPolicy.resetSystemUiVisibilityLw();
    }
}
