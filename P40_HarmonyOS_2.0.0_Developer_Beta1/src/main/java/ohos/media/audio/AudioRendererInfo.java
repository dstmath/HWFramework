package ohos.media.audio;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class AudioRendererInfo {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioRendererInfo.class);
    public static final int SESSION_ID_UNSPECIFIED = 0;
    private final AudioStreamInfo audioStreamInfo;
    private final long bufferSizeInBytes;
    private final String distributedDeviceId;
    private final boolean isOffload;
    private final AudioStreamOutputFlag outputFlag;
    private final int selectDeviceId;
    private final int sessionID;

    private AudioRendererInfo(AudioStreamInfo audioStreamInfo2, int i, long j, boolean z, AudioStreamOutputFlag audioStreamOutputFlag, String str, int i2) {
        this.audioStreamInfo = audioStreamInfo2;
        this.sessionID = i;
        this.bufferSizeInBytes = j;
        this.isOffload = z;
        this.outputFlag = audioStreamOutputFlag;
        this.distributedDeviceId = str;
        this.selectDeviceId = i2;
    }

    public AudioStreamInfo getAudioStreamInfo() {
        return this.audioStreamInfo;
    }

    public int getSessionID() {
        return this.sessionID;
    }

    public long getBufferSizeInBytes() {
        return this.bufferSizeInBytes;
    }

    public boolean isOffload() {
        return this.isOffload;
    }

    public AudioStreamOutputFlag getAudioStreamOutputFlag() {
        return this.outputFlag;
    }

    public String getDistributedDeviceId() {
        return this.distributedDeviceId;
    }

    public int getDeviceId() {
        return this.selectDeviceId;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("AudioRendererInfo: streamOutputFlag = ");
        sb.append(this.outputFlag);
        sb.append(", sessionID = ");
        sb.append(this.sessionID);
        sb.append(", bufferSizeInBytes = ");
        sb.append(this.bufferSizeInBytes);
        sb.append(", isOffload = ");
        sb.append(this.isOffload);
        if (this.audioStreamInfo == null) {
            str = "";
        } else {
            str = "," + this.audioStreamInfo.toString();
        }
        sb.append(str);
        return sb.toString();
    }

    public static class Builder {
        private AudioStreamInfo audioStreamInfo;
        private long bufferSizeInBytes = 0;
        private String distributedDeviceId = "";
        private boolean isOffload = false;
        private AudioStreamOutputFlag outputFlag = AudioStreamOutputFlag.AUDIO_STREAM_OUTPUT_FLAG_NONE;
        private int selectDeviceId = 0;
        private int sessionID = 0;

        public Builder audioStreamInfo(AudioStreamInfo audioStreamInfo2) {
            this.audioStreamInfo = audioStreamInfo2;
            return this;
        }

        public Builder sessionID(int i) {
            this.sessionID = i;
            return this;
        }

        public Builder deviceId(int i) {
            this.selectDeviceId = i;
            return this;
        }

        public Builder bufferSizeInBytes(long j) {
            this.bufferSizeInBytes = j;
            return this;
        }

        public Builder isOffload(boolean z) {
            this.isOffload = z;
            return this;
        }

        public Builder audioStreamOutputFlag(AudioStreamOutputFlag audioStreamOutputFlag) {
            this.outputFlag = audioStreamOutputFlag;
            return this;
        }

        public Builder distributedDeviceId(String str) {
            this.distributedDeviceId = str;
            return this;
        }

        public AudioRendererInfo build() {
            return new AudioRendererInfo(this.audioStreamInfo, this.sessionID, this.bufferSizeInBytes, this.isOffload, this.outputFlag, this.distributedDeviceId, this.selectDeviceId);
        }
    }

    public enum AudioStreamOutputFlag {
        AUDIO_STREAM_OUTPUT_FLAG_NONE(0),
        AUDIO_STREAM_OUTPUT_FLAG_DIRECT(1),
        AUDIO_STREAM_OUTPUT_FLAG_PRIMARY(2),
        AUDIO_STREAM_OUTPUT_FLAG_FAST(4),
        AUDIO_STREAM_OUTPUT_FLAG_DEEP_BUFFER(8),
        AUDIO_STREAM_OUTPUT_FLAG_COMPRESS_OFFLOAD(16),
        AUDIO_STREAM_OUTPUT_FLAG_NONE_BLOCKING(32),
        AUDIO_STREAM_OUTPUT_FLAG_HW_AV_SYNC(64),
        AUDIO_STREAM_OUTPUT_FLAG_TTS(128),
        AUDIO_STREAM_OUTPUT_FLAG_RAW(256),
        AUDIO_STREAM_OUTPUT_FLAG_SYNC(512),
        AUDIO_STREAM_OUTPUT_FLAG_IEC958_NONAUDIO(1024),
        AUDIO_STREAM_OUTPUT_FLAG_DIRECT_PCM(8192),
        AUDIO_STREAM_OUTPUT_FLAG_MMAP_NOIRQ(16384),
        AUDIO_STREAM_OUTPUT_FLAG_VOIP_RX(32768),
        AUDIO_STREAM_OUTPUT_FLAG_INCALL_MUSIC(65536);
        
        private final int outputFlag;

        private AudioStreamOutputFlag(int i) {
            this.outputFlag = i;
        }

        public static AudioStreamOutputFlag valueOf(int i) {
            AudioStreamOutputFlag[] values = values();
            for (AudioStreamOutputFlag audioStreamOutputFlag : values) {
                if (i == audioStreamOutputFlag.getOutputFlag()) {
                    return audioStreamOutputFlag;
                }
            }
            AudioRendererInfo.LOGGER.error("Invalid input value for AudioStreamOutputFlag, value = %{public}d", Integer.valueOf(i));
            return null;
        }

        public int getOutputFlag() {
            return this.outputFlag;
        }
    }
}
