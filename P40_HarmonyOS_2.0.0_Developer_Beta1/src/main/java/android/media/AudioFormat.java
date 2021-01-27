package android.media;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;

public final class AudioFormat implements Parcelable {
    public static final int AUDIO_FORMAT_HAS_PROPERTY_CHANNEL_INDEX_MASK = 8;
    public static final int AUDIO_FORMAT_HAS_PROPERTY_CHANNEL_MASK = 4;
    public static final int AUDIO_FORMAT_HAS_PROPERTY_ENCODING = 1;
    public static final int AUDIO_FORMAT_HAS_PROPERTY_NONE = 0;
    public static final int AUDIO_FORMAT_HAS_PROPERTY_SAMPLE_RATE = 2;
    @Deprecated
    public static final int CHANNEL_CONFIGURATION_DEFAULT = 1;
    @Deprecated
    public static final int CHANNEL_CONFIGURATION_INVALID = 0;
    @Deprecated
    public static final int CHANNEL_CONFIGURATION_MONO = 2;
    @Deprecated
    public static final int CHANNEL_CONFIGURATION_STEREO = 3;
    public static final int CHANNEL_INVALID = 0;
    public static final int CHANNEL_IN_BACK = 32;
    public static final int CHANNEL_IN_BACK_PROCESSED = 512;
    public static final int CHANNEL_IN_DEFAULT = 1;
    public static final int CHANNEL_IN_FRONT = 16;
    public static final int CHANNEL_IN_FRONT_BACK = 48;
    public static final int CHANNEL_IN_FRONT_PROCESSED = 256;
    public static final int CHANNEL_IN_LEFT = 4;
    public static final int CHANNEL_IN_LEFT_PROCESSED = 64;
    public static final int CHANNEL_IN_MONO = 16;
    public static final int CHANNEL_IN_PRESSURE = 1024;
    public static final int CHANNEL_IN_RIGHT = 8;
    public static final int CHANNEL_IN_RIGHT_PROCESSED = 128;
    public static final int CHANNEL_IN_STEREO = 12;
    public static final int CHANNEL_IN_VOICE_DNLINK = 32768;
    public static final int CHANNEL_IN_VOICE_UPLINK = 16384;
    public static final int CHANNEL_IN_X_AXIS = 2048;
    public static final int CHANNEL_IN_Y_AXIS = 4096;
    public static final int CHANNEL_IN_Z_AXIS = 8192;
    public static final int CHANNEL_OUT_5POINT1 = 252;
    public static final int CHANNEL_OUT_5POINT1_SIDE = 6204;
    @Deprecated
    public static final int CHANNEL_OUT_7POINT1 = 1020;
    public static final int CHANNEL_OUT_7POINT1_SURROUND = 6396;
    public static final int CHANNEL_OUT_BACK_CENTER = 1024;
    public static final int CHANNEL_OUT_BACK_LEFT = 64;
    public static final int CHANNEL_OUT_BACK_RIGHT = 128;
    public static final int CHANNEL_OUT_DEFAULT = 1;
    public static final int CHANNEL_OUT_FRONT_CENTER = 16;
    public static final int CHANNEL_OUT_FRONT_LEFT = 4;
    public static final int CHANNEL_OUT_FRONT_LEFT_OF_CENTER = 256;
    public static final int CHANNEL_OUT_FRONT_RIGHT = 8;
    public static final int CHANNEL_OUT_FRONT_RIGHT_OF_CENTER = 512;
    public static final int CHANNEL_OUT_LOW_FREQUENCY = 32;
    public static final int CHANNEL_OUT_MONO = 4;
    public static final int CHANNEL_OUT_QUAD = 204;
    public static final int CHANNEL_OUT_QUAD_SIDE = 6156;
    public static final int CHANNEL_OUT_SIDE_LEFT = 2048;
    public static final int CHANNEL_OUT_SIDE_RIGHT = 4096;
    public static final int CHANNEL_OUT_STEREO = 12;
    public static final int CHANNEL_OUT_SURROUND = 1052;
    public static final int CHANNEL_OUT_TOP_BACK_CENTER = 262144;
    public static final int CHANNEL_OUT_TOP_BACK_LEFT = 131072;
    public static final int CHANNEL_OUT_TOP_BACK_RIGHT = 524288;
    public static final int CHANNEL_OUT_TOP_CENTER = 8192;
    public static final int CHANNEL_OUT_TOP_FRONT_CENTER = 32768;
    public static final int CHANNEL_OUT_TOP_FRONT_LEFT = 16384;
    public static final int CHANNEL_OUT_TOP_FRONT_RIGHT = 65536;
    public static final Parcelable.Creator<AudioFormat> CREATOR = new Parcelable.Creator<AudioFormat>() {
        /* class android.media.AudioFormat.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioFormat createFromParcel(Parcel p) {
            return new AudioFormat(p);
        }

        @Override // android.os.Parcelable.Creator
        public AudioFormat[] newArray(int size) {
            return new AudioFormat[size];
        }
    };
    public static final int ENCODING_AAC_ELD = 15;
    public static final int ENCODING_AAC_HE_V1 = 11;
    public static final int ENCODING_AAC_HE_V2 = 12;
    public static final int ENCODING_AAC_LC = 10;
    public static final int ENCODING_AAC_XHE = 16;
    public static final int ENCODING_AC3 = 5;
    public static final int ENCODING_AC4 = 17;
    public static final int ENCODING_DEFAULT = 1;
    public static final int ENCODING_DOLBY_MAT = 19;
    public static final int ENCODING_DOLBY_TRUEHD = 14;
    public static final int ENCODING_DTS = 7;
    public static final int ENCODING_DTS_HD = 8;
    public static final int ENCODING_E_AC3 = 6;
    public static final int ENCODING_E_AC3_JOC = 18;
    public static final int ENCODING_IEC61937 = 13;
    public static final int ENCODING_INVALID = 0;
    public static final int ENCODING_MP3 = 9;
    public static final int ENCODING_PCM_16BIT = 2;
    public static final int ENCODING_PCM_8BIT = 3;
    public static final int ENCODING_PCM_FLOAT = 4;
    public static final int SAMPLE_RATE_HZ_MAX = 192000;
    public static final int SAMPLE_RATE_HZ_MAX_FOR_DIRECT = 384000;
    public static final int SAMPLE_RATE_HZ_MIN = 4000;
    public static final int SAMPLE_RATE_UNSPECIFIED = 0;
    public static final int[] SURROUND_SOUND_ENCODING = {5, 6, 7, 8, 10, 14, 17, 18, 19};
    private final int mChannelCount;
    private final int mChannelIndexMask;
    @UnsupportedAppUsage
    private final int mChannelMask;
    @UnsupportedAppUsage
    private final int mEncoding;
    private final int mFrameSizeInBytes;
    private final int mPropertySetMask;
    @UnsupportedAppUsage
    private final int mSampleRate;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Encoding {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SurroundSoundEncoding {
    }

    public static String toLogFriendlyEncoding(int enc) {
        if (enc == 0) {
            return "ENCODING_INVALID";
        }
        switch (enc) {
            case 2:
                return "ENCODING_PCM_16BIT";
            case 3:
                return "ENCODING_PCM_8BIT";
            case 4:
                return "ENCODING_PCM_FLOAT";
            case 5:
                return "ENCODING_AC3";
            case 6:
                return "ENCODING_E_AC3";
            case 7:
                return "ENCODING_DTS";
            case 8:
                return "ENCODING_DTS_HD";
            case 9:
                return "ENCODING_MP3";
            case 10:
                return "ENCODING_AAC_LC";
            case 11:
                return "ENCODING_AAC_HE_V1";
            case 12:
                return "ENCODING_AAC_HE_V2";
            case 13:
                return "ENCODING_IEC61937";
            case 14:
                return "ENCODING_DOLBY_TRUEHD";
            case 15:
                return "ENCODING_AAC_ELD";
            case 16:
                return "ENCODING_AAC_XHE";
            case 17:
                return "ENCODING_AC4";
            case 18:
                return "ENCODING_E_AC3_JOC";
            case 19:
                return "ENCODING_DOLBY_MAT";
            default:
                return "invalid encoding " + enc;
        }
    }

    public static int inChannelMaskFromOutChannelMask(int outMask) throws IllegalArgumentException {
        if (outMask != 1) {
            int channelCountFromOutChannelMask = channelCountFromOutChannelMask(outMask);
            if (channelCountFromOutChannelMask == 1) {
                return 16;
            }
            if (channelCountFromOutChannelMask == 2) {
                return 12;
            }
            throw new IllegalArgumentException("Unsupported channel configuration for input.");
        }
        throw new IllegalArgumentException("Illegal CHANNEL_OUT_DEFAULT channel mask for input.");
    }

    public static int channelCountFromInChannelMask(int mask) {
        return Integer.bitCount(mask);
    }

    public static int channelCountFromOutChannelMask(int mask) {
        return Integer.bitCount(mask);
    }

    public static int convertChannelOutMaskToNativeMask(int javaMask) {
        return javaMask >> 2;
    }

    public static int convertNativeChannelMaskToOutMask(int nativeMask) {
        return nativeMask << 2;
    }

    public static int getBytesPerSample(int audioFormat) {
        if (!(audioFormat == 1 || audioFormat == 2)) {
            if (audioFormat == 3) {
                return 1;
            }
            if (audioFormat == 4) {
                return 4;
            }
            if (audioFormat != 13) {
                throw new IllegalArgumentException("Bad audio format " + audioFormat);
            }
        }
        return 2;
    }

    public static boolean isValidEncoding(int audioFormat) {
        switch (audioFormat) {
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPublicEncoding(int audioFormat) {
        switch (audioFormat) {
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEncodingLinearPcm(int audioFormat) {
        switch (audioFormat) {
            case 1:
            case 2:
            case 3:
            case 4:
                return true;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                return false;
            default:
                throw new IllegalArgumentException("Bad audio format " + audioFormat);
        }
    }

    public static boolean isEncodingLinearFrames(int audioFormat) {
        switch (audioFormat) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 13:
                return true;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                return false;
            default:
                throw new IllegalArgumentException("Bad audio format " + audioFormat);
        }
    }

    public static int[] filterPublicFormats(int[] formats) {
        if (formats == null) {
            return null;
        }
        int[] myCopy = Arrays.copyOf(formats, formats.length);
        int size = 0;
        for (int i = 0; i < myCopy.length; i++) {
            if (isPublicEncoding(myCopy[i])) {
                if (size != i) {
                    myCopy[size] = myCopy[i];
                }
                size++;
            }
        }
        return Arrays.copyOf(myCopy, size);
    }

    public AudioFormat() {
        throw new UnsupportedOperationException("There is no valid usage of this constructor");
    }

    @UnsupportedAppUsage
    private AudioFormat(int encoding, int sampleRate, int channelMask, int channelIndexMask) {
        this(15, encoding, sampleRate, channelMask, channelIndexMask);
    }

    private AudioFormat(int propertySetMask, int encoding, int sampleRate, int channelMask, int channelIndexMask) {
        this.mPropertySetMask = propertySetMask;
        int i = 0;
        this.mEncoding = (propertySetMask & 1) != 0 ? encoding : 0;
        this.mSampleRate = (propertySetMask & 2) != 0 ? sampleRate : 0;
        this.mChannelMask = (propertySetMask & 4) != 0 ? channelMask : 0;
        this.mChannelIndexMask = (propertySetMask & 8) != 0 ? channelIndexMask : i;
        int channelIndexCount = Integer.bitCount(getChannelIndexMask());
        int channelCount = channelCountFromOutChannelMask(getChannelMask());
        if (channelCount == 0) {
            channelCount = channelIndexCount;
        } else if (!(channelCount == channelIndexCount || channelIndexCount == 0)) {
            channelCount = 0;
        }
        this.mChannelCount = channelCount;
        int frameSizeInBytes = 1;
        try {
            frameSizeInBytes = getBytesPerSample(this.mEncoding) * channelCount;
        } catch (IllegalArgumentException e) {
        }
        this.mFrameSizeInBytes = frameSizeInBytes != 0 ? frameSizeInBytes : 1;
    }

    public int getEncoding() {
        return this.mEncoding;
    }

    public int getSampleRate() {
        return this.mSampleRate;
    }

    public int getChannelMask() {
        return this.mChannelMask;
    }

    public int getChannelIndexMask() {
        return this.mChannelIndexMask;
    }

    public int getChannelCount() {
        return this.mChannelCount;
    }

    public int getFrameSizeInBytes() {
        return this.mFrameSizeInBytes;
    }

    public int getPropertySetMask() {
        return this.mPropertySetMask;
    }

    public String toLogFriendlyString() {
        return String.format("%dch %dHz %s", Integer.valueOf(this.mChannelCount), Integer.valueOf(this.mSampleRate), toLogFriendlyEncoding(this.mEncoding));
    }

    public static class Builder {
        private int mChannelIndexMask = 0;
        private int mChannelMask = 0;
        private int mEncoding = 0;
        private int mPropertySetMask = 0;
        private int mSampleRate = 0;

        public Builder() {
        }

        public Builder(AudioFormat af) {
            this.mEncoding = af.mEncoding;
            this.mSampleRate = af.mSampleRate;
            this.mChannelMask = af.mChannelMask;
            this.mChannelIndexMask = af.mChannelIndexMask;
            this.mPropertySetMask = af.mPropertySetMask;
        }

        public AudioFormat build() {
            return new AudioFormat(this.mPropertySetMask, this.mEncoding, this.mSampleRate, this.mChannelMask, this.mChannelIndexMask);
        }

        public Builder setEncoding(int encoding) throws IllegalArgumentException {
            switch (encoding) {
                case 1:
                    this.mEncoding = 2;
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                    this.mEncoding = encoding;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid encoding " + encoding);
            }
            this.mPropertySetMask |= 1;
            return this;
        }

        public Builder setChannelMask(int channelMask) {
            if (channelMask == 0) {
                throw new IllegalArgumentException("Invalid zero channel mask");
            } else if (this.mChannelIndexMask == 0 || Integer.bitCount(channelMask) == Integer.bitCount(this.mChannelIndexMask)) {
                this.mChannelMask = channelMask;
                this.mPropertySetMask |= 4;
                return this;
            } else {
                throw new IllegalArgumentException("Mismatched channel count for mask " + Integer.toHexString(channelMask).toUpperCase());
            }
        }

        public Builder setChannelIndexMask(int channelIndexMask) {
            if (channelIndexMask == 0) {
                throw new IllegalArgumentException("Invalid zero channel index mask");
            } else if (this.mChannelMask == 0 || Integer.bitCount(channelIndexMask) == Integer.bitCount(this.mChannelMask)) {
                this.mChannelIndexMask = channelIndexMask;
                this.mPropertySetMask |= 8;
                return this;
            } else {
                throw new IllegalArgumentException("Mismatched channel count for index mask " + Integer.toHexString(channelIndexMask).toUpperCase());
            }
        }

        public Builder setSampleRate(int sampleRate) throws IllegalArgumentException {
            if ((sampleRate >= 4000 && sampleRate <= 192000) || sampleRate == 384000 || sampleRate == 0) {
                this.mSampleRate = sampleRate;
                this.mPropertySetMask |= 2;
                return this;
            }
            throw new IllegalArgumentException("Invalid sample rate " + sampleRate);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioFormat that = (AudioFormat) o;
        int i = this.mPropertySetMask;
        if (i != that.mPropertySetMask) {
            return false;
        }
        if (((i & 1) == 0 || this.mEncoding == that.mEncoding) && (((this.mPropertySetMask & 2) == 0 || this.mSampleRate == that.mSampleRate) && (((this.mPropertySetMask & 4) == 0 || this.mChannelMask == that.mChannelMask) && ((this.mPropertySetMask & 8) == 0 || this.mChannelIndexMask == that.mChannelIndexMask)))) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mPropertySetMask), Integer.valueOf(this.mSampleRate), Integer.valueOf(this.mEncoding), Integer.valueOf(this.mChannelMask), Integer.valueOf(this.mChannelIndexMask));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPropertySetMask);
        dest.writeInt(this.mEncoding);
        dest.writeInt(this.mSampleRate);
        dest.writeInt(this.mChannelMask);
        dest.writeInt(this.mChannelIndexMask);
    }

    private AudioFormat(Parcel in) {
        this(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
    }

    public String toString() {
        return new String("AudioFormat: props=" + this.mPropertySetMask + " enc=" + this.mEncoding + " chan=0x" + Integer.toHexString(this.mChannelMask).toUpperCase() + " chan_index=0x" + Integer.toHexString(this.mChannelIndexMask).toUpperCase() + " rate=" + this.mSampleRate);
    }

    public static String toDisplayName(int audioFormat) {
        if (audioFormat == 5) {
            return "Dolby Digital";
        }
        if (audioFormat == 6) {
            return "Dolby Digital Plus";
        }
        if (audioFormat == 7) {
            return "DTS";
        }
        if (audioFormat == 8) {
            return "DTS HD";
        }
        if (audioFormat == 10) {
            return "AAC";
        }
        if (audioFormat == 14) {
            return "Dolby TrueHD";
        }
        switch (audioFormat) {
            case 17:
                return "Dolby AC-4";
            case 18:
                return "Dolby Atmos in Dolby Digital Plus";
            case 19:
                return "Dolby MAT";
            default:
                return "Unknown surround sound format";
        }
    }
}
