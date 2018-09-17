package android.hardware.camera2;

public class CaptureFailure {
    public static final int REASON_ERROR = 0;
    public static final int REASON_FLUSHED = 1;
    private final boolean mDropped;
    private final long mFrameNumber;
    private final int mReason;
    private final CaptureRequest mRequest;
    private final int mSequenceId;

    public CaptureFailure(CaptureRequest request, int reason, boolean dropped, int sequenceId, long frameNumber) {
        this.mRequest = request;
        this.mReason = reason;
        this.mDropped = dropped;
        this.mSequenceId = sequenceId;
        this.mFrameNumber = frameNumber;
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
}
