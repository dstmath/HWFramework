package android.hardware.camera2;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.impl.PhysicalCaptureResultInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TotalCaptureResult extends CaptureResult {
    private final List<CaptureResult> mPartialResults;
    private final HashMap<String, CaptureResult> mPhysicalCaptureResults;
    private final int mSessionId;

    public TotalCaptureResult(CameraMetadataNative results, CaptureRequest parent, CaptureResultExtras extras, List<CaptureResult> partials, int sessionId, PhysicalCaptureResultInfo[] physicalResults) {
        super(results, parent, extras);
        if (partials == null) {
            this.mPartialResults = new ArrayList();
        } else {
            this.mPartialResults = partials;
        }
        this.mSessionId = sessionId;
        this.mPhysicalCaptureResults = new HashMap<>();
        for (PhysicalCaptureResultInfo onePhysicalResult : physicalResults) {
            this.mPhysicalCaptureResults.put(onePhysicalResult.getCameraId(), new CaptureResult(onePhysicalResult.getCameraMetadata(), parent, extras));
        }
    }

    public TotalCaptureResult(CameraMetadataNative results, int sequenceId) {
        super(results, sequenceId);
        this.mPartialResults = new ArrayList();
        this.mSessionId = -1;
        this.mPhysicalCaptureResults = new HashMap<>();
    }

    public List<CaptureResult> getPartialResults() {
        return Collections.unmodifiableList(this.mPartialResults);
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public Map<String, CaptureResult> getPhysicalCameraResults() {
        return Collections.unmodifiableMap(this.mPhysicalCaptureResults);
    }
}
