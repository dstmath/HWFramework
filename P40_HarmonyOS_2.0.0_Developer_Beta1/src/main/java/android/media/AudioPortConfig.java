package android.media;

import android.annotation.UnsupportedAppUsage;

public class AudioPortConfig {
    static final int CHANNEL_MASK = 2;
    static final int FORMAT = 4;
    static final int GAIN = 8;
    static final int SAMPLE_RATE = 1;
    @UnsupportedAppUsage
    private final int mChannelMask;
    @UnsupportedAppUsage
    int mConfigMask = 0;
    @UnsupportedAppUsage
    private final int mFormat;
    @UnsupportedAppUsage
    private final AudioGainConfig mGain;
    @UnsupportedAppUsage
    final AudioPort mPort;
    @UnsupportedAppUsage
    private final int mSamplingRate;

    @UnsupportedAppUsage
    AudioPortConfig(AudioPort port, int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        this.mPort = port;
        this.mSamplingRate = samplingRate;
        this.mChannelMask = channelMask;
        this.mFormat = format;
        this.mGain = gain;
    }

    @UnsupportedAppUsage
    public AudioPort port() {
        return this.mPort;
    }

    public int samplingRate() {
        return this.mSamplingRate;
    }

    public int channelMask() {
        return this.mChannelMask;
    }

    public int format() {
        return this.mFormat;
    }

    public AudioGainConfig gain() {
        return this.mGain;
    }

    public String toString() {
        return "{mPort:" + this.mPort + ", mSamplingRate:" + this.mSamplingRate + ", mChannelMask: " + this.mChannelMask + ", mFormat:" + this.mFormat + ", mGain:" + this.mGain + "}";
    }
}
