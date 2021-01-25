package ohos.bluetooth;

import java.util.Objects;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class A2dpCodecInfo implements Sequenceable {
    public static final int BITS_PER_SAMPLE_16 = 1;
    public static final int BITS_PER_SAMPLE_24 = 2;
    public static final int BITS_PER_SAMPLE_32 = 4;
    public static final int CHANNEL_MONO = 1;
    public static final int CHANNEL_STEREO = 2;
    public static final int CODEC_ID_AAC = 1;
    public static final int CODEC_ID_APTX = 2;
    public static final int CODEC_ID_APTX_HD = 3;
    public static final int CODEC_ID_LDAC = 4;
    public static final int CODEC_ID_LHDC = 5;
    public static final int CODEC_ID_LHDC_LL = 6;
    public static final int CODEC_ID_SBC = 0;
    public static final int CODEC_PRIORITY_DEFAULT = 0;
    public static final int CODEC_PRIORITY_DISABLED = -1;
    public static final int CODEC_PRIORITY_HIGHEST = 1000000;
    public static final int SAMPLE_RATE_176400 = 16;
    public static final int SAMPLE_RATE_192000 = 32;
    public static final int SAMPLE_RATE_44100 = 1;
    public static final int SAMPLE_RATE_48000 = 2;
    public static final int SAMPLE_RATE_88200 = 4;
    public static final int SAMPLE_RATE_96000 = 8;
    private int mBitsPerSample;
    private int mChannel;
    private int mCodecId;
    private int mCodecPriority;
    private long mReserveInfo1;
    private long mReserveInfo2;
    private long mReserveInfo3;
    private long mReserveInfo4;
    private int mSampleRate;

    public int getCodecId() {
        return this.mCodecId;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof A2dpCodecInfo)) {
            return false;
        }
        A2dpCodecInfo a2dpCodecInfo = (A2dpCodecInfo) obj;
        if (a2dpCodecInfo.mCodecId == this.mCodecId && a2dpCodecInfo.mCodecPriority == this.mCodecPriority && a2dpCodecInfo.mSampleRate == this.mSampleRate && a2dpCodecInfo.mBitsPerSample == this.mBitsPerSample && a2dpCodecInfo.mChannel == this.mChannel && a2dpCodecInfo.mReserveInfo1 == this.mReserveInfo1 && a2dpCodecInfo.mReserveInfo2 == this.mReserveInfo2 && a2dpCodecInfo.mReserveInfo3 == this.mReserveInfo3 && a2dpCodecInfo.mReserveInfo4 == this.mReserveInfo4) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mCodecId), Integer.valueOf(this.mCodecPriority), Integer.valueOf(this.mSampleRate), Integer.valueOf(this.mBitsPerSample), Integer.valueOf(this.mChannel), Long.valueOf(this.mReserveInfo1), Long.valueOf(this.mReserveInfo2), Long.valueOf(this.mReserveInfo3), Long.valueOf(this.mReserveInfo4));
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.mCodecId);
        parcel.writeInt(this.mCodecPriority);
        parcel.writeInt(this.mSampleRate);
        parcel.writeInt(this.mBitsPerSample);
        parcel.writeInt(this.mChannel);
        parcel.writeLong(this.mReserveInfo1);
        parcel.writeLong(this.mReserveInfo2);
        parcel.writeLong(this.mReserveInfo3);
        parcel.writeLong(this.mReserveInfo4);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.mCodecId = parcel.readInt();
        this.mCodecPriority = parcel.readInt();
        this.mSampleRate = parcel.readInt();
        this.mBitsPerSample = parcel.readInt();
        this.mChannel = parcel.readInt();
        this.mReserveInfo1 = parcel.readLong();
        this.mReserveInfo2 = parcel.readLong();
        this.mReserveInfo3 = parcel.readLong();
        this.mReserveInfo4 = parcel.readLong();
        return true;
    }
}
