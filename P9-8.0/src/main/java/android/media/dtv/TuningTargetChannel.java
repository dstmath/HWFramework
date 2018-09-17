package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TuningTargetChannel implements Parcelable {
    public static final Creator<TuningTargetChannel> CREATOR = new Creator<TuningTargetChannel>() {
        public TuningTargetChannel createFromParcel(Parcel source) {
            return new TuningTargetChannel(source, null);
        }

        public TuningTargetChannel[] newArray(int size) {
            return new TuningTargetChannel[size];
        }
    };
    public static final String TAG = "TuningTargetChannel";
    public static final short TUNERBAND_CF = (short) 18;
    public static final short TUNERBAND_CF1 = (short) 20;
    public static final short TUNERBAND_CHBAND_NUM = (short) 21;
    public static final short TUNERBAND_CS = (short) 19;
    public static final short TUNERBAND_D = (short) 10;
    public static final short TUNERBAND_DF = (short) 13;
    public static final short TUNERBAND_DS = (short) 16;
    public static final short TUNERBAND_K = (short) 9;
    public static final short TUNERBAND_KF = (short) 12;
    public static final short TUNERBAND_KS = (short) 15;
    public static final short TUNERBAND_S = (short) 11;
    public static final short TUNERBAND_SF = (short) 14;
    public static final short TUNERBAND_SS = (short) 17;
    public static final short TUNERBAND_UF = (short) 0;
    public static final short TUNERBAND_UF1 = (short) 2;
    public static final short TUNERBAND_US = (short) 1;
    public static final short TUNERBAND_VLF = (short) 6;
    public static final short TUNERBAND_VLF1 = (short) 8;
    public static final short TUNERBAND_VLS = (short) 7;
    public static final short TUNERBAND_VMF = (short) 3;
    public static final short TUNERBAND_VMF1 = (short) 5;
    public static final short TUNERBAND_VMS = (short) 4;
    private int mChannelRecordIndex;
    private long mDetectSyncTime;
    private int mDtvSys;
    private int mIntervalTime;
    private int mTargetChannelNum;
    private int mTunerBand;
    private TunerTuneState mTunerTunestat;

    /* synthetic */ TuningTargetChannel(Parcel in, TuningTargetChannel -this1) {
        this(in);
    }

    public int getTargetChannelNum() {
        return this.mTargetChannelNum;
    }

    public void setTargetChannelNum(int targetChannelNum) {
        this.mTargetChannelNum = targetChannelNum;
    }

    public int getTunerBand() {
        return this.mTunerBand;
    }

    public void setTunerBand(int tunerBand) {
        this.mTunerBand = tunerBand;
    }

    public int getIntervalTime() {
        return this.mIntervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.mIntervalTime = intervalTime;
    }

    public int getChannelRecordIndex() {
        return this.mChannelRecordIndex;
    }

    public void setChannelRecordIndex(int channelRecordIndex) {
        this.mChannelRecordIndex = channelRecordIndex;
    }

    public long getDetectSyncTime() {
        return this.mDetectSyncTime;
    }

    public void setDetectSyncTime(long detectSyncTime) {
        this.mDetectSyncTime = detectSyncTime;
    }

    public int getDtvSys() {
        return this.mDtvSys;
    }

    public void setDtvSys(int dtvSys) {
        this.mDtvSys = dtvSys;
    }

    public TunerTuneState getTunerTunestat() {
        return this.mTunerTunestat;
    }

    public void setTunerTunestat(TunerTuneState tunerTunestat) {
        this.mTunerTunestat = tunerTunestat;
    }

    private TuningTargetChannel(Parcel in) {
        this.mTargetChannelNum = in.readInt();
        this.mTunerBand = in.readInt();
        this.mIntervalTime = in.readInt();
        this.mChannelRecordIndex = in.readInt();
        this.mDtvSys = in.readInt();
        this.mDetectSyncTime = in.readLong();
        this.mTunerTunestat = (TunerTuneState) in.readValue(TunerTuneState.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTargetChannelNum);
        dest.writeInt(this.mTunerBand);
        dest.writeInt(this.mIntervalTime);
        dest.writeInt(this.mChannelRecordIndex);
        dest.writeLong(this.mDetectSyncTime);
        dest.writeInt(this.mDtvSys);
        dest.writeValue(this.mTunerTunestat);
    }
}
