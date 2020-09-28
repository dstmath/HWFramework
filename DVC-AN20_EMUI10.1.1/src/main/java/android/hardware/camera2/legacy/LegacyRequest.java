package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.util.Size;
import com.android.internal.util.Preconditions;

public class LegacyRequest {
    public final CaptureRequest captureRequest;
    public final CameraCharacteristics characteristics;
    public final Camera.Parameters parameters;
    public final Size previewSize;

    public LegacyRequest(CameraCharacteristics characteristics2, CaptureRequest captureRequest2, Size previewSize2, Camera.Parameters parameters2) {
        this.characteristics = (CameraCharacteristics) Preconditions.checkNotNull(characteristics2, "characteristics must not be null");
        this.captureRequest = (CaptureRequest) Preconditions.checkNotNull(captureRequest2, "captureRequest must not be null");
        this.previewSize = (Size) Preconditions.checkNotNull(previewSize2, "previewSize must not be null");
        Preconditions.checkNotNull(parameters2, "parameters must not be null");
        this.parameters = Camera.getParametersCopy(parameters2);
    }

    public void setParameters(Camera.Parameters parameters2) {
        Preconditions.checkNotNull(parameters2, "parameters must not be null");
        this.parameters.copyFrom(parameters2);
    }
}
