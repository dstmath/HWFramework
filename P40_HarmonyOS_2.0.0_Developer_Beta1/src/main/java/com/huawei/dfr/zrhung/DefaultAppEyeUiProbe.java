package com.huawei.dfr.zrhung;

import android.os.Handler;
import android.os.Message;
import android.zrhung.IAppEyeUiProbe;

public class DefaultAppEyeUiProbe implements IAppEyeUiProbe {
    public static IAppEyeUiProbe getDefault() {
        return new DefaultAppEyeUiProbe();
    }

    public void beginDispatching(Message msg, Handler target, Runnable callback) {
    }

    public void endDispatching() {
    }
}
