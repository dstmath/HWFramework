package android.media;

public class AudioMixPort extends AudioPort {
    private final int mIoHandle;

    AudioMixPort(AudioHandle handle, int ioHandle, int role, String deviceName, int[] samplingRates, int[] channelMasks, int[] channelIndexMasks, int[] formats, AudioGain[] gains) {
        super(handle, role, deviceName, samplingRates, channelMasks, channelIndexMasks, formats, gains);
        this.mIoHandle = ioHandle;
    }

    public AudioMixPortConfig buildConfig(int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        AudioMixPortConfig audioMixPortConfig = new AudioMixPortConfig(this, samplingRate, channelMask, format, gain);
        return audioMixPortConfig;
    }

    public int ioHandle() {
        return this.mIoHandle;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof AudioMixPort) || this.mIoHandle != ((AudioMixPort) o).ioHandle()) {
            return false;
        }
        return super.equals(o);
    }
}
