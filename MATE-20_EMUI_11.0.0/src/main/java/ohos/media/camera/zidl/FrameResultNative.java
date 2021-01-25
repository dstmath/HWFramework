package ohos.media.camera.zidl;

import java.util.Map;
import ohos.media.camera.params.ResultKey;

public class FrameResultNative {
    private final int afTriggerId;
    private final int captureTriggerId;
    private final String errorPhysicalCameraId;
    private final int errorStreamId;
    private final FrameConfigNative frameConfigNative;
    private final long frameNumber;
    private final boolean isPartial;
    private final Map<ResultKey.Key<?>, Object> logicalCameraResult;
    private final Map<String, Map<ResultKey.Key<?>, Object>> physicalCaptureResults;
    private final int preCaptureTriggerId;
    private final int sequenceId;
    private final long timestamp;

    public FrameResultNative(Builder builder) {
        this.frameNumber = builder.frameNumber;
        this.captureTriggerId = builder.captureTriggerId;
        this.sequenceId = builder.sequenceId;
        this.timestamp = builder.timestamp;
        this.afTriggerId = builder.afTriggerId;
        this.preCaptureTriggerId = builder.preCaptureTriggerId;
        this.isPartial = builder.isPartial;
        this.errorStreamId = builder.errorStreamId;
        this.errorPhysicalCameraId = builder.errorPhysicalCameraId;
        this.logicalCameraResult = builder.logicalCameraResult;
        this.physicalCaptureResults = builder.physicalCaptureResults;
        this.frameConfigNative = builder.frameConfigNative;
    }

    public long getFrameNumber() {
        return this.frameNumber;
    }

    public int getSequenceId() {
        return this.sequenceId;
    }

    public int getCaptureTriggerId() {
        return this.captureTriggerId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getAfTriggerId() {
        return this.afTriggerId;
    }

    public int getPreCaptureTriggerId() {
        return this.preCaptureTriggerId;
    }

    public boolean isPartial() {
        return this.isPartial;
    }

    public int getErrorStreamId() {
        return this.errorStreamId;
    }

    public String getErrorPhysicalCameraId() {
        return this.errorPhysicalCameraId;
    }

    public Map<ResultKey.Key<?>, Object> getLogicalCameraResult() {
        return this.logicalCameraResult;
    }

    public Map<String, Map<ResultKey.Key<?>, Object>> getPhysicalCaptureResults() {
        return this.physicalCaptureResults;
    }

    public FrameConfigNative getFrameConfigNative() {
        return this.frameConfigNative;
    }

    public static class Builder {
        private int afTriggerId = -1;
        private final int captureTriggerId;
        private String errorPhysicalCameraId = null;
        private int errorStreamId = -1;
        private final FrameConfigNative frameConfigNative;
        private final long frameNumber;
        private boolean isPartial = false;
        private Map<ResultKey.Key<?>, Object> logicalCameraResult = null;
        private Map<String, Map<ResultKey.Key<?>, Object>> physicalCaptureResults = null;
        private int preCaptureTriggerId = -1;
        private final int sequenceId;
        private long timestamp = -1;

        public Builder(long j, int i, int i2, FrameConfigNative frameConfigNative2) {
            this.frameNumber = j;
            this.captureTriggerId = i;
            this.sequenceId = i2;
            this.frameConfigNative = frameConfigNative2;
        }

        public Builder timestamp(long j) {
            this.timestamp = j;
            return this;
        }

        public Builder afTriggerId(int i) {
            this.afTriggerId = i;
            return this;
        }

        public Builder preCaptureTriggerId(int i) {
            this.preCaptureTriggerId = i;
            return this;
        }

        public Builder isPartial(boolean z) {
            this.isPartial = z;
            return this;
        }

        public Builder errorStreamId(int i) {
            this.errorStreamId = i;
            return this;
        }

        public Builder errorPhysicalCameraId(String str) {
            this.errorPhysicalCameraId = str;
            return this;
        }

        public Builder logicalCameraResult(Map<ResultKey.Key<?>, Object> map) {
            this.logicalCameraResult = map;
            return this;
        }

        public Builder physicalCaptureResults(Map<String, Map<ResultKey.Key<?>, Object>> map) {
            this.physicalCaptureResults = map;
            return this;
        }

        public FrameResultNative build() {
            return new FrameResultNative(this);
        }
    }
}
