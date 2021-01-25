package ohos.media.camera.device.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.CameraConfig;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.image.common.Size;

public class CameraConfigImpl implements CameraConfig {
    private FrameStateCallback frameStateCallback;
    private EventHandler handler;
    private final List<Surface> surfaceList = new ArrayList();

    public CameraConfigImpl() {
    }

    public CameraConfigImpl(CameraConfigImpl cameraConfigImpl) {
        this.surfaceList.addAll(cameraConfigImpl.surfaceList);
        this.frameStateCallback = cameraConfigImpl.frameStateCallback;
        this.handler = cameraConfigImpl.handler;
    }

    public void addSurface(Surface surface) {
        Objects.requireNonNull(surface, "surface should not be null!");
        this.surfaceList.add(surface);
    }

    public void removeSurface(Surface surface) {
        Objects.requireNonNull(surface, "surface should not be null!");
        this.surfaceList.remove(surface);
    }

    public List<Surface> getSurfaceList() {
        return this.surfaceList;
    }

    public void setFrameStateCallback(FrameStateCallback frameStateCallback2, EventHandler eventHandler) {
        if (frameStateCallback2 == null) {
            this.frameStateCallback = null;
            this.handler = null;
            return;
        }
        Objects.requireNonNull(eventHandler, "The handler for frame state callback should not be null!");
        this.frameStateCallback = frameStateCallback2;
        this.handler = eventHandler;
    }

    /* access modifiers changed from: package-private */
    public FrameStateCallback getFrameStateCallback() {
        return this.frameStateCallback;
    }

    /* access modifiers changed from: package-private */
    public EventHandler getFrameStateHandler() {
        return this.handler;
    }

    static final class Builder implements CameraConfig.Builder {
        private final CameraConfigImpl config;

        @Override // ohos.media.camera.device.CameraConfig.Builder
        public CameraConfig.Builder addDeferredSurface(Surface surface) {
            return this;
        }

        @Override // ohos.media.camera.device.CameraConfig.Builder
        public <T> CameraConfig.Builder addDeferredSurfaceSize(Size size, Class<T> cls) {
            return this;
        }

        public Builder(CameraConfigImpl cameraConfigImpl) {
            this.config = cameraConfigImpl;
        }

        @Override // ohos.media.camera.device.CameraConfig.Builder
        public CameraConfig.Builder addSurface(Surface surface) {
            this.config.addSurface(surface);
            return this;
        }

        @Override // ohos.media.camera.device.CameraConfig.Builder
        public CameraConfig.Builder removeSurface(Surface surface) {
            this.config.removeSurface(surface);
            return this;
        }

        @Override // ohos.media.camera.device.CameraConfig.Builder
        public CameraConfig.Builder setFrameStateCallback(FrameStateCallback frameStateCallback, EventHandler eventHandler) {
            this.config.setFrameStateCallback(frameStateCallback, eventHandler);
            return this;
        }

        @Override // ohos.media.camera.device.CameraConfig.Builder
        public CameraConfig build() {
            return new CameraConfigImpl(this.config);
        }
    }
}
