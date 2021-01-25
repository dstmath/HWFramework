package ohos.media.camera.mode.function.focus;

import ohos.agp.utils.Rect;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.ActionStateCallback;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.function.CaptureCallbackManagerWrapper;

public class Camera3aManager {
    private final ExposureFunction exposureFunction;
    private final FocusFunction focusFunction;

    public Camera3aManager(CameraController cameraController, EventHandler eventHandler, CameraAbilityImpl cameraAbilityImpl, CaptureCallbackManagerWrapper captureCallbackManagerWrapper) {
        this.focusFunction = new FocusFunction(cameraController, eventHandler, cameraAbilityImpl, captureCallbackManagerWrapper);
        this.exposureFunction = new ExposureFunction(cameraController, eventHandler, captureCallbackManagerWrapper);
    }

    public void setCafFocus(Rect rect) {
        this.exposureFunction.setCafFocus(rect);
        this.focusFunction.setCafFocus(rect);
    }

    public void setTafFocus(boolean z, Rect rect, ActionStateCallback actionStateCallback) {
        this.exposureFunction.setTafFocus(Boolean.valueOf(z), rect);
        this.focusFunction.setTafFocus(z, rect, actionStateCallback);
    }

    public void setMfFocus() {
        this.focusFunction.setMfFocus();
    }

    public void setMfDistance(float f) {
        this.focusFunction.setMfDistance(f);
    }

    public void setMeteringMode(byte b) {
        this.exposureFunction.setMeteringMode(b);
    }

    public void setAeLock(boolean z) {
        this.exposureFunction.setAeLock(z);
    }

    public FocusFunction getFocusFunction() {
        return this.focusFunction;
    }

    public ExposureFunction getExposureFunction() {
        return this.exposureFunction;
    }
}
