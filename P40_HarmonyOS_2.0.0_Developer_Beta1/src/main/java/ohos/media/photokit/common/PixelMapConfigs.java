package ohos.media.photokit.common;

import ohos.media.image.common.PixelFormat;

public class PixelMapConfigs {
    private PixelFormat actualConfig = PixelFormat.ARGB_8888;
    private PixelFormat preferredConfig = PixelFormat.ARGB_8888;

    public void setPreferredConfig(PixelFormat pixelFormat) {
        if (pixelFormat != null) {
            this.preferredConfig = pixelFormat;
            return;
        }
        throw new IllegalArgumentException("preferred config must not be null");
    }

    public PixelFormat getPreferredConfig() {
        return this.preferredConfig;
    }

    public PixelFormat getActualConfig() {
        return this.actualConfig;
    }
}
