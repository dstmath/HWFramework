package ohos.media.common;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class VideoProperty {
    public static final int BITRATE_INIT = -1;
    public static final int DEGREES_INIT = -1;
    public static final int FPS_INIT = 0;
    public static final int HEIGHT_INIT = -1;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(VideoProperty.class);
    public static final int RATE_INIT = -1;
    public static final int VIDEO_ENCODER_INIT = -1;
    public static final int WIDTH_INIT = -1;
    private int recorderBitRate;
    private int recorderDegrees;
    private int recorderFps;
    private int recorderHeight;
    private int recorderRate;
    private int recorderVideoEncoder;
    private int recorderWidth;

    private VideoProperty() {
        this.recorderFps = 0;
        this.recorderDegrees = -1;
        this.recorderRate = -1;
        this.recorderWidth = -1;
        this.recorderHeight = -1;
        this.recorderVideoEncoder = -1;
        this.recorderBitRate = -1;
    }

    public int getRecorderFps() {
        return this.recorderFps;
    }

    public int getRecorderDegrees() {
        return this.recorderDegrees;
    }

    public int getRecorderRate() {
        return this.recorderRate;
    }

    public int getRecorderWidth() {
        return this.recorderWidth;
    }

    public int getRecorderHeight() {
        return this.recorderHeight;
    }

    public int getRecorderVideoEncoder() {
        return this.recorderVideoEncoder;
    }

    public int getRecorderBitRate() {
        return this.recorderBitRate;
    }

    public static class Builder {
        private int recorderBitRate = -1;
        private int recorderDegrees = -1;
        private int recorderFps = 0;
        private int recorderHeight = -1;
        private int recorderRate = -1;
        private int recorderVideoEncoder = -1;
        private int recorderWidth = -1;

        public Builder() {
        }

        public Builder(VideoProperty videoProperty) {
            if (videoProperty == null) {
                VideoProperty.LOGGER.error("property cannot be null", new Object[0]);
                return;
            }
            this.recorderFps = videoProperty.recorderFps;
            this.recorderDegrees = videoProperty.recorderDegrees;
            this.recorderRate = videoProperty.recorderRate;
            this.recorderWidth = videoProperty.recorderWidth;
            this.recorderHeight = videoProperty.recorderHeight;
            this.recorderVideoEncoder = videoProperty.recorderVideoEncoder;
            this.recorderBitRate = videoProperty.recorderBitRate;
        }

        public VideoProperty build() {
            VideoProperty videoProperty = new VideoProperty();
            videoProperty.recorderFps = this.recorderFps;
            videoProperty.recorderDegrees = this.recorderDegrees;
            videoProperty.recorderRate = this.recorderRate;
            videoProperty.recorderWidth = this.recorderWidth;
            videoProperty.recorderHeight = this.recorderHeight;
            videoProperty.recorderVideoEncoder = this.recorderVideoEncoder;
            videoProperty.recorderBitRate = this.recorderBitRate;
            return videoProperty;
        }

        public Builder setRecorderFps(int i) {
            this.recorderFps = i;
            return this;
        }

        public Builder setRecorderDegrees(int i) {
            this.recorderDegrees = i;
            return this;
        }

        public Builder setRecorderRate(int i) {
            this.recorderRate = i;
            return this;
        }

        public Builder setRecorderWidth(int i) {
            this.recorderWidth = i;
            return this;
        }

        public Builder setRecorderHeight(int i) {
            this.recorderHeight = i;
            return this;
        }

        public Builder setRecorderVideoEncoder(int i) {
            this.recorderVideoEncoder = i;
            return this;
        }

        public Builder setRecorderBitRate(int i) {
            this.recorderBitRate = i;
            return this;
        }
    }
}
