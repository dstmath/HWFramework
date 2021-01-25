package ohos.media.audio;

import ohos.media.audio.AudioCapturerInfo;

public final class AudioCapturerConfig {
    private final AudioCapturerInfo.AudioInputSource audioInputSource;
    private final int capturerPortId;
    private final boolean capturerSilenced;
    private final String packageName;
    private final int sessionId;
    private final AudioStreamInfo streamInfo;

    public AudioCapturerConfig() {
        this(0, null, "", 0, false, AudioCapturerInfo.AudioInputSource.AUDIO_INPUT_SOURCE_INVALID);
    }

    public AudioCapturerConfig(int i, AudioStreamInfo audioStreamInfo, String str, int i2, boolean z, AudioCapturerInfo.AudioInputSource audioInputSource2) {
        this.sessionId = i;
        this.streamInfo = audioStreamInfo;
        this.packageName = str;
        this.capturerPortId = i2;
        this.capturerSilenced = z;
        this.audioInputSource = audioInputSource2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public AudioStreamInfo getStreamInfo() {
        return this.streamInfo;
    }

    public int getSessionId() {
        return this.sessionId;
    }

    public int getPortId() {
        return this.capturerPortId;
    }

    public boolean isSilenced() {
        return this.capturerSilenced;
    }

    public AudioCapturerInfo.AudioInputSource getAudioInputSource() {
        return this.audioInputSource;
    }
}
