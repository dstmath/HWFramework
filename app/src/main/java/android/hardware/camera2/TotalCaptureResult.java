package android.hardware.camera2;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TotalCaptureResult extends CaptureResult {
    private final List<CaptureResult> mPartialResults;
    private final int mSessionId;

    public TotalCaptureResult(CameraMetadataNative results, CaptureRequest parent, CaptureResultExtras extras, List<CaptureResult> partials, int sessionId) {
        super(results, parent, extras);
        if (partials == null) {
            this.mPartialResults = new ArrayList();
        } else {
            this.mPartialResults = partials;
        }
        this.mSessionId = sessionId;
    }

    public TotalCaptureResult(CameraMetadataNative results, int sequenceId) {
        super(results, sequenceId);
        this.mPartialResults = new ArrayList();
        this.mSessionId = -1;
    }

    public List<CaptureResult> getPartialResults() {
        return Collections.unmodifiableList(this.mPartialResults);
    }

    public int getSessionId() {
        return this.mSessionId;
    }
}
