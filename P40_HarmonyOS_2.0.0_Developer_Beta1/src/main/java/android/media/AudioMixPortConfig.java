package android.media;

import android.annotation.UnsupportedAppUsage;

public class AudioMixPortConfig extends AudioPortConfig {
    @UnsupportedAppUsage
    AudioMixPortConfig(AudioMixPort mixPort, int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        super(mixPort, samplingRate, channelMask, format, gain);
    }

    @Override // android.media.AudioPortConfig
    public AudioMixPort port() {
        return (AudioMixPort) this.mPort;
    }
}
