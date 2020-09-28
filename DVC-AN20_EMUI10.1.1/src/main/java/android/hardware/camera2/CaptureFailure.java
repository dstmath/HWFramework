package android.hardware.camera2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CaptureFailure {
    public static final int REASON_ERROR = 0;
    public static final int REASON_FLUSHED = 1;
    private final boolean mDropped;
    private final String mErrorPhysicalCameraId;
    private final long mFrameNumber;
    private final int mReason;
    private final CaptureRequest mRequest;
    private final int mSequenceId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface FailureReason {
    }

    public CaptureFailure(CaptureRequest request, int reason, boolean dropped, int sequenceId, long frameNumber, String errorPhysicalCameraId) {
        this.mRequest = request;
        this.mReason = reason;
        this.mDropped = dropped;
        this.mSequenceId = sequenceId;
        this.mFrameNumber = frameNumber;
        this.mErrorPhysicalCameraId = errorPhysicalCameraId;
    }

    public CaptureRequest getRequest() {
        return this.mRequest;
    }

    public long getFrameNumber() {
        return this.mFrameNumber;
    }

    public int getReason() {
        return this.mReason;
    }

    public boolean wasImageCaptured() {
        return !this.mDropped;
    }

    public int getSequenceId() {
        return this.mSequenceId;
    }

    public String getPhysicalCameraId() {
        return this.mErrorPhysicalCameraId;
    }
}
