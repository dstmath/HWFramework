package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
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
    public static final Creator<AudioFormat> CREATOR = null;
    public static final int ENCODING_AAC_HE_V1 = 11;
    public static final int ENCODING_AAC_HE_V2 = 12;
    public static final int ENCODING_AAC_LC = 10;
    public static final int ENCODING_AC3 = 5;
    public static final int ENCODING_DEFAULT = 1;
    public static final int ENCODING_DTS = 7;
    public static final int ENCODING_DTS_HD = 8;
    public static final int ENCODING_E_AC3 = 6;
    public static final int ENCODING_IEC61937 = 13;
    public static final int ENCODING_INVALID = 0;
    public static final int ENCODING_MP3 = 9;
    public static final int ENCODING_PCM_16BIT = 2;
    public static final int ENCODING_PCM_8BIT = 3;
    public static final int ENCODING_PCM_FLOAT = 4;
    public static final int SAMPLE_RATE_HZ_MAX = 192000;
    public static final int SAMPLE_RATE_HZ_MIN = 4000;
    public static final int SAMPLE_RATE_UNSPECIFIED = 0;
    private int mChannelIndexMask;
    private int mChannelMask;
    private int mEncoding;
    private int mPropertySetMask;
    private int mSampleRate;

    public static class Builder {
        private int mChannelIndexMask;
        private int mChannelMask;
        private int mEncoding;
        private int mPropertySetMask;
        private int mSampleRate;

        public Builder() {
            this.mEncoding = AudioFormat.ENCODING_INVALID;
            this.mSampleRate = AudioFormat.ENCODING_INVALID;
            this.mChannelMask = AudioFormat.ENCODING_INVALID;
            this.mChannelIndexMask = AudioFormat.ENCODING_INVALID;
            this.mPropertySetMask = AudioFormat.ENCODING_INVALID;
        }

        public Builder(AudioFormat af) {
            this.mEncoding = AudioFormat.ENCODING_INVALID;
            this.mSampleRate = AudioFormat.ENCODING_INVALID;
            this.mChannelMask = AudioFormat.ENCODING_INVALID;
            this.mChannelIndexMask = AudioFormat.ENCODING_INVALID;
            this.mPropertySetMask = AudioFormat.ENCODING_INVALID;
            this.mEncoding = af.mEncoding;
            this.mSampleRate = af.mSampleRate;
            this.mChannelMask = af.mChannelMask;
            this.mChannelIndexMask = af.mChannelIndexMask;
            this.mPropertySetMask = af.mPropertySetMask;
        }

        public AudioFormat build() {
            AudioFormat af = new AudioFormat(null);
            af.mEncoding = this.mEncoding;
            af.mSampleRate = this.mSampleRate;
            af.mChannelMask = this.mChannelMask;
            af.mChannelIndexMask = this.mChannelIndexMask;
            af.mPropertySetMask = this.mPropertySetMask;
            return af;
        }

        public Builder setEncoding(int encoding) throws IllegalArgumentException {
            switch (encoding) {
                case AudioFormat.ENCODING_DEFAULT /*1*/:
                    this.mEncoding = AudioFormat.ENCODING_PCM_16BIT;
                    break;
                case AudioFormat.ENCODING_PCM_16BIT /*2*/:
                case AudioFormat.ENCODING_PCM_8BIT /*3*/:
                case AudioFormat.ENCODING_PCM_FLOAT /*4*/:
                case AudioFormat.ENCODING_AC3 /*5*/:
                case AudioFormat.ENCODING_E_AC3 /*6*/:
                case AudioFormat.ENCODING_DTS /*7*/:
                case AudioFormat.ENCODING_DTS_HD /*8*/:
                case AudioFormat.ENCODING_IEC61937 /*13*/:
                    this.mEncoding = encoding;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid encoding " + encoding);
            }
            this.mPropertySetMask |= AudioFormat.ENCODING_DEFAULT;
            return this;
        }

        public Builder setChannelMask(int channelMask) {
            if (channelMask == 0) {
                throw new IllegalArgumentException("Invalid zero channel mask");
            } else if (this.mChannelIndexMask == 0 || Integer.bitCount(channelMask) == Integer.bitCount(this.mChannelIndexMask)) {
                this.mChannelMask = channelMask;
                this.mPropertySetMask |= AudioFormat.ENCODING_PCM_FLOAT;
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
                this.mPropertySetMask |= AudioFormat.ENCODING_DTS_HD;
                return this;
            } else {
                throw new IllegalArgumentException("Mismatched channel count for index mask " + Integer.toHexString(channelIndexMask).toUpperCase());
            }
        }

        public Builder setSampleRate(int sampleRate) throws IllegalArgumentException {
            if ((sampleRate < AudioFormat.SAMPLE_RATE_HZ_MIN || sampleRate > AudioFormat.SAMPLE_RATE_HZ_MAX) && sampleRate != 0) {
                throw new IllegalArgumentException("Invalid sample rate " + sampleRate);
            }
            this.mSampleRate = sampleRate;
            this.mPropertySetMask |= AudioFormat.ENCODING_PCM_16BIT;
            return this;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.AudioFormat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioFormat.<clinit>():void");
    }

    public static int inChannelMaskFromOutChannelMask(int outMask) throws IllegalArgumentException {
        if (outMask == ENCODING_DEFAULT) {
            throw new IllegalArgumentException("Illegal CHANNEL_OUT_DEFAULT channel mask for input.");
        }
        switch (channelCountFromOutChannelMask(outMask)) {
            case ENCODING_DEFAULT /*1*/:
                return CHANNEL_OUT_FRONT_CENTER;
            case ENCODING_PCM_16BIT /*2*/:
                return ENCODING_AAC_HE_V2;
            default:
                throw new IllegalArgumentException("Unsupported channel configuration for input.");
        }
    }

    public static int channelCountFromInChannelMask(int mask) {
        return Integer.bitCount(mask);
    }

    public static int channelCountFromOutChannelMask(int mask) {
        return Integer.bitCount(mask);
    }

    public static int convertChannelOutMaskToNativeMask(int javaMask) {
        return javaMask >> ENCODING_PCM_16BIT;
    }

    public static int convertNativeChannelMaskToOutMask(int nativeMask) {
        return nativeMask << ENCODING_PCM_16BIT;
    }

    public static int getBytesPerSample(int audioFormat) {
        switch (audioFormat) {
            case ENCODING_DEFAULT /*1*/:
            case ENCODING_PCM_16BIT /*2*/:
            case ENCODING_IEC61937 /*13*/:
                return ENCODING_PCM_16BIT;
            case ENCODING_PCM_8BIT /*3*/:
                return ENCODING_DEFAULT;
            case ENCODING_PCM_FLOAT /*4*/:
                return ENCODING_PCM_FLOAT;
            default:
                throw new IllegalArgumentException("Bad audio format " + audioFormat);
        }
    }

    public static boolean isValidEncoding(int audioFormat) {
        switch (audioFormat) {
            case ENCODING_PCM_16BIT /*2*/:
            case ENCODING_PCM_8BIT /*3*/:
            case ENCODING_PCM_FLOAT /*4*/:
            case ENCODING_AC3 /*5*/:
            case ENCODING_E_AC3 /*6*/:
            case ENCODING_DTS /*7*/:
            case ENCODING_DTS_HD /*8*/:
            case ENCODING_MP3 /*9*/:
            case ENCODING_AAC_LC /*10*/:
            case ENCODING_AAC_HE_V1 /*11*/:
            case ENCODING_AAC_HE_V2 /*12*/:
            case ENCODING_IEC61937 /*13*/:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPublicEncoding(int audioFormat) {
        switch (audioFormat) {
            case ENCODING_PCM_16BIT /*2*/:
            case ENCODING_PCM_8BIT /*3*/:
            case ENCODING_PCM_FLOAT /*4*/:
            case ENCODING_AC3 /*5*/:
            case ENCODING_E_AC3 /*6*/:
            case ENCODING_DTS /*7*/:
            case ENCODING_DTS_HD /*8*/:
            case ENCODING_IEC61937 /*13*/:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEncodingLinearPcm(int audioFormat) {
        switch (audioFormat) {
            case ENCODING_DEFAULT /*1*/:
            case ENCODING_PCM_16BIT /*2*/:
            case ENCODING_PCM_8BIT /*3*/:
            case ENCODING_PCM_FLOAT /*4*/:
                return true;
            case ENCODING_AC3 /*5*/:
            case ENCODING_E_AC3 /*6*/:
            case ENCODING_DTS /*7*/:
            case ENCODING_DTS_HD /*8*/:
            case ENCODING_MP3 /*9*/:
            case ENCODING_AAC_LC /*10*/:
            case ENCODING_AAC_HE_V1 /*11*/:
            case ENCODING_AAC_HE_V2 /*12*/:
            case ENCODING_IEC61937 /*13*/:
                return false;
            default:
                throw new IllegalArgumentException("Bad audio format " + audioFormat);
        }
    }

    public static boolean isEncodingLinearFrames(int audioFormat) {
        switch (audioFormat) {
            case ENCODING_DEFAULT /*1*/:
            case ENCODING_PCM_16BIT /*2*/:
            case ENCODING_PCM_8BIT /*3*/:
            case ENCODING_PCM_FLOAT /*4*/:
            case ENCODING_IEC61937 /*13*/:
                return true;
            case ENCODING_AC3 /*5*/:
            case ENCODING_E_AC3 /*6*/:
            case ENCODING_DTS /*7*/:
            case ENCODING_DTS_HD /*8*/:
            case ENCODING_MP3 /*9*/:
            case ENCODING_AAC_LC /*10*/:
            case ENCODING_AAC_HE_V1 /*11*/:
            case ENCODING_AAC_HE_V2 /*12*/:
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
        int size = ENCODING_INVALID;
        for (int i = ENCODING_INVALID; i < myCopy.length; i += ENCODING_DEFAULT) {
            if (isPublicEncoding(myCopy[i])) {
                if (size != i) {
                    myCopy[size] = myCopy[i];
                }
                size += ENCODING_DEFAULT;
            }
        }
        return Arrays.copyOf(myCopy, size);
    }

    public AudioFormat() {
        throw new UnsupportedOperationException("There is no valid usage of this constructor");
    }

    private AudioFormat(int ignoredArgument) {
    }

    private AudioFormat(int encoding, int sampleRate, int channelMask, int channelIndexMask) {
        this.mEncoding = encoding;
        this.mSampleRate = sampleRate;
        this.mChannelMask = channelMask;
        this.mChannelIndexMask = channelIndexMask;
        this.mPropertySetMask = 15;
    }

    public int getEncoding() {
        if ((this.mPropertySetMask & ENCODING_DEFAULT) == 0) {
            return ENCODING_INVALID;
        }
        return this.mEncoding;
    }

    public int getSampleRate() {
        return this.mSampleRate;
    }

    public int getChannelMask() {
        if ((this.mPropertySetMask & ENCODING_PCM_FLOAT) == 0) {
            return ENCODING_INVALID;
        }
        return this.mChannelMask;
    }

    public int getChannelIndexMask() {
        if ((this.mPropertySetMask & ENCODING_DTS_HD) == 0) {
            return ENCODING_INVALID;
        }
        return this.mChannelIndexMask;
    }

    public int getChannelCount() {
        int channelIndexCount = Integer.bitCount(getChannelIndexMask());
        int channelCount = channelCountFromOutChannelMask(getChannelMask());
        if (channelCount == 0) {
            return channelIndexCount;
        }
        if (channelCount == channelIndexCount || channelIndexCount == 0) {
            return channelCount;
        }
        return ENCODING_INVALID;
    }

    public int getPropertySetMask() {
        return this.mPropertySetMask;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioFormat that = (AudioFormat) o;
        if (this.mPropertySetMask != that.mPropertySetMask) {
            return false;
        }
        if (((this.mPropertySetMask & ENCODING_DEFAULT) != 0 && this.mEncoding != that.mEncoding) || (((this.mPropertySetMask & ENCODING_PCM_16BIT) != 0 && this.mSampleRate != that.mSampleRate) || ((this.mPropertySetMask & ENCODING_PCM_FLOAT) != 0 && this.mChannelMask != that.mChannelMask))) {
            z = false;
        } else if (!((this.mPropertySetMask & ENCODING_DTS_HD) == 0 || this.mChannelIndexMask == that.mChannelIndexMask)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        Object[] objArr = new Object[ENCODING_AC3];
        objArr[ENCODING_INVALID] = Integer.valueOf(this.mPropertySetMask);
        objArr[ENCODING_DEFAULT] = Integer.valueOf(this.mSampleRate);
        objArr[ENCODING_PCM_16BIT] = Integer.valueOf(this.mEncoding);
        objArr[ENCODING_PCM_8BIT] = Integer.valueOf(this.mChannelMask);
        objArr[ENCODING_PCM_FLOAT] = Integer.valueOf(this.mChannelIndexMask);
        return Objects.hash(objArr);
    }

    public int describeContents() {
        return ENCODING_INVALID;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPropertySetMask);
        dest.writeInt(this.mEncoding);
        dest.writeInt(this.mSampleRate);
        dest.writeInt(this.mChannelMask);
        dest.writeInt(this.mChannelIndexMask);
    }

    private AudioFormat(Parcel in) {
        this.mPropertySetMask = in.readInt();
        this.mEncoding = in.readInt();
        this.mSampleRate = in.readInt();
        this.mChannelMask = in.readInt();
        this.mChannelIndexMask = in.readInt();
    }

    public String toString() {
        return new String("AudioFormat: props=" + this.mPropertySetMask + " enc=" + this.mEncoding + " chan=0x" + Integer.toHexString(this.mChannelMask).toUpperCase() + " chan_index=0x" + Integer.toHexString(this.mChannelIndexMask).toUpperCase() + " rate=" + this.mSampleRate);
    }
}
