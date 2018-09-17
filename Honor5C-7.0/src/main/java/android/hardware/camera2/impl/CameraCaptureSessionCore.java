package android.hardware.camera2.impl;

import android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK;

public interface CameraCaptureSessionCore {
    StateCallbackKK getDeviceStateCallback();

    boolean isAborting();

    void replaceSessionClose();
}
