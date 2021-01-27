package com.android.server.wm;

import android.graphics.Rect;

public class WindowFramesEx {
    private WindowFrames mWindowFrames;

    public void setWindowFrames(WindowFrames windowFrames) {
        this.mWindowFrames = windowFrames;
    }

    public Rect getDisplayFrame() {
        return this.mWindowFrames.mDisplayFrame;
    }
}
