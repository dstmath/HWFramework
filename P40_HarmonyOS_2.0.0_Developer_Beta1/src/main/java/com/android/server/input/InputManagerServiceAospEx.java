package com.android.server.input;

import android.view.InputDevice;

public class InputManagerServiceAospEx {
    private InputManagerService mInputManagerService;

    public void setInputManagerService(InputManagerService inputManagerService) {
        this.mInputManagerService = inputManagerService;
    }

    public InputDevice[] getInputDevices() {
        return this.mInputManagerService.getInputDevices();
    }
}
