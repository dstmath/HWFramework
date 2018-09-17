package com.huawei.android.hardware.input;

import android.hardware.input.InputManager;
import android.view.InputEvent;

public class InputManagerEx {
    public static boolean injectInputEvent(InputManager inputManager, InputEvent inputEvent, int mode) {
        return inputManager.injectInputEvent(inputEvent, mode);
    }

    public static int getInjectInputEventModeAsync() {
        return 0;
    }

    public static int getInjectInputEventModeWaitForResult() {
        return 1;
    }

    public static int getInjectInputEventModeWaitForFinish() {
        return 2;
    }
}
