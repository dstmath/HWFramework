package com.android.server.wm;

public class DisplayRotationEx {
    private DisplayRotation mDisplayRotation;

    public DisplayRotationEx() {
    }

    public DisplayRotationEx(DisplayRotation displayRotation) {
        this.mDisplayRotation = displayRotation;
    }

    public int getUserRotationMode() {
        return this.mDisplayRotation.getUserRotationMode();
    }
}
