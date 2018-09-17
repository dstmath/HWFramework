package android.media;

public class AudioMixPortConfig extends AudioPortConfig {
    AudioMixPortConfig(AudioMixPort mixPort, int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        super(mixPort, samplingRate, channelMask, format, gain);
    }

    public AudioMixPort port() {
        return (AudioMixPort) this.mPort;
    }
}
