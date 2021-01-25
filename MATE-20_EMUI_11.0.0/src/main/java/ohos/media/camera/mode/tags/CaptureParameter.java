package ohos.media.camera.mode.tags;

import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.params.ParameterKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CaptureParameter<T> {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CaptureParameter.class);
    private final ParameterKey.Key<T> key;
    private final T value;

    public CaptureParameter(ParameterKey.Key<T> key2, T t) {
        this.key = key2;
        this.value = t;
    }

    public ParameterKey.Key<T> getKey() {
        return this.key;
    }

    public T getValue() {
        return this.value;
    }

    public void applyToBuilder(FrameConfig.Builder builder) {
        if (builder == null) {
            LOGGER.error("applyToBuilder: null builder!", new Object[0]);
            return;
        }
        try {
            builder.setParameter(this.key, this.value);
        } catch (IllegalArgumentException e) {
            LOGGER.error("[HAL unsupported]set parameter(%{public}s, %{public}s) IllegalArgumentException: %{public}s", this.key.getName(), this.value, e.getMessage());
        }
    }
}
