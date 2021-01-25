package ohos.media.camera.mode.impl;

import ohos.media.camera.mode.TakePictureResult;

public final class TakePictureResultImpl implements TakePictureResult {
    private final int exposureTime;
    private final int state;

    public TakePictureResultImpl(int i, int i2) {
        this.state = i;
        this.exposureTime = i2;
    }

    @Override // ohos.media.camera.mode.TakePictureResult
    public int getState() {
        return this.state;
    }

    @Override // ohos.media.camera.mode.TakePictureResult
    public int getExposureTime() {
        return this.exposureTime;
    }
}
