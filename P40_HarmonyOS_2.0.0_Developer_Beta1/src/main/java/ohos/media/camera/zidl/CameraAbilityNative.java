package ohos.media.camera.zidl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.ResultKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CameraAbilityNative {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraAbilityNative.class);
    private final Map<PropertyKey.Key<?>, Object> cachedAbilityMetadata = new ConcurrentHashMap();
    private final String cameraId;
    private final StreamConfigAbility streamConfigAbility;
    private final List<ParameterKey.Key<?>> supportedParameters = new ArrayList();
    private final List<PropertyKey.Key<?>> supportedProperties = new ArrayList();
    private final List<ResultKey.Key<?>> supportedResults = new ArrayList();

    public CameraAbilityNative(String str, StreamConfigAbility streamConfigAbility2) {
        this.cameraId = str;
        this.streamConfigAbility = streamConfigAbility2;
    }

    public void addSupportedProperties(PropertyKey.Key<?> key) {
        this.supportedProperties.add(key);
    }

    public void addSupportedParameters(ParameterKey.Key<?> key) {
        this.supportedParameters.add(key);
    }

    public void addSupportedResults(ResultKey.Key<?> key) {
        this.supportedResults.add(key);
    }

    public <T> T getPropertyValue(PropertyKey.Key<T> key) {
        return Optional.ofNullable(this.cachedAbilityMetadata.get(key)).filter(new Predicate() {
            /* class ohos.media.camera.zidl.$$Lambda$CameraAbilityNative$e75e0g6wU6gzi9XPpqfiN2S5fdQ */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return PropertyKey.Key.this.checkType(obj.getClass());
            }
        }).orElse(null);
    }

    public void setPropertyValue(PropertyKey.Key<?> key, Object obj) {
        if (!key.checkType(obj.getClass())) {
            LOGGER.error("Class type %{public}s is not correct for key: %{public}s in camera: %{public}s", obj.getClass().toGenericString(), key.toString(), this.cameraId);
        }
        this.cachedAbilityMetadata.put(key, obj);
    }

    public String getCameraId() {
        return this.cameraId;
    }

    public Map<PropertyKey.Key<?>, Object> getCachedAbilityMetadata() {
        return this.cachedAbilityMetadata;
    }

    public StreamConfigAbility getStreamConfigAbility() {
        return this.streamConfigAbility;
    }

    public List<ParameterKey.Key<?>> getSupportedParameters() {
        return this.supportedParameters;
    }

    public List<PropertyKey.Key<?>> getSupportedProperties() {
        return this.supportedProperties;
    }

    public List<ResultKey.Key<?>> getSupportedResults() {
        return this.supportedResults;
    }
}
