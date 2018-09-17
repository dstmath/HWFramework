package android.hardware.camera2.legacy;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.legacy.LegacyExceptionUtils.BufferQueueAbandonedException;
import android.util.Log;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.Collection;

public class RequestHolder {
    private static final String TAG = "RequestHolder";
    private volatile boolean mFailed;
    private final long mFrameNumber;
    private final Collection<Long> mJpegSurfaceIds;
    private final int mNumJpegTargets;
    private final int mNumPreviewTargets;
    private boolean mOutputAbandoned;
    private final boolean mRepeating;
    private final CaptureRequest mRequest;
    private final int mRequestId;
    private final int mSubsequeceId;

    public static final class Builder {
        private final Collection<Long> mJpegSurfaceIds;
        private final int mNumJpegTargets = numJpegTargets(this.mRequest);
        private final int mNumPreviewTargets = numPreviewTargets(this.mRequest);
        private final boolean mRepeating;
        private final CaptureRequest mRequest;
        private final int mRequestId;
        private final int mSubsequenceId;

        public Builder(int requestId, int subsequenceId, CaptureRequest request, boolean repeating, Collection<Long> jpegSurfaceIds) {
            Preconditions.checkNotNull(request, "request must not be null");
            this.mRequestId = requestId;
            this.mSubsequenceId = subsequenceId;
            this.mRequest = request;
            this.mRepeating = repeating;
            this.mJpegSurfaceIds = jpegSurfaceIds;
        }

        private boolean jpegType(Surface s) throws BufferQueueAbandonedException {
            return LegacyCameraDevice.containsSurfaceId(s, this.mJpegSurfaceIds);
        }

        private boolean previewType(Surface s) throws BufferQueueAbandonedException {
            return jpegType(s) ^ 1;
        }

        private int numJpegTargets(CaptureRequest request) {
            int count = 0;
            for (Surface s : request.getTargets()) {
                try {
                    if (jpegType(s)) {
                        count++;
                    }
                } catch (BufferQueueAbandonedException e) {
                    Log.d(RequestHolder.TAG, "Surface abandoned, skipping...", e);
                }
            }
            return count;
        }

        private int numPreviewTargets(CaptureRequest request) {
            int count = 0;
            for (Surface s : request.getTargets()) {
                try {
                    if (previewType(s)) {
                        count++;
                    }
                } catch (BufferQueueAbandonedException e) {
                    Log.d(RequestHolder.TAG, "Surface abandoned, skipping...", e);
                }
            }
            return count;
        }

        public RequestHolder build(long frameNumber) {
            return new RequestHolder(this.mRequestId, this.mSubsequenceId, this.mRequest, this.mRepeating, frameNumber, this.mNumJpegTargets, this.mNumPreviewTargets, this.mJpegSurfaceIds, null);
        }
    }

    /* synthetic */ RequestHolder(int requestId, int subsequenceId, CaptureRequest request, boolean repeating, long frameNumber, int numJpegTargets, int numPreviewTargets, Collection jpegSurfaceIds, RequestHolder -this8) {
        this(requestId, subsequenceId, request, repeating, frameNumber, numJpegTargets, numPreviewTargets, jpegSurfaceIds);
    }

    private RequestHolder(int requestId, int subsequenceId, CaptureRequest request, boolean repeating, long frameNumber, int numJpegTargets, int numPreviewTargets, Collection<Long> jpegSurfaceIds) {
        this.mFailed = false;
        this.mOutputAbandoned = false;
        this.mRepeating = repeating;
        this.mRequest = request;
        this.mRequestId = requestId;
        this.mSubsequeceId = subsequenceId;
        this.mFrameNumber = frameNumber;
        this.mNumJpegTargets = numJpegTargets;
        this.mNumPreviewTargets = numPreviewTargets;
        this.mJpegSurfaceIds = jpegSurfaceIds;
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    public boolean isRepeating() {
        return this.mRepeating;
    }

    public int getSubsequeceId() {
        return this.mSubsequeceId;
    }

    public long getFrameNumber() {
        return this.mFrameNumber;
    }

    public CaptureRequest getRequest() {
        return this.mRequest;
    }

    public Collection<Surface> getHolderTargets() {
        return getRequest().getTargets();
    }

    public boolean hasJpegTargets() {
        return this.mNumJpegTargets > 0;
    }

    public boolean hasPreviewTargets() {
        return this.mNumPreviewTargets > 0;
    }

    public int numJpegTargets() {
        return this.mNumJpegTargets;
    }

    public int numPreviewTargets() {
        return this.mNumPreviewTargets;
    }

    public boolean jpegType(Surface s) throws BufferQueueAbandonedException {
        return LegacyCameraDevice.containsSurfaceId(s, this.mJpegSurfaceIds);
    }

    public void failRequest() {
        Log.w(TAG, "Capture failed for request: " + getRequestId());
        this.mFailed = true;
    }

    public boolean requestFailed() {
        return this.mFailed;
    }

    public void setOutputAbandoned() {
        this.mOutputAbandoned = true;
    }

    public boolean isOutputAbandoned() {
        return this.mOutputAbandoned;
    }
}
