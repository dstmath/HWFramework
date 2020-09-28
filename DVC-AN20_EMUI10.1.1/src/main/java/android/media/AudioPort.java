package android.media;

import android.annotation.UnsupportedAppUsage;
import android.security.keystore.KeyProperties;

public class AudioPort {
    public static final int ROLE_NONE = 0;
    public static final int ROLE_SINK = 2;
    public static final int ROLE_SOURCE = 1;
    private static final String TAG = "AudioPort";
    public static final int TYPE_DEVICE = 1;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_SESSION = 3;
    public static final int TYPE_SUBMIX = 2;
    @UnsupportedAppUsage
    private AudioPortConfig mActiveConfig;
    private final int[] mChannelIndexMasks;
    private final int[] mChannelMasks;
    private final int[] mFormats;
    @UnsupportedAppUsage
    private final AudioGain[] mGains;
    @UnsupportedAppUsage
    AudioHandle mHandle;
    private final String mName;
    @UnsupportedAppUsage
    protected final int mRole;
    private final int[] mSamplingRates;

    @UnsupportedAppUsage
    AudioPort(AudioHandle handle, int role, String name, int[] samplingRates, int[] channelMasks, int[] channelIndexMasks, int[] formats, AudioGain[] gains) {
        this.mHandle = handle;
        this.mRole = role;
        this.mName = name;
        this.mSamplingRates = samplingRates;
        this.mChannelMasks = channelMasks;
        this.mChannelIndexMasks = channelIndexMasks;
        this.mFormats = formats;
        this.mGains = gains;
    }

    /* access modifiers changed from: package-private */
    public AudioHandle handle() {
        return this.mHandle;
    }

    @UnsupportedAppUsage
    public int id() {
        return this.mHandle.id();
    }

    @UnsupportedAppUsage
    public int role() {
        return this.mRole;
    }

    public String name() {
        return this.mName;
    }

    public int[] samplingRates() {
        return this.mSamplingRates;
    }

    public int[] channelMasks() {
        return this.mChannelMasks;
    }

    public int[] channelIndexMasks() {
        return this.mChannelIndexMasks;
    }

    public int[] formats() {
        return this.mFormats;
    }

    public AudioGain[] gains() {
        return this.mGains;
    }

    /* access modifiers changed from: package-private */
    public AudioGain gain(int index) {
        if (index < 0) {
            return null;
        }
        AudioGain[] audioGainArr = this.mGains;
        if (index >= audioGainArr.length) {
            return null;
        }
        return audioGainArr[index];
    }

    public AudioPortConfig buildConfig(int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        return new AudioPortConfig(this, samplingRate, channelMask, format, gain);
    }

    public AudioPortConfig activeConfig() {
        return this.mActiveConfig;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof AudioPort)) {
            return false;
        }
        return this.mHandle.equals(((AudioPort) o).handle());
    }

    public int hashCode() {
        return this.mHandle.hashCode();
    }

    public String toString() {
        String role = Integer.toString(this.mRole);
        int i = this.mRole;
        if (i == 0) {
            role = KeyProperties.DIGEST_NONE;
        } else if (i == 1) {
            role = HwMediaMonitorUtils.TYPE_SOURCE;
        } else if (i == 2) {
            role = "SINK";
        }
        return "{mHandle: " + this.mHandle + ", mRole: " + role + "}";
    }
}
