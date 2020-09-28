package android.media;

import android.annotation.UnsupportedAppUsage;

public class AudioGainConfig {
    @UnsupportedAppUsage
    private final int mChannelMask;
    AudioGain mGain;
    @UnsupportedAppUsage
    private final int mIndex;
    @UnsupportedAppUsage
    private final int mMode;
    @UnsupportedAppUsage
    private final int mRampDurationMs;
    @UnsupportedAppUsage
    private final int[] mValues;

    @UnsupportedAppUsage
    AudioGainConfig(int index, AudioGain gain, int mode, int channelMask, int[] values, int rampDurationMs) {
        this.mIndex = index;
        this.mGain = gain;
        this.mMode = mode;
        this.mChannelMask = channelMask;
        this.mValues = values;
        this.mRampDurationMs = rampDurationMs;
    }

    /* access modifiers changed from: package-private */
    public int index() {
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
