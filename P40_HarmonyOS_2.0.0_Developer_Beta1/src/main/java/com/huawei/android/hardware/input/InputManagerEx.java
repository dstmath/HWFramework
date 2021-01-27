package com.huawei.android.hardware.input;

import android.content.Context;
import android.hardware.input.InputManager;
import android.view.InputEvent;
import com.huawei.annotation.HwSystemApi;

public class InputManagerEx {
    private static final String VR_VIRTUAL_SCREEN_NAME = "com.huawei.vrvirtualscreen";

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

    public static boolean injectInputEventByDisplayId(InputEvent inputEvent, int mode, int displayId, Context context) {
        if (inputEvent == null || context == null || !VR_VIRTUAL_SCREEN_NAME.equals(context.getPackageName())) {
            return false;
        }
        return HwInputManager.injectInputEventByDisplayId(inputEvent, mode, displayId);
    }

    public static void fadeMousePointer() {
        HwInputManager.fadeMousePointer();
    }

    public static void setMousePosition(float xPosition, float yPosition) {
        HwInputManager.setMousePosition(xPosition, yPosition);
    }

    @HwSystemApi
    public static InputManager getInstance() {
        return InputManager.getInstance();
    }
}
