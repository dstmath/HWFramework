package com.huawei.ace.plugin.vibrator;

public abstract class VibratorPluginBase {
    /* access modifiers changed from: protected */
    public native void nativeInit();

    public abstract void vibrate(int i);
}
