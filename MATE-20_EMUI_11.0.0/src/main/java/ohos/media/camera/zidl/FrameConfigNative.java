package ohos.media.camera.zidl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ohos.agp.graphics.Surface;
import ohos.media.camera.params.ParameterKey;

public class FrameConfigNative {
    private final Map<ParameterKey.Key<?>, Object> configParameters;
    private final Surface coordinateSurface;
    private final int frameConfigType;
    private final Object mark;
    private final List<Surface> surfaceList;

    public @interface FrameConfigType {
        public static final int FRAME_CONFIG_PICTURE = 2;
        public static final int FRAME_CONFIG_PREVIEW = 1;
        public static final int FRAME_CONFIG_RECORD = 3;
    }

    private FrameConfigNative(Object obj, Map<ParameterKey.Key<?>, Object> map, int i, List<Surface> list, Surface surface) {
        this.mark = obj;
        this.configParameters = map;
        this.frameConfigType = i;
        this.surfaceList = list;
        this.coordinateSurface = surface;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getFrameConfigType() {
        return this.frameConfigType;
    }

    public List<Surface> getSurfaces() {
        return this.surfaceList;
    }

    public Surface getCoordinateSurface() {
        return this.coordinateSurface;
    }

    public <T> T get(ParameterKey.Key<T> key) {
        return (T) this.configParameters.get(key);
    }

    public List<ParameterKey.Key<?>> getKeys() {
        return new ArrayList(this.configParameters.keySet());
    }

    public Map<ParameterKey.Key<?>, Object> getConfigParameters() {
        return this.configParameters;
    }

    public Object getMark() {
        return this.mark;
    }

    public static class Builder {
        private Map<ParameterKey.Key<?>, Object> configParameters;
        private Surface coordinateSurface;
        private int frameConfigType;
        private Object mark;
        private List<Surface> surfaceList;

        Builder() {
        }

        public Builder configParameters(Map<ParameterKey.Key<?>, Object> map) {
            this.configParameters = map;
            return this;
        }

        public Builder frameConfigType(int i) {
            this.frameConfigType = i;
            return this;
        }

        public Builder surfaceList(List<Surface> list) {
            this.surfaceList = list;
            return this;
        }

        public Builder coordinateSurface(Surface surface) {
            this.coordinateSurface = surface;
            return this;
        }

        public Builder mark(Object obj) {
            this.mark = obj;
            return this;
        }

        public FrameConfigNative build() {
            return new FrameConfigNative(this.mark, this.configParameters, this.frameConfigType, this.surfaceList, this.coordinateSurface);
        }

        public String toString() {
            return "Builder{configParameters=" + this.configParameters + ", frameConfigType=" + this.frameConfigType + ", surfaceList=" + this.surfaceList + ", coordinateSurface=" + this.coordinateSurface + ", mark=" + this.mark + '}';
        }
    }
}
