package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class TunerACInfo implements Parcelable {
    private static final int BIT_NUMBER_LEN = 56;
    public static final Creator<TunerACInfo> CREATOR = new Creator<TunerACInfo>() {
        public TunerACInfo createFromParcel(Parcel source) {
            return new TunerACInfo(source, null);
        }

        public TunerACInfo[] newArray(int size) {
            return new TunerACInfo[size];
        }
    };
    public static final String TAG = "TunerACInfo";
    private int mCurrentTime;
    private int mPageClass;
    private char[] mPgCls0AreaArr;
    private int mPgCls0AreaNum;
    private int mPgCls1Amount;
    private TunerClass1Info mPgCls1info0;
    private TunerClass1Info mPgCls1info1;
    private int mSignalId;
    private int mStaEndFlag;
    private int mUpdateFlag;

    /* synthetic */ TunerACInfo(Parcel in, TunerACInfo -this1) {
        this(in);
    }

    public TunerACInfo() {
        this.mPgCls0AreaArr = new char[56];
        this.mPgCls1info0 = new TunerClass1Info();
        this.mPgCls1info1 = new TunerClass1Info();
    }

    private TunerACInfo(Parcel in) {
        this.mPgCls0AreaArr = new char[56];
        readFromParcel(in);
    }

    public int getStaEndFlag() {
        return this.mStaEndFlag;
    }

    public void setStaEndFlag(int staEndFlag) {
        this.mStaEndFlag = staEndFlag;
    }

    public int getUpdateFlag() {
        return this.mUpdateFlag;
    }

    public void setUpdateFlag(int updateFlag) {
        this.mUpdateFlag = updateFlag;
    }

    public int getSignalId() {
        return this.mSignalId;
    }

    public void setSignalId(int signalId) {
        this.mSignalId = signalId;
    }

    public int getCurrentTime() {
        return this.mCurrentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.mCurrentTime = currentTime;
    }

    public int getPageClass() {
        return this.mPageClass;
    }

    public void setPageClass(int pageClass) {
        this.mPageClass = pageClass;
    }

    public char[] getPgCls0Area() {
        return (char[]) this.mPgCls0AreaArr.clone();
    }

    public void setPgCls0Area(char[] pgCls0AreaArr) {
        if (56 < pgCls0AreaArr.length || pgCls0AreaArr.length <= 0) {
            Log.e(TAG, "pgCls0AreaArr.length : " + pgCls0AreaArr.length);
        } else {
            this.mPgCls0AreaArr = (char[]) pgCls0AreaArr.clone();
        }
    }

    public int getPgCls0AreaNum() {
        return this.mPgCls0AreaNum;
    }

    public void setPgCls0AreaNum(int pgCls0AreaNum) {
        this.mPgCls0AreaNum = pgCls0AreaNum;
    }

    public int getPgCls1Amount() {
        return this.mPgCls1Amount;
    }

    public void setPgCls1Amount(int pgCls1Amount) {
        this.mPgCls1Amount = pgCls1Amount;
    }

    public TunerClass1Info getPgCls1info0() {
        return this.mPgCls1info0;
    }

    public void setPgCls1info0(TunerClass1Info pgCls1info0) {
        this.mPgCls1info0 = pgCls1info0;
    }

    public TunerClass1Info getPgCls1info1() {
        return this.mPgCls1info1;
    }

    public void setPgCls1info1(TunerClass1Info pgCls1info1) {
        this.mPgCls1info1 = pgCls1info1;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStaEndFlag);
        dest.writeInt(this.mUpdateFlag);
        dest.writeInt(this.mSignalId);
        dest.writeInt(this.mCurrentTime);
        dest.writeInt(this.mPageClass);
        dest.writeInt(this.mPgCls0AreaNum);
        dest.writeInt(this.mPgCls1Amount);
        dest.writeCharArray(this.mPgCls0AreaArr);
        dest.writeValue(this.mPgCls1info0);
        dest.writeValue(this.mPgCls1info1);
    }

    public final void readFromParcel(Parcel source) {
        this.mStaEndFlag = source.readInt();
        this.mUpdateFlag = source.readInt();
        this.mSignalId = source.readInt();
        this.mCurrentTime = source.readInt();
        this.mPageClass = source.readInt();
        source.readCharArray(this.mPgCls0AreaArr);
        this.mPgCls0AreaNum = source.readInt();
        this.mPgCls1Amount = source.readInt();
        this.mPgCls1info0 = (TunerClass1Info) source.readValue(TunerClass1Info.class.getClassLoader());
        this.mPgCls1info1 = (TunerClass1Info) source.readValue(TunerClass1Info.class.getClassLoader());
    }
}
