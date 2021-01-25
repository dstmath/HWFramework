package ohos.ai.cv.common;

import ohos.ai.engine.utils.HiAILog;
import ohos.media.image.PixelMap;

public class VisionImage {
    private static final String TAG = "VisionImage";
    private PixelMap pixelMap;

    private VisionImage(PixelMap pixelMap2) {
        this.pixelMap = pixelMap2;
    }

    public static VisionImage fromPixelMap(PixelMap pixelMap2) {
        return new VisionImage(pixelMap2);
    }

    public PixelMap getPixelMap() {
        if (this.pixelMap == null) {
            HiAILog.error(TAG, "pixelMap is null");
        }
        return this.pixelMap;
    }
}
