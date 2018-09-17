package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.util.Size;
import com.android.internal.util.Preconditions;

public class LegacyRequest {
    public final CaptureRequest captureRequest;
    public final CameraCharacteristics characteristics;
    public final Parameters parameters;
    public final Size previewSize;

    public LegacyRequest(CameraCharacteristics characteristics, CaptureRequest captureRequest, Size previewSize, Parameters parameters) {
        this.characteristics = (CameraCharacteristics) Preconditions.checkNotNull(characteristics, "characteristics must not be null");
        this.captureRequest = (CaptureRequest) Preconditions.checkNotNull(captureRequest, "captureRequest must not be null");
        this.previewSize = (Size) Preconditions.checkNotNull(previewSize, "previewSize must not be null");
        Preconditions.checkNotNull(parameters, "parameters must not be null");
        this.parameters = Camera.getParametersCopy(parameters);
    }

    public void setParameters(Parameters parameters) {
        Preconditions.checkNotNull(parameters, "parameters must not be null");
        this.parameters.copyFrom(parameters);
    }
}
