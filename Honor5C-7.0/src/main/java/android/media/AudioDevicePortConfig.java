package android.media;

public class AudioDevicePortConfig extends AudioPortConfig {
    AudioDevicePortConfig(AudioDevicePort devicePort, int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        super(devicePort, samplingRate, channelMask, format, gain);
    }

    AudioDevicePortConfig(AudioDevicePortConfig config) {
        this(config.port(), config.samplingRate(), config.channelMask(), config.format(), config.gain());
    }

    public AudioDevicePort port() {
        return (AudioDevicePort) this.mPort;
    }
}
