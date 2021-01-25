package ohos.media.camera.device.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import ohos.media.camera.device.CameraAbility;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.ResultKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.camera.zidl.CameraAbilityNative;
import ohos.media.camera.zidl.StreamConfigAbility;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CameraAbilityImpl implements CameraAbility {
    private static final float DEFAULT_ZOOM_RATIO = 1.0f;
    private static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraAbilityImpl.class);
    private static final Map<ParameterKey.Key<?>, PropertyKey.Key<?>> PARAMETER_TO_PROPERTY;
    private final Map<PropertyKey.Key<?>, Object> cachedAbilityMetadata;
    private final String cameraId;
    private final StreamConfigAbility streamConfigAbility;
    private final List<ParameterKey.Key<?>> supportedParameters;
    private final List<PropertyKey.Key<?>> supportedProperties;
    private final List<ResultKey.Key<?>> supportedResults;

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(ParameterKey.VIDEO_STABILIZATION, PropertyKey.VIDEO_STABILIZATION_SUPPORT);
        hashMap.put(ParameterKey.IMAGE_MIRROR, InnerPropertyKey.MIRROR_FUNCTION);
        PARAMETER_TO_PROPERTY = Collections.unmodifiableMap(hashMap);
    }

    public CameraAbilityImpl(String str, CameraAbilityNative cameraAbilityNative) {
        this.cameraId = str;
        this.streamConfigAbility = cameraAbilityNative.getStreamConfigAbility();
        this.cachedAbilityMetadata = cameraAbilityNative.getCachedAbilityMetadata();
        this.supportedParameters = cameraAbilityNative.getSupportedParameters();
        this.supportedProperties = cameraAbilityNative.getSupportedProperties();
        this.supportedResults = cameraAbilityNative.getSupportedResults();
    }

    public String getCameraId() {
        return this.cameraId;
    }

    @Override // ohos.media.camera.device.CameraAbility
    public <T> T getPropertyValue(PropertyKey.Key<T> key) {
        Objects.requireNonNull(key, "key should not be null!");
        return Optional.ofNullable(this.cachedAbilityMetadata.get(key)).filter(new Predicate() {
            /* class ohos.media.camera.device.impl.$$Lambda$CameraAbilityImpl$JaAKhM2Ob4PSx69soyZg6FI3D0s */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return PropertyKey.Key.this.checkType(obj.getClass());
            }
        }).orElse(null);
    }

    @Override // ohos.media.camera.device.CameraAbility
    public List<Size> getSupportedSizes(int i) {
        List<Size> outputSizes = this.streamConfigAbility.getOutputSizes(i);
        if (outputSizes.isEmpty()) {
            LOGGER.warn("No output sizes for format %{public}d, camera %{public}s", Integer.valueOf(i), this.cameraId);
        }
        return outputSizes;
    }

    public int[] getOutputSizesFormats() {
        return this.streamConfigAbility.getOutputSizesFormats();
    }

    @Override // ohos.media.camera.device.CameraAbility
    public <T> List<Size> getSupportedSizes(Class<T> cls) {
        Objects.requireNonNull(cls, "clazz must not be null");
        List<Size> outputSizes = this.streamConfigAbility.getOutputSizes(cls);
        if (outputSizes.isEmpty()) {
            LOGGER.warn("No output sizes for class %{public}s, camera %{public}s", cls.toGenericString(), this.cameraId);
        }
        return outputSizes;
    }

    @Override // ohos.media.camera.device.CameraAbility
    public int[] getSupportedAfMode() {
        Integer[] numArr = (Integer[]) this.cachedAbilityMetadata.get(InnerPropertyKey.AF_MODE);
        if (numArr == null) {
            return EMPTY_INT_ARRAY;
        }
        return Arrays.stream(numArr).mapToInt($$Lambda$CameraAbilityImpl$Sf5VNPsRQDSJNsot5hcuG4Vf4.INSTANCE).toArray();
    }

    @Override // ohos.media.camera.device.CameraAbility
    public int[] getSupportedAeMode() {
        Integer[] numArr = (Integer[]) this.cachedAbilityMetadata.get(InnerPropertyKey.AE_MODE);
        if (numArr == null) {
            return EMPTY_INT_ARRAY;
        }
        return Arrays.stream(numArr).mapToInt($$Lambda$CameraAbilityImpl$BHxZkVwhvp4LIHHC_3jOGRzrgQk.INSTANCE).toArray();
    }

    @Override // ohos.media.camera.device.CameraAbility
    public float[] getSupportedZoom() {
        Float f = (Float) this.cachedAbilityMetadata.get(InnerPropertyKey.ZOOM_RATIO);
        return f == null ? EMPTY_FLOAT_ARRAY : new float[]{1.0f, f.floatValue()};
    }

    @Override // ohos.media.camera.device.CameraAbility
    public int[] getSupportedFlashMode() {
        Integer[] numArr = (Integer[]) this.cachedAbilityMetadata.get(InnerPropertyKey.FLASH_MODE);
        if (numArr == null) {
            return EMPTY_INT_ARRAY;
        }
        return Arrays.stream(numArr).mapToInt($$Lambda$CameraAbilityImpl$I259c9ZzOJVXxY5Vdq0HGJ6yU.INSTANCE).toArray();
    }

    @Override // ohos.media.camera.device.CameraAbility
    public int[] getSupportedFaceDetection() {
        Integer[] numArr = (Integer[]) this.cachedAbilityMetadata.get(InnerPropertyKey.FACE_DETECT_MODE);
        if (numArr == null) {
            return EMPTY_INT_ARRAY;
        }
        return Arrays.stream(numArr).mapToInt($$Lambda$CameraAbilityImpl$FHPshK9cfidCx2ZrXk9PCI1OtzQ.INSTANCE).toArray();
    }

    public <T> boolean isSurfaceClassSupported(Class<T> cls) {
        Objects.requireNonNull(cls, "clazz should not be null!");
        return this.streamConfigAbility.isSurfaceClassSupported(cls);
    }

    public boolean isFormatSupported(int i) {
        return this.streamConfigAbility.isFormatSupported(i);
    }

    @Override // ohos.media.camera.device.CameraAbility
    public List<PropertyKey.Key<?>> getSupportedProperties() {
        return Collections.unmodifiableList(this.supportedProperties);
    }

    @Override // ohos.media.camera.device.CameraAbility
    public List<ParameterKey.Key<?>> getSupportedParameters() {
        return Collections.unmodifiableList(this.supportedParameters);
    }

    @Override // ohos.media.camera.device.CameraAbility
    public <T> List<T> getParameterRange(ParameterKey.Key<T> key) {
        Optional<PropertyKeyWrapper> propertyRangeKey = getPropertyRangeKey(key);
        int i = 0;
        if (!propertyRangeKey.isPresent()) {
            LOGGER.warn("Cannot find range key for %{public}s in camera %{public}s", key.toString(), this.cameraId);
            return Collections.emptyList();
        }
        PropertyKeyWrapper propertyKeyWrapper = propertyRangeKey.get();
        if (propertyKeyWrapper.isBool) {
            Boolean bool = (Boolean) getPropertyValue(propertyKeyWrapper.key);
            if (bool == null || !bool.booleanValue()) {
                return Collections.singletonList(Boolean.FALSE);
            }
            return Arrays.asList(Boolean.TRUE, Boolean.FALSE);
        }
        Object propertyValue = getPropertyValue(propertyKeyWrapper.key);
        if (propertyValue == null) {
            LOGGER.warn("getPropertyValue returns null for key %{public}s", propertyKeyWrapper.key);
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        if (propertyValue instanceof byte[]) {
            byte[] bArr = (byte[]) propertyValue;
            int length = bArr.length;
            while (i < length) {
                arrayList.add(Byte.valueOf(bArr[i]));
                i++;
            }
        } else if (propertyValue instanceof int[]) {
            int[] iArr = (int[]) propertyValue;
            int length2 = iArr.length;
            while (i < length2) {
                arrayList.add(Integer.valueOf(iArr[i]));
                i++;
            }
        } else if (propertyValue instanceof float[]) {
            float[] fArr = (float[]) propertyValue;
            int length3 = fArr.length;
            while (i < length3) {
                arrayList.add(Float.valueOf(fArr[i]));
                i++;
            }
        } else if ((propertyValue instanceof Float) || (propertyValue instanceof Long) || (propertyValue instanceof Integer)) {
            arrayList.add(propertyValue);
        } else {
            Object[] objArr = (Object[]) propertyValue;
            int length4 = objArr.length;
            while (i < length4) {
                arrayList.add(objArr[i]);
                i++;
            }
        }
        return arrayList;
    }

    @Override // ohos.media.camera.device.CameraAbility
    public List<ResultKey.Key<?>> getSupportedResults() {
        return Collections.unmodifiableList(this.supportedResults);
    }

    private Optional<PropertyKeyWrapper> getPropertyRangeKey(ParameterKey.Key<?> key) {
        if (key == null || !PARAMETER_TO_PROPERTY.containsKey(key)) {
            return Optional.empty();
        }
        if (key.checkType(Boolean.class)) {
            return Optional.of(new PropertyKeyWrapper(true, PARAMETER_TO_PROPERTY.get(key)));
        }
        return Optional.of(new PropertyKeyWrapper(false, PARAMETER_TO_PROPERTY.get(key)));
    }

    public List<?> getPropertyRange(PropertyKey.Key<byte[]> key) {
        return Collections.emptyList();
    }

    public boolean isLogicalCamera() {
        int[] iArr = (int[]) getPropertyValue(InnerPropertyKey.AVAILABLE_CAPABILITIES);
        if (iArr != null) {
            for (int i : iArr) {
                if (i == 11) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> getPhysicalCameraIds() {
        if (!isLogicalCamera()) {
            LOGGER.debug("This is not a logical camera, cannot get physical camera", new Object[0]);
            return Collections.emptySet();
        }
        byte[] bArr = (byte[]) getPropertyValue(InnerPropertyKey.LOGICAL_CAMERA_PHYSICAL_IDS);
        if (bArr == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet(Arrays.asList(new String(bArr, StandardCharsets.UTF_8).split("\u0000"))));
    }

    /* access modifiers changed from: private */
    public static class PropertyKeyWrapper {
        private final boolean isBool;
        private final PropertyKey.Key<?> key;

        public PropertyKeyWrapper(boolean z, PropertyKey.Key<?> key2) {
            this.isBool = z;
            this.key = key2;
        }
    }
}
