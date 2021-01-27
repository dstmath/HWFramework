package ohos.media.camera.zidl;

import java.util.Objects;
import ohos.agp.graphics.Surface;
import ohos.media.camera.device.adapter.utils.SurfaceUtils;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class StreamConfiguration {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(StreamConfiguration.class);
    public static final int SURFACE_TYPE_SURFACE_HOLDER = 0;
    private volatile boolean isDeferred;
    private volatile Surface surface;
    private final Size surfaceSize;
    private final int surfaceType;

    public StreamConfiguration(Surface surface2, boolean z, Size size, int i) {
        this.surface = surface2;
        this.isDeferred = z;
        this.surfaceSize = new Size(size.width, size.height);
        this.surfaceType = i;
    }

    public boolean isDeferred() {
        return this.isDeferred;
    }

    public Size getSurfaceSize() {
        return this.surfaceSize;
    }

    public int getSurfaceType() {
        return this.surfaceType;
    }

    public Surface getSurface() {
        return this.surface;
    }

    public void attachSurface(Surface surface2) {
        Objects.requireNonNull(surface2);
        if (this.isDeferred) {
            if (this.surfaceSize.equals(SurfaceUtils.getSurfaceSize(surface2))) {
                this.surface = surface2;
                return;
            }
            throw new IllegalArgumentException("Surface size is not compatible with previously configure!");
        }
        throw new IllegalStateException("Cannot attach surface to non deferred surface!");
    }

    public void updateStatus() {
        this.isDeferred = false;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StreamConfiguration)) {
            return false;
        }
        StreamConfiguration streamConfiguration = (StreamConfiguration) obj;
        if (!this.isDeferred) {
            return this.isDeferred == streamConfiguration.isDeferred && this.surface.equals(streamConfiguration.surface);
        }
        if (this.isDeferred == streamConfiguration.isDeferred && this.surfaceType == streamConfiguration.surfaceType && this.surfaceSize.equals(streamConfiguration.surfaceSize)) {
            return this.surface == null || streamConfiguration.surface == null || this.surface.equals(streamConfiguration.surface);
        }
        return false;
    }

    public int hashCode() {
        if (this.isDeferred) {
            return Objects.hash(this.surfaceSize, Integer.valueOf(this.surfaceType));
        }
        return Objects.hash(this.surface);
    }

    public String toString() {
        return "StreamConfiguration{surface=" + this.surface + ", isDeferred=" + this.isDeferred + ", surfaceSize=" + this.surfaceSize + ", surfaceType=" + this.surfaceType + '}';
    }
}
