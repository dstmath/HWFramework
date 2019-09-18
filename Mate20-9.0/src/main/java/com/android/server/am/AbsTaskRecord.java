package com.android.server.am;

import android.content.res.Configuration;
import com.android.server.wm.ConfigurationContainer;

public abstract class AbsTaskRecord extends ConfigurationContainer {
    protected int mNextWindowState = this.mWindowState;
    protected int mOriginalWindowState = this.mWindowState;
    protected int mWindowState = 4;

    public void overrideConfigOrienForFreeForm(Configuration config) {
    }

    public int getWindowState() {
        return this.mWindowState;
    }

    /* access modifiers changed from: protected */
    public boolean isMaximizedPortraitAppOnPCMode(String packageName) {
        return false;
    }
}
