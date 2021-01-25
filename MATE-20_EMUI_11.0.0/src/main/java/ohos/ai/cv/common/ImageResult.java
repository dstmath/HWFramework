package ohos.ai.cv.common;

import ohos.media.image.PixelMap;

public class ImageResult {
    private PixelMap pixelMap;

    public ImageResult(PixelMap pixelMap2) {
        this.pixelMap = pixelMap2;
    }

    public ImageResult() {
        this(null);
    }

    public PixelMap getPixelMap() {
        return this.pixelMap;
    }

    public void setPixelMap(PixelMap pixelMap2) {
        this.pixelMap = pixelMap2;
    }
}
