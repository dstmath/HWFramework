package com.huawei.android.view;

import android.view.InputEvent;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class InputEventEx {
    public static void setDisplayId(InputEvent inputEvent, int displayId) {
        if (inputEvent != null) {
            inputEvent.setDisplayId(displayId);
        }
    }
}
