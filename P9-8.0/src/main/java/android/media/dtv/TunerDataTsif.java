package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TunerDataTsif implements Parcelable {
    public static final Creator<TunerDataTsif> CREATOR = new Creator<TunerDataTsif>() {
        public TunerDataTsif createFromParcel(Parcel source) {
            return new TunerDataTsif(source, null);
        }

        public TunerDataTsif[] newArray(int size) {
            return new TunerDataTsif[size];
        }
    };
    public static final String TAG = "TunerDataTsif";
    public static final int TUNER_DRV_TS_ADDFEC = 1;
    public static final int TUNER_DRV_TS_NORMAL = 0;
    public static final int TUNER_DRV_TS_TSTAMP = 2;
    private int mAesMode;
    private int mDwind0;
    private int mDwind1;
    private int mDwind2;
    private int mSpiCalibrationFlag;
    private int mSpiTsBitPerWord;
    private int mThl0;
    private int mThl1;
    private int mThl2;
    private int mTsPktType;

    /* synthetic */ TunerDataTsif(Parcel in, TunerDataTsif -this1) {
        this(in);
    }

    private TunerDataTsif(Parcel in) {
        this.mDwind0 = in.readInt();
        this.mDwind1 = in.readInt();
        this.mDwind2 = in.readInt();
        this.mThl0 = in.readInt();
        this.mThl1 = in.readInt();
        this.mThl2 = in.readInt();
        this.mTsPktType = in.readInt();
        this.mAesMode = in.readInt();
        this.mSpiTsBitPerWord = in.readInt();
        this.mSpiCalibrationFlag = in.readInt();
    }

    public int getDwind0() {
        return this.mDwind0;
    }

    public void setDwind0(int dwind) {
        this.mDwind0 = dwind;
    }

    public int getDwind1() {
        return this.mDwind1;
    }

    public void setDwind1(int dwind) {
        this.mDwind1 = dwind;
    }

    public int getDwind2() {
        return this.mDwind2;
    }

    public void setDwind2(int dwind) {
        this.mDwind2 = dwind;
    }

    public int getThl0() {
        return this.mThl0;
    }

    public void setThl0(int thl) {
        this.mThl0 = thl;
    }

    public int getThl1() {
        return this.mThl1;
    }

    public void setThl1(int thl) {
        this.mThl1 = thl;
    }

    public int getThl2() {
        return this.mThl2;
    }

    public void setThl2(int thl) {
        this.mThl2 = thl;
    }

    public int getTsPktType() {
        return this.mTsPktType;
    }

    public void setTsPktType(int tsPktType) {
        this.mTsPktType = tsPktType;
    }

    public int getAesMode() {
        return this.mAesMode;
    }

    public void setAesMode(int aesMode) {
        this.mAesMode = aesMode;
    }

    public int getSpiTsBitPerWord() {
        return this.mSpiTsBitPerWord;
    }

    public void setSpiTsBitPerWord(int spiTsBitPerWord) {
        this.mSpiTsBitPerWord = spiTsBitPerWord;
    }

    public int getSpiCalibrationFlag() {
        return this.mSpiCalibrationFlag;
    }

    public void setSpiCalibrationFlag(int spiCalibrationFlag) {
        this.mSpiCalibrationFlag = spiCalibrationFlag;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDwind0);
        dest.writeInt(this.mDwind1);
        dest.writeInt(this.mDwind2);
        dest.writeInt(this.mThl0);
        dest.writeInt(this.mThl1);
        dest.writeInt(this.mThl2);
        dest.writeInt(this.mTsPktType);
        dest.writeInt(this.mAesMode);
        dest.writeInt(this.mSpiTsBitPerWord);
        dest.writeInt(this.mSpiCalibrationFlag);
    }

    public String toString() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("[mAesMode : ").append(this.mAesMode);
        sBuilder.append(",mDwind0 : ").append(this.mDwind0);
        sBuilder.append(",mDwind1 : ").append(this.mDwind1);
        sBuilder.append(",mDwind2 : ").append(this.mDwind2);
        sBuilder.append(",mSpiCalibrationFlag : ").append(this.mSpiCalibrationFlag);
        sBuilder.append(",mSpiTsBitPerWord : ").append(this.mSpiTsBitPerWord);
        sBuilder.append(",mThl0 : ").append(this.mThl0);
        sBuilder.append(",mThl1 : ").append(this.mThl1);
        sBuilder.append(",mThl2 : ").append(this.mThl2);
        sBuilder.append(",mTsPktType : ").append(this.mTsPktType).append("]");
        return sBuilder.toString();
    }
}
