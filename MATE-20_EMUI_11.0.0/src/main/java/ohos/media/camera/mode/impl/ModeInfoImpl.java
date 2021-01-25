package ohos.media.camera.mode.impl;

import java.util.Arrays;
import ohos.media.camera.device.impl.CameraInfoImpl;
import ohos.media.camera.mode.Mode;

public class ModeInfoImpl extends CameraInfoImpl {
    @Mode.Type
    private int[] supportedModes;

    public ModeInfoImpl(CameraInfoImpl cameraInfoImpl) {
        super(cameraInfoImpl);
    }

    public int[] getSupportedModes() {
        return this.supportedModes;
    }

    public void setSupportedModes(int[] iArr) {
        this.supportedModes = iArr;
    }

    @Override // ohos.media.camera.device.impl.CameraInfoImpl
    public String toString() {
        return super.toString() + "ModeInfoImpl{supportedModes=" + Arrays.toString(this.supportedModes) + '}';
    }
}
