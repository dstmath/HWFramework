package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TunerCNInfo implements Parcelable {
    public static final Creator<TunerCNInfo> CREATOR = new Creator<TunerCNInfo>() {
        public TunerCNInfo createFromParcel(Parcel source) {
            return new TunerCNInfo(source, null);
        }

        public TunerCNInfo[] newArray(int size) {
            return new TunerCNInfo[size];
        }
    };
    public static final String TAG = "TunerChData";
    public static final byte VALUE_MONITOR_MODE_NO_WAIT = (byte) 1;
    public static final byte VALUE_MONITOR_MODE_WAIT_UPDATE = (byte) 0;
    private int mCn;
    private int mCnrd;
    private byte mFrmlck;
    private byte mSysF;
    private byte mTimeoutFlg;
    private byte mTunerMonitorGetmode;
    private TunerSyncInfo mTunerSyncInfo;
    private TunerTMCCInfo mTunerTMCCInfo;

    /* synthetic */ TunerCNInfo(Parcel in, TunerCNInfo -this1) {
        this(in);
    }

    public byte getTunerMonitorGetmode() {
        return this.mTunerMonitorGetmode;
    }

    public void setTunerMonitorGetmode(byte tunerMonitorGetmode) {
        this.mTunerMonitorGetmode = tunerMonitorGetmode;
    }

    public TunerSyncInfo getTunerSyncInfo() {
        return this.mTunerSyncInfo;
    }

    public void setTunerSyncInfo(TunerSyncInfo tunerSyncInfo) {
        this.mTunerSyncInfo = tunerSyncInfo;
    }

    public TunerTMCCInfo getTunerTMCCInfo() {
        return this.mTunerTMCCInfo;
    }

    public void setTunerTMCCInfo(TunerTMCCInfo tunerTMCCInfo) {
        this.mTunerTMCCInfo = tunerTMCCInfo;
    }

    public byte getTimeoutFlg() {
        return this.mTimeoutFlg;
    }

    public void setTimeoutFlg(byte timeoutFlg) {
        this.mTimeoutFlg = timeoutFlg;
    }

    public byte getSysF() {
        return this.mSysF;
    }

    public void setSysF(byte sysF) {
        this.mSysF = sysF;
    }

    public int getCnrd() {
        return this.mCnrd;
    }

    public void setCnrd(int cnrd) {
        this.mCnrd = cnrd;
    }

    public long getCn() {
        return (long) this.mCn;
    }

    public void setCn(int cn) {
        this.mCn = cn;
    }

    public int getFrmlck() {
        return this.mFrmlck;
    }

    public void setFrmlck(byte frmlck) {
        this.mFrmlck = frmlck;
    }

    public TunerCNInfo() {
        this.mTunerSyncInfo = new TunerSyncInfo();
        this.mTunerTMCCInfo = new TunerTMCCInfo();
    }

    private TunerCNInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mTunerMonitorGetmode);
        dest.writeByte(this.mTimeoutFlg);
        dest.writeByte(this.mSysF);
        dest.writeByte(this.mFrmlck);
        dest.writeInt(this.mCnrd);
        dest.writeInt(this.mCn);
        dest.writeValue(this.mTunerSyncInfo);
        dest.writeValue(this.mTunerTMCCInfo);
    }

    public final void readFromParcel(Parcel source) {
        this.mTunerMonitorGetmode = source.readByte();
        this.mTimeoutFlg = source.readByte();
        this.mSysF = source.readByte();
        this.mFrmlck = source.readByte();
        this.mCnrd = source.readInt();
        this.mCn = source.readInt();
        this.mTunerSyncInfo = (TunerSyncInfo) source.readValue(TunerSyncInfo.class.getClassLoader());
        this.mTunerTMCCInfo = (TunerTMCCInfo) source.readValue(TunerTMCCInfo.class.getClassLoader());
    }
}
