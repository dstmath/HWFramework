package com.huawei.android.hardware.display;

import android.hardware.display.DisplayManagerInternal;
import android.os.Bundle;
import android.os.IBinder;
import com.android.server.LocalServices;
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
}
