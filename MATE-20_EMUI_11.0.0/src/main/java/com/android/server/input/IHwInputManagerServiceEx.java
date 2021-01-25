package com.android.server.input;

import android.os.Bundle;
import android.os.IBinder;
import com.huawei.android.hardware.input.IHwTHPEventListener;

public interface IHwInputManagerServiceEx {
    void checkHasShowDismissSoftInputAlertDialog(boolean z);

    void notifyNativeEvent(int i, int i2, int i3, int i4, int i5);

    void registerListener(IHwTHPEventListener iHwTHPEventListener, IBinder iBinder);

    String runHwTHPCommand(String str, String str2);

    String runSideTouchCommand(String str, String str2);

    int[] setTPCommand(int i, Bundle bundle);

    int setTouchscreenFeatureConfig(int i, String str);

    void showSwitchedKeyboardLayoutToast(String str, boolean z);

    void unregisterListener(IHwTHPEventListener iHwTHPEventListener, IBinder iBinder);
}
