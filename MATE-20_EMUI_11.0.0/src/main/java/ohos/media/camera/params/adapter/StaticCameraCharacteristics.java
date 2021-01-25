package ohos.media.camera.params.adapter;

import android.hardware.camera2.CameraCharacteristics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class StaticCameraCharacteristics {
    private static final int DEFAULT_BUFFERED_KEY_SIZE = 20;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(StaticCameraCharacteristics.class);
    private final Map<CameraCharacteristics.Key<?>, Object> cachedKeyMap = new ConcurrentHashMap(20);
    private final CameraCharacteristics cameraCharacteristics;

    public StaticCameraCharacteristics(CameraCharacteristics cameraCharacteristics2) {
        this.cameraCharacteristics = cameraCharacteristics2;
    }

    public <T> T get(CameraCharacteristics.Key<T> key) {
        if (this.cameraCharacteristics != null) {
            return (T) getKey(key);
        }
        return null;
    }

    private <T> T getKey(CameraCharacteristics.Key<T> key) {
        T t = (T) this.cachedKeyMap.get(key);
        if (t == null) {
            try {
                t = (T) this.cameraCharacteristics.get(key);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Get key from characteristics failed: %{public}s", e.getMessage());
                t = null;
            }
            if (t != null) {
                this.cachedKeyMap.put(key, t);
            }
        }
        return t;
    }
}
