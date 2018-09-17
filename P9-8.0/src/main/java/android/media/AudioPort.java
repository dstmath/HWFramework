package android.media;

public class AudioPort {
    public static final int ROLE_NONE = 0;
    public static final int ROLE_SINK = 2;
    public static final int ROLE_SOURCE = 1;
    private static final String TAG = "AudioPort";
    public static final int TYPE_DEVICE = 1;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_SESSION = 3;
    public static final int TYPE_SUBMIX = 2;
    private AudioPortConfig mActiveConfig;
    private final int[] mChannelIndexMasks;
    private final int[] mChannelMasks;
    private final int[] mFormats;
    private final AudioGain[] mGains;
    AudioHandle mHandle;
    private final String mName;
    protected final int mRole;
    private final int[] mSamplingRates;

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

    AudioHandle handle() {
        return this.mHandle;
    }

    public int id() {
        return this.mHandle.id();
    }

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

    AudioGain gain(int index) {
        if (index < 0 || index >= this.mGains.length) {
            return null;
        }
        return this.mGains[index];
    }

    public AudioPortConfig buildConfig(int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        return new AudioPortConfig(this, samplingRate, channelMask, format, gain);
    }

    public AudioPortConfig activeConfig() {
        return this.mActiveConfig;
    }

    public boolean equals(Object o) {
        if (o == null || ((o instanceof AudioPort) ^ 1) != 0) {
            return false;
        }
        return this.mHandle.equals(((AudioPort) o).handle());
    }

    public int hashCode() {
        return this.mHandle.hashCode();
    }

    public String toString() {
        String role = Integer.toString(this.mRole);
        switch (this.mRole) {
            case 0:
                role = "NONE";
                break;
            case 1:
                role = "SOURCE";
                break;
            case 2:
                role = "SINK";
                break;
        }
        return "{mHandle: " + this.mHandle + ", mRole: " + role + "}";
    }
}
