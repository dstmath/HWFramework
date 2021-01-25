package ohos.media.camera.device.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ohos.agp.graphics.Surface;
import ohos.agp.utils.Rect;
import ohos.location.Location;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.zidl.FrameConfigNative;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class FrameConfigImpl implements FrameConfig {
    private static final float DEFAULT_ZOOM_RATIO = 1.0f;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(FrameConfigImpl.class);
    @Metadata.AeMode
    private int aeMode;
    private Rect aeRect;
    @Metadata.AeTrigger
    private int aeTrigger;
    @Metadata.AfMode
    private int afMode;
    private Rect afRect;
    @Metadata.AfTrigger
    private int afTrigger;
    private final Map<ParameterKey.Key<?>, Object> configParameters;
    private Surface coordinateSurface;
    @Metadata.FaceDetectionType
    private int faceDetectionType;
    @Metadata.FlashMode
    private int flashMode;
    @Camera.FrameConfigType
    private final int frameConfigType;
    private int imageRotation;
    private Location location;
    private Object mark;
    private final List<Surface> surfaceList;
    private float zoomValue;

    public FrameConfigImpl(@Camera.FrameConfigType int i, Map<ParameterKey.Key<?>, Object> map) {
        this.surfaceList = new ArrayList();
        this.flashMode = 1;
        this.faceDetectionType = 0;
        this.afMode = 1;
        this.afTrigger = 0;
        this.aeMode = 1;
        this.aeTrigger = 0;
        this.zoomValue = 1.0f;
        this.frameConfigType = i;
        this.configParameters = map;
    }

    public FrameConfigImpl(FrameConfigNative frameConfigNative) {
        this.surfaceList = new ArrayList();
        this.flashMode = 1;
        this.faceDetectionType = 0;
        this.afMode = 1;
        this.afTrigger = 0;
        this.aeMode = 1;
        this.aeTrigger = 0;
        this.zoomValue = 1.0f;
        this.frameConfigType = frameConfigNative.getFrameConfigType();
        this.configParameters = frameConfigNative.getConfigParameters();
        this.surfaceList.addAll(frameConfigNative.getSurfaces());
        this.coordinateSurface = frameConfigNative.getCoordinateSurface();
        this.mark = frameConfigNative.getMark();
    }

    public FrameConfigImpl(FrameConfigImpl frameConfigImpl) {
        this.surfaceList = new ArrayList();
        this.flashMode = 1;
        this.faceDetectionType = 0;
        this.afMode = 1;
        this.afTrigger = 0;
        this.aeMode = 1;
        this.aeTrigger = 0;
        this.zoomValue = 1.0f;
        this.configParameters = new HashMap();
        for (Map.Entry<ParameterKey.Key<?>, Object> entry : frameConfigImpl.configParameters.entrySet()) {
            ParameterKey.Key<?> key = entry.getKey();
            this.configParameters.put(key, key.cloneValue(entry.getValue()));
        }
        this.frameConfigType = frameConfigImpl.frameConfigType;
        this.surfaceList.addAll(frameConfigImpl.surfaceList);
        this.coordinateSurface = frameConfigImpl.coordinateSurface;
        this.flashMode = frameConfigImpl.flashMode;
        this.faceDetectionType = frameConfigImpl.faceDetectionType;
        this.afMode = frameConfigImpl.afMode;
        this.afTrigger = frameConfigImpl.afTrigger;
        Rect rect = frameConfigImpl.afRect;
        Rect rect2 = null;
        this.afRect = rect == null ? null : new Rect(rect.left, frameConfigImpl.afRect.top, frameConfigImpl.afRect.right, frameConfigImpl.afRect.bottom);
        this.zoomValue = frameConfigImpl.zoomValue;
        Location location2 = frameConfigImpl.location;
        this.location = location2 == null ? null : new Location(location2);
        this.imageRotation = frameConfigImpl.imageRotation;
        this.aeMode = frameConfigImpl.aeMode;
        this.aeTrigger = frameConfigImpl.aeTrigger;
        Rect rect3 = frameConfigImpl.aeRect;
        this.aeRect = rect3 != null ? new Rect(rect3.left, frameConfigImpl.aeRect.top, frameConfigImpl.aeRect.right, frameConfigImpl.aeRect.bottom) : rect2;
        this.mark = frameConfigImpl.mark;
    }

    @Override // ohos.media.camera.device.FrameConfig
    @Camera.FrameConfigType
    public int getFrameConfigType() {
        return this.frameConfigType;
    }

    public void addSurface(Surface surface) {
        this.surfaceList.add(surface);
    }

    public void removeSurface(Surface surface) {
        this.surfaceList.remove(surface);
    }

    @Override // ohos.media.camera.device.FrameConfig
    public List<Surface> getSurfaces() {
        return this.surfaceList;
    }

    @Override // ohos.media.camera.device.FrameConfig
    public Surface getCoordinateSurface() {
        return this.coordinateSurface;
    }

    public void setCoordinateSurface(Surface surface) {
        Objects.requireNonNull(surface, "Surface should not be null!");
        if (this.surfaceList.contains(surface)) {
            this.coordinateSurface = surface;
            return;
        }
        throw new IllegalStateException("Surface should be added to FrameConfig first!");
    }

    public void setAfMode(@Metadata.AfMode int i, Rect rect) {
        this.afMode = i;
        this.afRect = rect;
        this.configParameters.put(InnerParameterKey.AF_MODE, Integer.valueOf(this.afMode));
        this.configParameters.put(InnerParameterKey.AF_REGION, this.afRect);
    }

    @Override // ohos.media.camera.device.FrameConfig
    @Metadata.AfMode
    public int getAfMode() {
        return this.afMode;
    }

    @Override // ohos.media.camera.device.FrameConfig
    public Rect getAfRect() {
        return this.afRect;
    }

    @Override // ohos.media.camera.device.FrameConfig
    @Metadata.AfTrigger
    public int getAfTrigger() {
        return this.afTrigger;
    }

    public void setAfTrigger(@Metadata.AfTrigger int i) {
        this.afTrigger = i;
        this.configParameters.put(InnerParameterKey.AF_TRIGGER, Integer.valueOf(this.afTrigger));
    }

    public void setAeMode(@Metadata.AeMode int i, Rect rect) {
        this.aeMode = i;
        this.aeRect = rect;
        this.configParameters.put(InnerParameterKey.AE_MODE, Integer.valueOf(this.aeMode));
        this.configParameters.put(InnerParameterKey.AE_REGION, this.aeRect);
    }

    @Override // ohos.media.camera.device.FrameConfig
    @Metadata.AeMode
    public int getAeMode() {
        return this.aeMode;
    }

    @Override // ohos.media.camera.device.FrameConfig
    public Rect getAeRect() {
        return this.aeRect;
    }

    @Override // ohos.media.camera.device.FrameConfig
    public float getZoomValue() {
        return this.zoomValue;
    }

    public void setZoomValue(float f) {
        this.zoomValue = f;
        this.configParameters.put(InnerParameterKey.ZOOM_RATIO, Float.valueOf(this.zoomValue));
    }

    @Override // ohos.media.camera.device.FrameConfig
    @Metadata.FlashMode
    public int getFlashMode() {
        return this.flashMode;
    }

    public void setFlashMode(@Metadata.FlashMode int i) {
        this.flashMode = i;
        this.configParameters.put(InnerParameterKey.FLASH_MODE, Integer.valueOf(this.flashMode));
    }

    @Override // ohos.media.camera.device.FrameConfig
    @Metadata.AeTrigger
    public int getAeTrigger() {
        return this.aeTrigger;
    }

    public void setAeTrigger(@Metadata.AeTrigger int i) {
        this.aeTrigger = i;
        this.configParameters.put(InnerParameterKey.AE_TRIGGER, Integer.valueOf(this.aeTrigger));
    }

    public void setFaceDetectionType(int i, boolean z) {
        if (z) {
            this.faceDetectionType = i | this.faceDetectionType;
        } else {
            this.faceDetectionType = (~i) & this.faceDetectionType;
        }
        this.configParameters.put(InnerParameterKey.FACE_DETECTION_TYPE, Integer.valueOf(this.faceDetectionType));
    }

    @Override // ohos.media.camera.device.FrameConfig
    public int getFaceDetectionType() {
        return this.faceDetectionType;
    }

    @Override // ohos.media.camera.device.FrameConfig
    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location2) {
        this.location = location2;
        this.configParameters.put(InnerParameterKey.LOCATION, this.location);
    }

    @Override // ohos.media.camera.device.FrameConfig
    public int getImageRotation() {
        return this.imageRotation;
    }

    public void setImageRotation(int i) {
        this.imageRotation = i;
        this.configParameters.put(InnerParameterKey.IMAGE_ROTATION, Integer.valueOf(this.imageRotation));
    }

    public <T> void setConfigParameter(ParameterKey.Key<T> key, T t) {
        if (t == null) {
            LOGGER.warn("setConfigParameter null value for key: %{public}s", key.getName());
            this.configParameters.put(key, null);
        } else if (key.checkType(t.getClass())) {
            this.configParameters.put(key, t);
        } else {
            LOGGER.error("setConfigParameter failed for incompatible type, key name: %{public}s, value type: %{public}s", key.getName(), t.getClass().toString());
        }
    }

    @Override // ohos.media.camera.device.FrameConfig
    public <T> T get(ParameterKey.Key<T> key) {
        return (T) this.configParameters.get(key);
    }

    @Override // ohos.media.camera.device.FrameConfig
    public List<ParameterKey.Key<?>> getKeys() {
        return new ArrayList(this.configParameters.keySet());
    }

    @Override // ohos.media.camera.device.FrameConfig
    public Object getMark() {
        return this.mark;
    }

    public void setMark(Object obj) {
        this.mark = obj;
    }

    /* access modifiers changed from: package-private */
    public FrameConfigNative cast2Native() {
        return FrameConfigNative.builder().frameConfigType(this.frameConfigType).configParameters(this.configParameters).surfaceList(this.surfaceList).coordinateSurface(defaultCoordinateSurface()).mark(this.mark).build();
    }

    private Surface defaultCoordinateSurface() {
        Surface surface = this.coordinateSurface;
        if (surface != null) {
            return surface;
        }
        LOGGER.debug("defaultCoordinateSurface, use the first surface as the coordinate", new Object[0]);
        return this.surfaceList.get(0);
    }

    static final class Builder implements FrameConfig.Builder {
        private final FrameConfigImpl config;

        public Builder(FrameConfigImpl frameConfigImpl) {
            this.config = frameConfigImpl;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder addSurface(Surface surface) {
            this.config.addSurface(surface);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder removeSurface(Surface surface) {
            this.config.removeSurface(surface);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setCoordinateSurface(Surface surface) {
            this.config.setCoordinateSurface(surface);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setAfMode(@Metadata.AfMode int i, Rect rect) {
            this.config.setAfMode(i, rect);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setAfTrigger(@Metadata.AfTrigger int i) {
            this.config.setAfTrigger(i);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setAeMode(@Metadata.AeMode int i, Rect rect) {
            this.config.setAeMode(i, rect);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setAeTrigger(@Metadata.AeTrigger int i) {
            this.config.setAeTrigger(i);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setZoom(float f) {
            this.config.setZoomValue(f);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setFlashMode(@Metadata.FlashMode int i) {
            this.config.setFlashMode(i);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setFaceDetection(int i, boolean z) {
            this.config.setFaceDetectionType(i, z);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setLocation(Location location) {
            this.config.setLocation(location);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setImageRotation(int i) {
            this.config.setImageRotation(i);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public <T> FrameConfig.Builder setParameter(ParameterKey.Key<T> key, T t) {
            this.config.setConfigParameter(key, t);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig.Builder setMark(Object obj) {
            this.config.setMark(obj);
            return this;
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public int getFrameConfigType() {
            return this.config.getFrameConfigType();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public List<Surface> getSurfaces() {
            return this.config.getSurfaces();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public Surface getCoordinateSurface() {
            return this.config.getCoordinateSurface();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public int getAfMode() {
            return this.config.getAfMode();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public Rect getAfRect() {
            return this.config.getAfRect();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public int getAfTrigger() {
            return this.config.getAfTrigger();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public int getAeMode() {
            return this.config.getAeMode();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public Rect getAeRect() {
            return this.config.getAeRect();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public int getAeTrigger() {
            return this.config.getAeTrigger();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public float getZoomValue() {
            return this.config.getZoomValue();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public int getFlashMode() {
            return this.config.getFlashMode();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public int getFaceDetectionType() {
            return this.config.getFaceDetectionType();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public Location getLocation() {
            return this.config.getLocation();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public int getImageRotation() {
            return this.config.getImageRotation();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public <T> T get(ParameterKey.Key<T> key) {
            return (T) this.config.get(key);
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public List<ParameterKey.Key<?>> getKeys() {
            return this.config.getKeys();
        }

        @Override // ohos.media.camera.device.FrameConfig.Builder
        public FrameConfig build() {
            return new FrameConfigImpl(this.config);
        }
    }
}
