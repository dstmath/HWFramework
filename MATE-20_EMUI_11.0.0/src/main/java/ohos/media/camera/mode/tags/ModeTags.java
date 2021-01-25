package ohos.media.camera.mode.tags;

import java.util.List;
import ohos.location.Location;
import ohos.media.image.common.Size;

public interface ModeTags {
    List<CaptureParameters> enableCapture();

    void enableForegroundProcess();

    CaptureParameters enablePreview();

    List<Size> getOutputSize(int i);

    boolean isBeautyEnabled();

    int setBeauty(int i, int i2);

    int setColorMode(int i);

    int setFlashMode(int i);

    int setLocation(Location location);

    void setSceneDetection(boolean z);

    int setSensorHdr(boolean z);

    void setSmileDetection(boolean z);

    void setVideoSize(Size size);

    default int setVideoStabilization(boolean z) {
        return 0;
    }

    int setWaterMarkEnabled(boolean z);

    int setZoom(float f);
}
