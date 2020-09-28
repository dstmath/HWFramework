package com.huawei.android.view;

import android.view.InputDevice;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class InputDeviceEx {
    public static boolean isExternal(InputDevice inputDevice) {
        if (inputDevice != null) {
            return inputDevice.isExternal();
        }
        return false;
    }
}
