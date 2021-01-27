package com.android.server.accessibility;

public class MagnificationControllerEx {
    private MagnificationController mMagnificationController = null;

    public void setMagnificationController(MagnificationController magnificationController) {
        this.mMagnificationController = magnificationController;
    }

    public MagnificationController getMagnificationController() {
        return this.mMagnificationController;
    }
}
