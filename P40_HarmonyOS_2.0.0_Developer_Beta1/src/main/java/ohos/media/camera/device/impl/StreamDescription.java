package ohos.media.camera.device.impl;

import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.media.camera.device.adapter.utils.SurfaceUtils;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class StreamDescription {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(StreamDescription.class);
    private static final int SURFACE_TYPE_SURFACE_HOLDER = 0;
    private static final int SURFACE_TYPE_UNKNOWN = -1;
    private final boolean isDeferred;
    private final Size size;
    private volatile Surface surface;
    private final int surfaceType;

    public StreamDescription(Surface surface2) {
        this.surface = surface2;
        Size surfaceSize = SurfaceUtils.getSurfaceSize(surface2);
        this.size = new Size(surfaceSize.width, surfaceSize.height);
        this.isDeferred = false;
        this.surfaceType = -1;
    }

    public <T> StreamDescription(Size size2, Class<T> cls) {
        this.surface = null;
        this.size = new Size(size2.width, size2.height);
        this.isDeferred = true;
        if (cls == SurfaceOps.class) {
            this.surfaceType = 0;
        } else {
            this.surfaceType = -1;
            throw new IllegalArgumentException("Unsupported surface type!");
        }
    }

    public void attachDeferredSurface(Surface surface2) {
        if (this.isDeferred) {
            if (this.surface != null) {
                LOGGER.warn("Surface has already been attached, replace with new one!", new Object[0]);
            }
            Size surfaceSize = SurfaceUtils.getSurfaceSize(surface2);
            if (this.size.equals(surfaceSize)) {
                this.surface = surface2;
                LOGGER.info("Attach Surface success!", new Object[0]);
                return;
            }
            throw new IllegalArgumentException("Surface size does not compatible! desired size:" + this.size + " actual size:" + surfaceSize);
        }
        LOGGER.error("Cannot attach surface to a normal stream!", new Object[0]);
        throw new IllegalArgumentException("Cannot attach surface to a normal stream!");
    }

    public Surface getSurface() {
        return this.surface;
    }

    public boolean isDeferred() {
        return this.isDeferred;
    }

    public Size getStreamSize() {
        return this.size;
    }

    public int getSurfaceType() {
        return this.surfaceType;
    }
}
