package ohos.multimodalinput.deviceadapter;

import android.hardware.input.InputManager;

public final class InputDeviceAdapter {
    public static int[] getAllInputDeviceID() {
        return InputManager.getInstance().getInputDeviceIds();
    }
}
