package ohos.media.common;

public final class AudioProperty {
    public static final int AUDIO_ENCODER_INIT = -1;
    public static final int BIT_RATE_INIT = 0;
    public static final int NUM_CHANNELS_INIT = 0;
    public static final int SAMPLING_RATE_INIT = 0;
    private int recorderAudioEncoder;
    private int recorderBitRate;
    private int recorderNumChannels;
    private int recorderSamplingRate;

    private AudioProperty() {
        this.recorderNumChannels = 0;
        this.recorderAudioEncoder = -1;
        this.recorderBitRate = 0;
        this.recorderSamplingRate = 0;
    }

    public int getRecorderNumChannels() {
        return this.recorderNumChannels;
    }

    public int getRecorderAudioEncoder() {
        return this.recorderAudioEncoder;
    }

    public int getRecorderBitRate() {
        return this.recorderBitRate;
    }

    public int getRecorderSamplingRate() {
        return this.recorderSamplingRate;
    }

    public static class Builder {
        private int recorderAudioEncoder = -1;
        private int recorderBitRate = 0;
        private int recorderNumChannels = 0;
        private int recorderSamplingRate = 0;

        public Builder() {
        }

        public Builder(AudioProperty audioProperty) {
            this.recorderNumChannels = audioProperty.recorderNumChannels;
            this.recorderAudioEncoder = audioProperty.recorderAudioEncoder;
            this.recorderBitRate = audioProperty.recorderBitRate;
            this.recorderSamplingRate = audioProperty.recorderSamplingRate;
        }

        public AudioProperty build() {
            AudioProperty audioProperty = new AudioProperty();
            audioProperty.recorderNumChannels = this.recorderNumChannels;
            audioProperty.recorderAudioEncoder = this.recorderAudioEncoder;
            audioProperty.recorderBitRate = this.recorderBitRate;
            audioProperty.recorderSamplingRate = this.recorderSamplingRate;
            return audioProperty;
        }

        public Builder setRecorderNumChannels(int i) {
            this.recorderNumChannels = i;
            return this;
        }

        public Builder setRecorderAudioEncoder(int i) {
            this.recorderAudioEncoder = i;
            return this;
        }

        public Builder setRecorderBitRate(int i) {
            this.recorderBitRate = i;
            return this;
        }

        public Builder setRecorderSamplingRate(int i) {
            this.recorderSamplingRate = i;
            return this;
        }
    }
}
