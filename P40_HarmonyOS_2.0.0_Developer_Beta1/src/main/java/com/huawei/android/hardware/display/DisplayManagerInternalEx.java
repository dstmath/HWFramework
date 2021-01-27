package com.huawei.android.hardware.display;

import android.hardware.display.DisplayManagerInternal;
import android.os.Bundle;
import android.os.IBinder;
import com.android.server.LocalServices;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DisplayManagerInternalEx {
    private static DisplayManagerInternal sDisplayManagerInternal = ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class));
    private DisplayManagerInternal mDisplayManagerInternal;

    private DisplayManagerInternalEx() {
        this.mDisplayManagerInternal = null;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
    }

    public static void pcDisplayChange(boolean connected) {
        sDisplayManagerInternal.pcDisplayChange(connected);
    }

    public static DisplayManagerInternalEx getInstance() {
        return new DisplayManagerInternalEx();
    }

    public boolean setHwBrightnessData(String name, Bundle data, int[] result) {
        return this.mDisplayManagerInternal.setHwBrightnessData(name, data, result);
    }

    public boolean getHwBrightnessData(String name, Bundle data, int[] result) {
        return this.mDisplayManagerInternal.getHwBrightnessData(name, data, result);
    }

    public IBinder getDisplayToken(int displayId) {
        return this.mDisplayManagerInternal.getDisplayToken(displayId);
    }

    public int setDisplayMode(int mode, int state, boolean isWakeup) {
        return this.mDisplayManagerInternal.setDisplayMode(mode, state, isWakeup);
    }

    public void startDawnAnimation() {
        this.mDisplayManagerInternal.startDawnAnimation();
    }

    public boolean registerScreenOnUnBlockerCallback(HwFoldScreenManagerInternal.ScreenOnUnblockerCallback callback) {
        return this.mDisplayManagerInternal.registerScreenOnUnBlockerCallback(callback);
    }
}
