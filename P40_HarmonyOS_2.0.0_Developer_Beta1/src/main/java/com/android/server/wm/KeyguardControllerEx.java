package com.android.server.wm;

public class KeyguardControllerEx {
    private KeyguardController mKeyguardController;

    public KeyguardController getKeyguardController() {
        return this.mKeyguardController;
    }

    public void setKeyguardController(KeyguardController keyguardController) {
        this.mKeyguardController = keyguardController;
    }

    public boolean isKeyguardLocked() {
        KeyguardController keyguardController = this.mKeyguardController;
        if (keyguardController == null) {
            return false;
        }
        return keyguardController.isKeyguardLocked();
    }
}
