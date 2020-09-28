package com.huawei.android.hardware.display;

import android.hardware.display.DisplayManagerInternal;
import com.android.server.LocalServices;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DisplayManagerInternalEx {
    private static DisplayManagerInternal sDisplayManagerInternal = ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class));

    public static void pcDisplayChange(boolean connected) {
        sDisplayManagerInternal.pcDisplayChange(connected);
    }
}
