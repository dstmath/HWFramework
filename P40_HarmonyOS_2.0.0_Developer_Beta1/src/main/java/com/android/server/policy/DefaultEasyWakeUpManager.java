package com.android.server.policy;

import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import com.huawei.server.policy.keyguard.KeyguardServiceDelegateEx;
import huawei.android.app.IEasyWakeUpManager;

public class DefaultEasyWakeUpManager extends IEasyWakeUpManager.Stub {
    public DefaultEasyWakeUpManager(Context context, Handler handler, KeyguardServiceDelegateEx keyguardDelegate) {
    }

    public static DefaultEasyWakeUpManager getInstance(Context context, Handler handler, KeyguardServiceDelegateEx keyguardDelegate) {
        return new DefaultEasyWakeUpManager(context, handler, keyguardDelegate);
    }

    public void saveTouchPointNodePath() {
    }

    public boolean handleWakeUpKey(KeyEvent event, int screenOffReason) {
        return false;
    }

    public void turnOffSensorListener() {
    }

    public void turnOnSensorListener() {
    }

    public boolean setEasyWakeUpFlag(int flag) {
        return false;
    }
}
