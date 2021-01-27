package android.hardware.camera2.impl;

import android.hardware.camera2.impl.CameraDeviceImpl;

public interface CameraCaptureSessionCore {
    CameraDeviceImpl.StateCallbackKK getDeviceStateCallback();

    boolean isAborting();

    void replaceSessionClose();
}
