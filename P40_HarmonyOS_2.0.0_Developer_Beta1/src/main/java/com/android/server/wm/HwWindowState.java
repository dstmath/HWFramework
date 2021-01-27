package com.android.server.wm;

public class HwWindowState {
    private static HwWindowState mInstance = null;

    protected HwWindowState() {
    }

    public static HwWindowState getDefault() {
        if (mInstance == null) {
            mInstance = new HwWindowState();
        }
        return mInstance;
    }
}
