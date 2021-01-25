package ohos.media.camera.mode.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.mode.ActionDataCallback;
import ohos.media.camera.mode.ActionStateCallback;
import ohos.media.camera.mode.Mode;
import ohos.media.camera.mode.ModeConfig;
import ohos.media.camera.mode.impl.ModeConfigImpl;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Pair;

public class ModeConfigImpl implements ModeConfig {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ModeConfigImpl.class);
    private final List<Pair<Size, Integer>> captureImageProfiles;
    private ActionDataCallbackImpl dataCallback;
    private EventHandler dataHandler;
    private final List<Pair<Size, Class<?>>> deferredPreviewProfiles;
    private final List<Surface> deferredPreviewSurfaces;
    private boolean isDeferredPreview;
    private boolean isSurfaceUpdated;
    private boolean isWaitForDeferredSurface;
    private final List<Surface> previewSurfaces;
    private ActionStateCallbackImpl stateCallback;
    private EventHandler stateHandler;
    private int videoFps;
    private final List<Size> videoSizes;
    private final List<Surface> videoSurfaces;

    private ModeConfigImpl() {
        this.previewSurfaces = new ArrayList();
        this.deferredPreviewProfiles = new ArrayList();
        this.deferredPreviewSurfaces = new ArrayList();
        this.captureImageProfiles = new ArrayList();
        this.videoSurfaces = new ArrayList();
        this.videoSizes = new ArrayList();
        this.isSurfaceUpdated = false;
        this.isDeferredPreview = false;
        this.isWaitForDeferredSurface = false;
        this.videoFps = 30;
    }

    private ModeConfigImpl(ModeConfigImpl modeConfigImpl) {
        this.previewSurfaces = new ArrayList();
        this.deferredPreviewProfiles = new ArrayList();
        this.deferredPreviewSurfaces = new ArrayList();
        this.captureImageProfiles = new ArrayList();
        this.videoSurfaces = new ArrayList();
        this.videoSizes = new ArrayList();
        this.isSurfaceUpdated = false;
        this.isDeferredPreview = false;
        this.isWaitForDeferredSurface = false;
        this.videoFps = 30;
        this.previewSurfaces.addAll(modeConfigImpl.previewSurfaces);
        this.deferredPreviewProfiles.addAll(modeConfigImpl.deferredPreviewProfiles);
        this.deferredPreviewSurfaces.addAll(modeConfigImpl.deferredPreviewSurfaces);
        this.captureImageProfiles.addAll(modeConfigImpl.captureImageProfiles);
        this.videoSurfaces.addAll(modeConfigImpl.videoSurfaces);
        this.dataCallback = modeConfigImpl.dataCallback;
        this.dataHandler = modeConfigImpl.dataHandler;
        this.stateCallback = modeConfigImpl.stateCallback;
        this.stateHandler = modeConfigImpl.stateHandler;
        this.isSurfaceUpdated = modeConfigImpl.isSurfaceUpdated;
        this.isDeferredPreview = modeConfigImpl.isDeferredPreview;
        this.isWaitForDeferredSurface = modeConfigImpl.isWaitForDeferredSurface;
        this.videoSizes.addAll(modeConfigImpl.videoSizes);
        this.videoFps = modeConfigImpl.videoFps;
    }

    public List<Surface> getPreviewSurfaces() {
        return this.previewSurfaces;
    }

    public List<Pair<Size, Class<?>>> getDeferredPreviewProfiles() {
        return this.deferredPreviewProfiles;
    }

    public List<Pair<Size, Integer>> getCaptureImageProfiles() {
        return this.captureImageProfiles;
    }

    public List<Surface> getVideoSurfaces() {
        return this.videoSurfaces;
    }

    public EventHandler getDataHandler() {
        return this.dataHandler;
    }

    public EventHandler getStateHandler() {
        return this.stateHandler;
    }

    public ActionDataCallbackImpl getDataCallback() {
        return this.dataCallback;
    }

    public ActionStateCallbackImpl getStateCallback() {
        return this.stateCallback;
    }

    public List<Surface> getDeferredPreviewSurfaces() {
        return this.deferredPreviewSurfaces;
    }

    public boolean isSurfaceUpdated() {
        return this.isSurfaceUpdated || this.isDeferredPreview;
    }

    public boolean isDeferredPreview() {
        return this.isDeferredPreview;
    }

    public boolean isWaitForDeferredSurface() {
        return this.isWaitForDeferredSurface;
    }

    public void finishSurfaceUpdate() {
        this.isSurfaceUpdated = false;
    }

    public void waitForDeferredSurface() {
        this.isWaitForDeferredSurface = true;
    }

    public List<Size> getVideoSizes() {
        return this.videoSizes;
    }

    public int getVideoFps() {
        return this.videoFps;
    }

    public void finishDeferredSurface() {
        this.isDeferredPreview = false;
        this.isWaitForDeferredSurface = false;
    }

    /* access modifiers changed from: package-private */
    public static final class Builder implements ModeConfig.Builder {
        private final ModeConfigImpl config = new ModeConfigImpl();
        private final Mode mode;

        public Builder(Mode mode2) {
            this.mode = mode2;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder addPreviewSurface(Surface surface) {
            Objects.requireNonNull(surface, "surface should not be null");
            checkInWaitDeferredSurface();
            if (!this.config.previewSurfaces.contains(surface)) {
                this.config.previewSurfaces.add(surface);
                this.config.isSurfaceUpdated = true;
            }
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder removePreviewSurface(Surface surface) {
            Objects.requireNonNull(surface, "surface should not be null");
            checkInWaitDeferredSurface();
            if (this.config.previewSurfaces.contains(surface)) {
                this.config.previewSurfaces.remove(surface);
                this.config.isSurfaceUpdated = true;
            } else if (this.config.deferredPreviewSurfaces.contains(surface)) {
                this.config.deferredPreviewSurfaces.remove(surface);
                this.config.isSurfaceUpdated = true;
            }
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public <T> Builder addDeferredPreviewSize(Size size, Class<T> cls) {
            if (size == null) {
                ModeConfigImpl.LOGGER.warn("addDeferredPreviewSize: surfaceSize is null", new Object[0]);
            }
            if (cls == null) {
                ModeConfigImpl.LOGGER.warn("addDeferredPreviewSize: clazz is null", new Object[0]);
            }
            Objects.requireNonNull(size, "surfaceSize should be null");
            Objects.requireNonNull(cls, "clazz should be null");
            checkInWaitDeferredSurface();
            Pair pair = new Pair(size, cls);
            for (Pair pair2 : this.config.deferredPreviewProfiles) {
                if (pair2.f.equals(size) && pair2.s.getName().equals(cls.getName())) {
                    return this;
                }
            }
            this.config.deferredPreviewProfiles.add(pair);
            this.config.isDeferredPreview = true;
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder addDeferredPreviewSurface(Surface surface) {
            Objects.requireNonNull(surface, "surface should not be null");
            if (this.config.isWaitForDeferredSurface) {
                if (!this.config.previewSurfaces.contains(surface) && !this.config.deferredPreviewSurfaces.contains(surface)) {
                    this.config.deferredPreviewSurfaces.add(surface);
                    this.config.isSurfaceUpdated = true;
                }
                return this;
            }
            throw new IllegalStateException("Deferred preview config is not ready!");
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder addCaptureImage(Size size, int i) {
            Objects.requireNonNull(size, "size should be null");
            checkInWaitDeferredSurface();
            Pair pair = new Pair(size, Integer.valueOf(i));
            for (Pair pair2 : this.config.captureImageProfiles) {
                if (pair2.f.equals(size) && pair2.s.intValue() == i) {
                    return this;
                }
            }
            this.config.captureImageProfiles.add(pair);
            this.config.isSurfaceUpdated = true;
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder removeCaptureImage(Size size, int i) {
            Objects.requireNonNull(size, "size should be null");
            checkInWaitDeferredSurface();
            this.config.captureImageProfiles.removeIf(new Predicate(size, i) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ModeConfigImpl$Builder$NRXiYs3FRerSn6H9spCI4dNwcyE */
                private final /* synthetic */ Size f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ModeConfigImpl.Builder.this.lambda$removeCaptureImage$0$ModeConfigImpl$Builder(this.f$1, this.f$2, (Pair) obj);
                }
            });
            return this;
        }

        public /* synthetic */ boolean lambda$removeCaptureImage$0$ModeConfigImpl$Builder(Size size, int i, Pair pair) {
            return (pair.f.equals(size) && pair.s.intValue() == i) || this.config.isSurfaceUpdated;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder addVideoSurface(Surface surface) {
            Objects.requireNonNull(surface, "surface should not be null");
            checkInWaitDeferredSurface();
            if (!this.config.videoSurfaces.contains(surface)) {
                this.config.videoSurfaces.add(surface);
                this.config.isSurfaceUpdated = true;
            }
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder removeVideoSurface(Surface surface) {
            Objects.requireNonNull(surface, "surface should not be null");
            checkInWaitDeferredSurface();
            if (this.config.videoSurfaces.contains(surface)) {
                this.config.videoSurfaces.remove(surface);
                this.config.isSurfaceUpdated = true;
            }
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder addVideoSize(Size size) {
            Objects.requireNonNull(size, "size should not be null");
            if (!this.config.videoSizes.contains(size)) {
                this.config.videoSizes.add(size);
            }
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder removeVideoSize(Size size) {
            Objects.requireNonNull(size, "size should not be null");
            this.config.videoSizes.remove(size);
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder setVideoFps(int i) {
            this.config.videoFps = i;
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder setDataCallback(ActionDataCallback actionDataCallback, EventHandler eventHandler) {
            if (actionDataCallback == null) {
                this.config.dataCallback = null;
                this.config.dataHandler = null;
            } else {
                Objects.requireNonNull(eventHandler, "handler for action data callback should not be null!");
                this.config.dataCallback = ActionDataCallbackImpl.obtain(this.mode, actionDataCallback, eventHandler);
                this.config.dataHandler = eventHandler;
            }
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public Builder setStateCallback(ActionStateCallback actionStateCallback, EventHandler eventHandler) {
            if (actionStateCallback == null) {
                this.config.stateCallback = null;
                this.config.stateHandler = null;
            } else {
                Objects.requireNonNull(eventHandler, "handler for action state callback should not be null!");
                this.config.stateCallback = ActionStateCallbackImpl.obtain(this.mode, actionStateCallback, eventHandler);
                this.config.stateHandler = eventHandler;
            }
            return this;
        }

        @Override // ohos.media.camera.mode.ModeConfig.Builder
        public ModeConfig build() {
            return new ModeConfigImpl();
        }

        private void checkInWaitDeferredSurface() {
            if (this.config.isWaitForDeferredSurface) {
                throw new IllegalStateException("Please finish deferred surface f!");
            }
        }
    }
}
