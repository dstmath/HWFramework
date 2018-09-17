package android.media;

public class AudioGainConfig {
    private final int mChannelMask;
    AudioGain mGain;
    private final int mIndex;
    private final int mMode;
    private final int mRampDurationMs;
    private final int[] mValues;

    AudioGainConfig(int index, AudioGain gain, int mode, int channelMask, int[] values, int rampDurationMs) {
        this.mIndex = index;
        this.mGain = gain;
        this.mMode = mode;
        this.mChannelMask = channelMask;
        this.mValues = values;
        this.mRampDurationMs = rampDurationMs;
    }

    int index() {
        return this.mIndex;
    }

    public int mode() {
        return this.mMode;
    }

    public int channelMask() {
        return this.mChannelMask;
    }

    public int[] values() {
        return this.mValues;
    }

    public int rampDurationMs() {
        return this.mRampDurationMs;
    }
}
