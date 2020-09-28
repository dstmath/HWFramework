package android.media;

import android.annotation.UnsupportedAppUsage;

public class AudioDevicePort extends AudioPort {
    private final String mAddress;
    private final int mType;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    @UnsupportedAppUsage
    AudioDevicePort(AudioHandle handle, String deviceName, int[] samplingRates, int[] channelMasks, int[] channelIndexMasks, int[] formats, AudioGain[] gains, int type, String address) {
        super(handle, AudioManager.isInputDevice(type) ? 1 : 2, deviceName, samplingRates, channelMasks, channelIndexMasks, formats, gains);
        this.mType = type;
        this.mAddress = address;
    }

    @UnsupportedAppUsage
    public int type() {
        return this.mType;
    }

    public String address() {
        return this.mAddress;
    }

    @Override // android.media.AudioPort
    public AudioDevicePortConfig buildConfig(int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        return new AudioDevicePortConfig(this, samplingRate, channelMask, format, gain);
    }

    @Override // android.media.AudioPort
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AudioDevicePort)) {
            return false;
        }
        AudioDevicePort other = (AudioDevicePort) o;
        if (this.mType != other.type()) {
            return false;
        }
        if ((this.mAddress != null || other.address() == null) && this.mAddress.equals(other.address())) {
            return super.equals(o);
        }
        return false;
    }

    @Override // android.media.AudioPort
    public String toString() {
        String type;
        if (this.mRole == 1) {
            type = AudioSystem.getInputDeviceName(this.mType);
        } else {
            type = AudioSystem.getOutputDeviceName(this.mType);
        }
        return "{" + super.toString() + ", mType: " + type + ", mAddress: " + this.mAddress + "}";
    }
}
