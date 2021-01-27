package ohos.media.camera.device.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.agp.graphics.Surface;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.params.AeResult;
import ohos.media.camera.params.AfResult;
import ohos.media.camera.params.Face;
import ohos.media.camera.params.FaceDetectionResult;
import ohos.media.camera.params.ParametersResult;
import ohos.media.camera.params.ResultKey;
import ohos.media.camera.params.adapter.InnerResultKey;
import ohos.media.camera.params.impl.AeResultImpl;
import ohos.media.camera.params.impl.AfResultImpl;
import ohos.media.camera.params.impl.FaceDetectionResultImpl;
import ohos.media.camera.params.impl.ParametersResultImpl;
import ohos.media.camera.zidl.FrameResultNative;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class FrameResultImpl implements FrameResult {
    private static final int FACE_ID_BITS = -65536;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(FrameResultImpl.class);
    private static final int SHIFT_BITS = 16;
    private static final int SMILE_SCORE_BITS = 255;
    private AeResult aeResult;
    private AfResult afResult;
    private final int captureTriggerId;
    private FaceDetectionResult faceDetectionResult;
    private final FrameConfig frameConfig;
    private final long frameNumber;
    private final boolean isPartial;
    private final Map<ResultKey.Key<?>, Object> logicalCameraResults;
    private final long timestamp;

    public FrameResultImpl(FrameResultNative frameResultNative) {
        this.frameConfig = new FrameConfigImpl(frameResultNative.getFrameConfigNative());
        this.frameNumber = frameResultNative.getFrameNumber();
        this.captureTriggerId = frameResultNative.getCaptureTriggerId();
        this.isPartial = frameResultNative.isPartial();
        this.logicalCameraResults = frameResultNative.getLogicalCameraResult();
        this.timestamp = frameResultNative.getTimestamp();
    }

    @Override // ohos.media.camera.device.FrameResult
    public long getFrameNumber() {
        return this.frameNumber;
    }

    @Override // ohos.media.camera.device.FrameResult
    @FrameResult.State
    public int getState() {
        return this.isPartial ? 2 : 1;
    }

    @Override // ohos.media.camera.device.FrameResult
    public boolean isFullResult() {
        return !this.isPartial;
    }

    @Override // ohos.media.camera.device.FrameResult
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override // ohos.media.camera.device.FrameResult
    public int getCaptureTriggerId() {
        return this.captureTriggerId;
    }

    @Override // ohos.media.camera.device.FrameResult
    public FrameConfig getFrameConfig() {
        return this.frameConfig;
    }

    @Override // ohos.media.camera.device.FrameResult
    public AfResult getAfResult() {
        Map<ResultKey.Key<?>, Object> map = this.logicalCameraResults;
        if (map == null) {
            LOGGER.warn("FrameResult logicalCameraResults is null, frameNumber: %{public}d", Long.valueOf(this.frameNumber));
            return AfResultImpl.getDefault();
        } else if (!map.containsKey(InnerResultKey.AF_STATE)) {
            LOGGER.warn("FrameResult does not contains af result, frameNumber: %{public}d", Long.valueOf(this.frameNumber));
            return AfResultImpl.getDefault();
        } else if (this.afResult != null) {
            LOGGER.debug("afResult not null, return directly, frameNumber: %{public}d", Long.valueOf(this.frameNumber));
            return this.afResult;
        } else {
            LOGGER.debug("init afResult, frameNumber: %{public}d", Long.valueOf(this.frameNumber));
            this.afResult = new AfResultImpl((Integer) this.logicalCameraResults.get(InnerResultKey.AF_STATE));
            return this.afResult;
        }
    }

    @Override // ohos.media.camera.device.FrameResult
    public AeResult getAeResult() {
        Map<ResultKey.Key<?>, Object> map = this.logicalCameraResults;
        if (map == null) {
            LOGGER.warn("FrameResult logicalCameraResults is null, %{public}d", Long.valueOf(this.frameNumber));
            return AeResultImpl.getDefault();
        } else if (!map.containsKey(InnerResultKey.AE_STATE)) {
            LOGGER.warn("FrameResult does not contains ae result, %{public}d", Long.valueOf(this.frameNumber));
            return AeResultImpl.getDefault();
        } else if (this.aeResult != null) {
            LOGGER.debug("aeResult not null, return directly, frameNumber: %{public}d", Long.valueOf(this.frameNumber));
            return this.aeResult;
        } else {
            LOGGER.debug("init aeResult, frameNumber: %{public}d", Long.valueOf(this.frameNumber));
            this.aeResult = new AeResultImpl((Integer) this.logicalCameraResults.get(InnerResultKey.AE_STATE));
            return this.aeResult;
        }
    }

    @Override // ohos.media.camera.device.FrameResult
    public FaceDetectionResult getFaceDetectionResult() {
        Map<ResultKey.Key<?>, Object> map = this.logicalCameraResults;
        if (map == null) {
            LOGGER.warn("FrameResult logicalCameraResults is null, %{public}d", Long.valueOf(this.frameNumber));
            return FaceDetectionResultImpl.getDefault();
        } else if (!map.containsKey(InnerResultKey.FACE_DETECT)) {
            LOGGER.debug("FrameResult does not contains face detect result, %{public}d", Long.valueOf(this.frameNumber));
            return FaceDetectionResultImpl.getDefault();
        } else if (this.faceDetectionResult != null) {
            LOGGER.debug("faceDetectionResult not null, return directly, frameNumber: %{public}d", Long.valueOf(this.frameNumber));
            return this.faceDetectionResult;
        } else {
            this.faceDetectionResult = new FaceDetectionResultImpl((Face[]) this.logicalCameraResults.get(InnerResultKey.FACE_DETECT), getSmileScores());
            return this.faceDetectionResult;
        }
    }

    @Override // ohos.media.camera.device.FrameResult
    public ParametersResult getParametersResult() {
        Map<ResultKey.Key<?>, Object> map = this.logicalCameraResults;
        if (map != null) {
            return new ParametersResultImpl(1, map);
        }
        LOGGER.warn("FrameResult logicalCameraResults is null, %{public}d", Long.valueOf(this.frameNumber));
        return null;
    }

    @Override // ohos.media.camera.device.FrameResult
    public List<Surface> getDroppedBufferOwners() {
        return Collections.emptyList();
    }

    private Map<Integer, Integer> getSmileScores() {
        int[] iArr = (int[]) this.logicalCameraResults.get(InnerResultKey.FACE_SMILE_SCORE);
        HashMap hashMap = new HashMap();
        if (iArr != null) {
            for (int i : iArr) {
                int i2 = (FACE_ID_BITS & i) >> 16;
                int i3 = i & 255;
                hashMap.put(Integer.valueOf(i2), Integer.valueOf(i3));
                LOGGER.debug("Face smile data, smileId: %{public}d, smileScore: %{public}d", Integer.valueOf(i2), Integer.valueOf(i3));
            }
        }
        return hashMap;
    }
}
