package android.bluetooth;

import android.net.wifi.WifiScanLog;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class BluetoothCodecConfig implements Parcelable {
    public static final int BITS_PER_SAMPLE_16 = 1;
    public static final int BITS_PER_SAMPLE_24 = 2;
    public static final int BITS_PER_SAMPLE_32 = 4;
    public static final int BITS_PER_SAMPLE_NONE = 0;
    public static final int CHANNEL_MODE_MONO = 1;
    public static final int CHANNEL_MODE_NONE = 0;
    public static final int CHANNEL_MODE_STEREO = 2;
    public static final int CODEC_PRIORITY_DEFAULT = 0;
    public static final int CODEC_PRIORITY_DISABLED = -1;
    public static final int CODEC_PRIORITY_HIGHEST = 1000000;
    public static final Creator<BluetoothCodecConfig> CREATOR = new Creator<BluetoothCodecConfig>() {
        public BluetoothCodecConfig createFromParcel(Parcel in) {
            return new BluetoothCodecConfig(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readLong(), in.readLong(), in.readLong(), in.readLong());
        }

        public BluetoothCodecConfig[] newArray(int size) {
            return new BluetoothCodecConfig[size];
        }
    };
    public static final int SAMPLE_RATE_176400 = 16;
    public static final int SAMPLE_RATE_192000 = 32;
    public static final int SAMPLE_RATE_44100 = 1;
    public static final int SAMPLE_RATE_48000 = 2;
    public static final int SAMPLE_RATE_88200 = 4;
    public static final int SAMPLE_RATE_96000 = 8;
    public static final int SAMPLE_RATE_NONE = 0;
    public static final int SOURCE_CODEC_TYPE_AAC = 1;
    public static final int SOURCE_CODEC_TYPE_APTX = 2;
    public static final int SOURCE_CODEC_TYPE_APTX_HD = 3;
    public static final int SOURCE_CODEC_TYPE_INVALID = 1000000;
    public static final int SOURCE_CODEC_TYPE_LDAC = 4;
    public static final int SOURCE_CODEC_TYPE_MAX = 5;
    public static final int SOURCE_CODEC_TYPE_SBC = 0;
    private final int mBitsPerSample;
    private final int mChannelMode;
    private int mCodecPriority;
    private final long mCodecSpecific1;
    private final long mCodecSpecific2;
    private final long mCodecSpecific3;
    private final long mCodecSpecific4;
    private final int mCodecType;
    private final int mSampleRate;

    public BluetoothCodecConfig(int codecType, int codecPriority, int sampleRate, int bitsPerSample, int channelMode, long codecSpecific1, long codecSpecific2, long codecSpecific3, long codecSpecific4) {
        this.mCodecType = codecType;
        this.mCodecPriority = codecPriority;
        this.mSampleRate = sampleRate;
        this.mBitsPerSample = bitsPerSample;
        this.mChannelMode = channelMode;
        this.mCodecSpecific1 = codecSpecific1;
        this.mCodecSpecific2 = codecSpecific2;
        this.mCodecSpecific3 = codecSpecific3;
        this.mCodecSpecific4 = codecSpecific4;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof BluetoothCodecConfig)) {
            return false;
        }
        BluetoothCodecConfig other = (BluetoothCodecConfig) o;
        if (other.mCodecType == this.mCodecType && other.mCodecPriority == this.mCodecPriority && other.mSampleRate == this.mSampleRate && other.mBitsPerSample == this.mBitsPerSample && other.mChannelMode == this.mChannelMode && other.mCodecSpecific1 == this.mCodecSpecific1 && other.mCodecSpecific2 == this.mCodecSpecific2 && other.mCodecSpecific3 == this.mCodecSpecific3 && other.mCodecSpecific4 == this.mCodecSpecific4) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mCodecType), Integer.valueOf(this.mCodecPriority), Integer.valueOf(this.mSampleRate), Integer.valueOf(this.mBitsPerSample), Integer.valueOf(this.mChannelMode), Long.valueOf(this.mCodecSpecific1), Long.valueOf(this.mCodecSpecific2), Long.valueOf(this.mCodecSpecific3), Long.valueOf(this.mCodecSpecific4)});
    }

    public boolean isValid() {
        if (this.mSampleRate == 0 || this.mBitsPerSample == 0 || this.mChannelMode == 0) {
            return false;
        }
        return true;
    }

    private static String appendCapabilityToString(String prevStr, String capStr) {
        if (prevStr == null) {
            return capStr;
        }
        return prevStr + "|" + capStr;
    }

    public String toString() {
        String sampleRateStr = null;
        if (this.mSampleRate == 0) {
            sampleRateStr = appendCapabilityToString(null, "NONE");
        }
        if ((this.mSampleRate & 1) != 0) {
            sampleRateStr = appendCapabilityToString(sampleRateStr, "44100");
        }
        if ((this.mSampleRate & 2) != 0) {
            sampleRateStr = appendCapabilityToString(sampleRateStr, "48000");
        }
        if ((this.mSampleRate & 4) != 0) {
            sampleRateStr = appendCapabilityToString(sampleRateStr, "88200");
        }
        if ((this.mSampleRate & 8) != 0) {
            sampleRateStr = appendCapabilityToString(sampleRateStr, "96000");
        }
        if ((this.mSampleRate & 16) != 0) {
            sampleRateStr = appendCapabilityToString(sampleRateStr, "176400");
        }
        if ((this.mSampleRate & 32) != 0) {
            sampleRateStr = appendCapabilityToString(sampleRateStr, "192000");
        }
        String bitsPerSampleStr = null;
        if (this.mBitsPerSample == 0) {
            bitsPerSampleStr = appendCapabilityToString(null, "NONE");
        }
        if ((this.mBitsPerSample & 1) != 0) {
            bitsPerSampleStr = appendCapabilityToString(bitsPerSampleStr, WifiScanLog.EVENT_KEY16);
        }
        if ((this.mBitsPerSample & 2) != 0) {
            bitsPerSampleStr = appendCapabilityToString(bitsPerSampleStr, WifiScanLog.EVENT_KEY24);
        }
        if ((this.mBitsPerSample & 4) != 0) {
            bitsPerSampleStr = appendCapabilityToString(bitsPerSampleStr, WifiScanLog.EVENT_KEY32);
        }
        String channelModeStr = null;
        if (this.mChannelMode == 0) {
            channelModeStr = appendCapabilityToString(null, "NONE");
        }
        if ((this.mChannelMode & 1) != 0) {
            channelModeStr = appendCapabilityToString(channelModeStr, "MONO");
        }
        if ((this.mChannelMode & 2) != 0) {
            channelModeStr = appendCapabilityToString(channelModeStr, "STEREO");
        }
        return "{codecName:" + getCodecName() + ",mCodecType:" + this.mCodecType + ",mCodecPriority:" + this.mCodecPriority + ",mSampleRate:" + String.format("0x%x", new Object[]{Integer.valueOf(this.mSampleRate)}) + "(" + sampleRateStr + ")" + ",mBitsPerSample:" + String.format("0x%x", new Object[]{Integer.valueOf(this.mBitsPerSample)}) + "(" + bitsPerSampleStr + ")" + ",mChannelMode:" + String.format("0x%x", new Object[]{Integer.valueOf(this.mChannelMode)}) + "(" + channelModeStr + ")" + ",mCodecSpecific1:" + this.mCodecSpecific1 + ",mCodecSpecific2:" + this.mCodecSpecific2 + ",mCodecSpecific3:" + this.mCodecSpecific3 + ",mCodecSpecific4:" + this.mCodecSpecific4 + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCodecType);
        out.writeInt(this.mCodecPriority);
        out.writeInt(this.mSampleRate);
        out.writeInt(this.mBitsPerSample);
        out.writeInt(this.mChannelMode);
        out.writeLong(this.mCodecSpecific1);
        out.writeLong(this.mCodecSpecific2);
        out.writeLong(this.mCodecSpecific3);
        out.writeLong(this.mCodecSpecific4);
    }

    public String getCodecName() {
        switch (this.mCodecType) {
            case 0:
                return "SBC";
            case 1:
                return "AAC";
            case 2:
                return "aptX";
            case 3:
                return "aptX HD";
            case 4:
                return "LDAC";
            case 1000000:
                return "INVALID CODEC";
            default:
                return "UNKNOWN CODEC(" + this.mCodecType + ")";
        }
    }

    public int getCodecType() {
        return this.mCodecType;
    }

    public boolean isMandatoryCodec() {
        return this.mCodecType == 0;
    }

    public int getCodecPriority() {
        return this.mCodecPriority;
    }

    public void setCodecPriority(int codecPriority) {
        this.mCodecPriority = codecPriority;
    }

    public int getSampleRate() {
        return this.mSampleRate;
    }

    public int getBitsPerSample() {
        return this.mBitsPerSample;
    }

    public int getChannelMode() {
        return this.mChannelMode;
    }

    public long getCodecSpecific1() {
        return this.mCodecSpecific1;
    }

    public long getCodecSpecific2() {
        return this.mCodecSpecific2;
    }

    public long getCodecSpecific3() {
        return this.mCodecSpecific3;
    }

    public long getCodecSpecific4() {
        return this.mCodecSpecific4;
    }

    public boolean sameAudioFeedingParameters(BluetoothCodecConfig other) {
        if (other != null && other.mSampleRate == this.mSampleRate && other.mBitsPerSample == this.mBitsPerSample && other.mChannelMode == this.mChannelMode) {
            return true;
        }
        return false;
    }
}
