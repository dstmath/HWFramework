package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;

public class TuningTargetChannel implements Parcelable {
    public static final Parcelable.Creator<TuningTargetChannel> CREATOR = new Parcelable.Creator<TuningTargetChannel>() {
        public TuningTargetChannel createFromParcel(Parcel source) {
            return new TuningTargetChannel(source);
        }

        public TuningTargetChannel[] newArray(int size) {
            return new TuningTargetChannel[size];
        }
    };
    public static final String TAG = "TuningTargetChannel";
    public static final short TUNERBAND_CF = 18;
    public static final short TUNERBAND_CF1 = 20;
    public static final short TUNERBAND_CHBAND_NUM = 21;
    public static final short TUNERBAND_CS = 19;
    public static final short TUNERBAND_D = 10;
    public static final short TUNERBAND_DF = 13;
    public static final short TUNERBAND_DS = 16;
    public static final short TUNERBAND_K = 9;
    public static final short TUNERBAND_KF = 12;
    public static final short TUNERBAND_KS = 15;
    public static final short TUNERBAND_S = 11;
    public static final short TUNERBAND_SF = 14;
    public static final short TUNERBAND_SS = 17;
    public static final short TUNERBAND_UF = 0;
    public static final short TUNERBAND_UF1 = 2;
    public static final short TUNERBAND_US = 1;
    public static final short TUNERBAND_VLF = 6;
    public static final short TUNERBAND_VLF1 = 8;
    public static final short TUNERBAND_VLS = 7;
    public static final short TUNERBAND_VMF = 3;
    public static final short TUNERBAND_VMF1 = 5;
    public static final short TUNERBAND_VMS = 4;
    private int mChannelRecordIndex;
    private long mDetectSyncTime;
    private int mDtvSys;
    private int mIntervalTime;
    private int mTargetChannelNum;
    private int mTunerBand;
    private TunerTuneState mTunerTunestat;

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
