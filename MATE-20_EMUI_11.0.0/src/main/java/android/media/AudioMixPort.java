package android.media;

import android.annotation.UnsupportedAppUsage;

public class AudioMixPort extends AudioPort {
    private final int mIoHandle;

    @UnsupportedAppUsage
    AudioMixPort(AudioHandle handle, int ioHandle, int role, String deviceName, int[] samplingRates, int[] channelMasks, int[] channelIndexMasks, int[] formats, AudioGain[] gains) {
        super(handle, role, deviceName, samplingRates, channelMasks, channelIndexMasks, formats, gains);
        this.mIoHandle = ioHandle;
    }

    @Override // android.media.AudioPort
    public AudioMixPortConfig buildConfig(int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        return new AudioMixPortConfig(this, samplingRate, channelMask, format, gain);
    }

    @UnsupportedAppUsage
    public int ioHandle() {
        return this.mIoHandle;
    }

    @Override // android.media.AudioPort
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AudioMixPort) || this.mIoHandle != ((AudioMixPort) o).ioHandle()) {
            return false;
        }
        return super.equals(o);
    }
}
