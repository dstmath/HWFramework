package ohos.media.camera.params.adapter;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.impl.CameraMetadataNative;
import java.util.Map;
import ohos.media.camera.params.ParameterKey;

public abstract class SpecialMappedKeyMethod {
    /* access modifiers changed from: package-private */
    public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
        throw new UnsupportedOperationException("Fatal Error: Must call from sub class!");
    }

    /* access modifiers changed from: package-private */
    public <T> T getMappedRequestKeyValue(CameraMetadataNative cameraMetadataNative) {
        throw new UnsupportedOperationException("Fatal Error: Must call from sub class!");
    }

    /* access modifiers changed from: package-private */
    public <T> T getMappedParameterKeyValue(Map<ParameterKey.Key<?>, Object> map) {
        throw new UnsupportedOperationException("Fatal Error: Must call from sub class!");
    }

    /* access modifiers changed from: package-private */
    public <T> T getMappedResultKeyValue(CameraMetadataNative cameraMetadataNative, StaticCameraCharacteristics staticCameraCharacteristics) {
        throw new UnsupportedOperationException("Fatal Error: Must call from sub class!");
    }
}
