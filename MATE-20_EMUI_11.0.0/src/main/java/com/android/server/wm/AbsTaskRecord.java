package com.android.server.wm;

import android.content.res.Configuration;

public abstract class AbsTaskRecord extends ConfigurationContainer {
    protected int mNextWindowState;
    protected int mOriginalWindowState;
    protected int mWindowState = 4;

    public AbsTaskRecord() {
        int i = this.mWindowState;
        this.mNextWindowState = i;
        this.mOriginalWindowState = i;
    }

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
