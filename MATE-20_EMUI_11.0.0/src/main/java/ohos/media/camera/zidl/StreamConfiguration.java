package ohos.media.camera.zidl;

import ohos.agp.graphics.Surface;

public final class StreamConfiguration {
    private final Surface surface;

    public StreamConfiguration(Surface surface2) {
        this.surface = surface2;
    }

    public Surface getSurface() {
        return this.surface;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof StreamConfiguration) {
            return this.surface.equals(((StreamConfiguration) obj).surface);
        }
        return false;
    }

    public int hashCode() {
        return this.surface.hashCode();
    }
}
