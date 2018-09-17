package android.hardware.camera2.impl;

public interface SetCommand {
    <T> void setValue(CameraMetadataNative cameraMetadataNative, T t);
}
