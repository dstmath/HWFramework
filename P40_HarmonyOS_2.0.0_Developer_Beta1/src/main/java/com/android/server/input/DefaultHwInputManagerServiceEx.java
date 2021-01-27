package com.android.server.input;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import com.huawei.android.hardware.input.IHwTHPEventListener;

public class DefaultHwInputManagerServiceEx implements IHwInputManagerServiceEx {
    public DefaultHwInputManagerServiceEx(IHwInputManagerInner ims, Context context) {
    }

    public void notifyNativeEvent(int eventType, int eventValue, int keyAction, int pid, int uid) {
    }

    public void checkHasShowDismissSoftInputAlertDialog(boolean isEmpty) {
    }

    public String runHwTHPCommand(String command, String parameter) {
        return "";
    }

    public void registerListener(IHwTHPEventListener listener, IBinder binder) {
    }

    public void unregisterListener(IHwTHPEventListener listener, IBinder binder) {
    }

    public String runSideTouchCommand(String command, String parameter) {
        return "";
    }

    public int[] runSideTouchCommandByType(int type, Bundle bundle) {
        return null;
    }

    public int setTouchscreenFeatureConfig(int feature, String config) {
        return 0;
    }

    public void showSwitchedKeyboardLayoutToast(String keyboardLayoutLabel, boolean isCurrentAuto) {
    }
}
