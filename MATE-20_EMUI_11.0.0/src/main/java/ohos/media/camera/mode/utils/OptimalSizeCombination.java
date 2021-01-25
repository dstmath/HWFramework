package ohos.media.camera.mode.utils;

import ohos.media.image.common.Size;

public class OptimalSizeCombination {
    private Size captureSize;
    private Size previewSize;
    private Size videoSize;

    public OptimalSizeCombination(Size size, Size size2, Size size3) {
        this.previewSize = size;
        this.captureSize = size2;
        this.videoSize = size3;
    }

    public Size getPreviewSize() {
        return this.previewSize;
    }

    public void setPreviewSize(Size size) {
        this.previewSize = size;
    }

    public Size getCaptureSize() {
        return this.captureSize;
    }

    public void setCaptureSize(Size size) {
        this.captureSize = size;
    }

    public Size getVideoSize() {
        return this.videoSize;
    }

    public void setVideoSize(Size size) {
        this.videoSize = size;
    }
}
