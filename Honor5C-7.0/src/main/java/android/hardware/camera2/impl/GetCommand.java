package android.hardware.camera2.impl;

import android.hardware.camera2.impl.CameraMetadataNative.Key;

public interface GetCommand {
    <T> T getValue(CameraMetadataNative cameraMetadataNative, Key<T> key);
}
