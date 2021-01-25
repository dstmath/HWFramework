package ohos.media.camera.mode.utils;

import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraStateCallback;

public class CameraDeviceEx {
    public static final int CAMERA_DEVICE_USED_STRONG = 0;
    public static final int CAMERA_DEVICE_USED_WEAK = 1;
    private Camera device;
    private CameraStateCallback stateCallback;
    private int usedState;

    public CameraDeviceEx() {
        this.usedState = 0;
    }

    public CameraDeviceEx(Camera camera, int i) {
        this.device = camera;
        this.usedState = i;
    }

    public int getUsedState() {
        return this.usedState;
    }

    public void setUsedState(int i) {
        this.usedState = i;
    }

    public Camera getDevice() {
        return this.device;
    }

    public void setDevice(Camera camera) {
        this.device = camera;
    }

    public CameraStateCallback getStateCallback() {
        return this.stateCallback;
    }

    public void setStateCallback(CameraStateCallback cameraStateCallback) {
        this.stateCallback = cameraStateCallback;
    }
}
