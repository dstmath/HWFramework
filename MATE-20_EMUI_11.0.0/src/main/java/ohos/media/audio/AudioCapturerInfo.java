package ohos.media.audio;

import ohos.multimodalinput.event.KeyEvent;

public final class AudioCapturerInfo {
    public static final int SESSION_ID_UNSPECIFIED = 0;
    private final AudioStreamInfo audioStreamInfo;
    private final long bufferSizeInBytes;
    private final String distributedDeviceId;
    private final AudioInputSource inputSource;
    private final int sessionID;

    private AudioCapturerInfo(AudioStreamInfo audioStreamInfo2, int i, long j, AudioInputSource audioInputSource, String str) {
        this.audioStreamInfo = audioStreamInfo2;
        this.sessionID = i;
        this.bufferSizeInBytes = j;
        this.inputSource = audioInputSource;
        this.distributedDeviceId = str;
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

    public AudioInputSource getInputSource() {
        return this.inputSource;
    }

    public String getDistributedDeviceId() {
        return this.distributedDeviceId;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("AudioCapturerInfo: inputSource = ");
        sb.append(this.inputSource);
        sb.append(", sessionID = ");
        sb.append(this.sessionID);
        sb.append(", bufferSizeInBytes = ");
        sb.append(this.bufferSizeInBytes);
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
        private AudioInputSource inputSource = AudioInputSource.AUDIO_INPUT_SOURCE_INVALID;
        private int sessionID = 0;

        public Builder audioStreamInfo(AudioStreamInfo audioStreamInfo2) {
            this.audioStreamInfo = audioStreamInfo2;
            return this;
        }

        public Builder sessionID(int i) {
            this.sessionID = i;
            return this;
        }

        public Builder bufferSizeInBytes(long j) {
            this.bufferSizeInBytes = j;
            return this;
        }

        public Builder audioInputSource(AudioInputSource audioInputSource) {
            this.inputSource = audioInputSource;
            return this;
        }

        public Builder distributedDeviceId(String str) {
            this.distributedDeviceId = str;
            return this;
        }

        public AudioCapturerInfo build() {
            return new AudioCapturerInfo(this.audioStreamInfo, this.sessionID, this.bufferSizeInBytes, this.inputSource, this.distributedDeviceId);
        }
    }

    public enum AudioInputSource {
        AUDIO_INPUT_SOURCE_INVALID(-1),
        AUDIO_INPUT_SOURCE_DEFAULT(0),
        AUDIO_INPUT_SOURCE_MIC(1),
        AUDIO_INPUT_SOURCE_VOICE_UPLINK(2),
        AUDIO_INPUT_SOURCE_VOICE_DOWNLINK(3),
        AUDIO_INPUT_SOURCE_VOICE_CALL(4),
        AUDIO_INPUT_SOURCE_CAMCORDER(5),
        AUDIO_INPUT_SOURCE_VOICE_RECOGNITION(6),
        AUDIO_INPUT_SOURCE_VOICE_COMMUNICATION(7),
        AUDIO_INPUT_SOURCE_REMOTE_SUBMIX(8),
        AUDIO_INPUT_SOURCE_UNPROCESSED(9),
        AUDIO_INPUT_SOURCE_VOICE_PERFORMANCE(10),
        AUDIO_INPUT_SOURCE_ECHO_REFERENCE(1997),
        AUDIO_INPUT_SOURCE_FM_TUNER(1998),
        AUDIO_INPUT_SOURCE_HOTWORD(1999),
        AUDIO_INPUT_SOURCE_REMOTE_SUBMIX_EXTEND(KeyEvent.KEY_VOICE_SOURCE_SWITCH);
        
        private final int inputSource;

        private AudioInputSource(int i) {
            this.inputSource = i;
        }

        public int getValue() {
            return this.inputSource;
        }

        public static AudioInputSource getEnum(int i) {
            AudioInputSource[] values = values();
            for (AudioInputSource audioInputSource : values) {
                if (audioInputSource.getValue() == i) {
                    return audioInputSource;
                }
            }
            return AUDIO_INPUT_SOURCE_INVALID;
        }
    }
}
