package ohos.multimodalinput.device;

import ohos.multimodalinput.deviceadapter.InputDeviceAdapter;

public final class InputDevice {
    public static int[] getAllInputDeviceID() {
        return InputDeviceAdapter.getAllInputDeviceID();
    }

    private InputDevice() {
    }
}
