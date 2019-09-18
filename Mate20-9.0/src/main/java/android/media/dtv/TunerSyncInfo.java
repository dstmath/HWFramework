package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;

public class TunerSyncInfo implements Parcelable {
    public static final Parcelable.Creator<TunerSyncInfo> CREATOR = new Parcelable.Creator<TunerSyncInfo>() {
        public TunerSyncInfo createFromParcel(Parcel source) {
            return new TunerSyncInfo(source);
        }

        public TunerSyncInfo[] newArray(int size) {
            return new TunerSyncInfo[size];
        }
    };
    public static final String TAG = "TunerSyncInfo";
    private byte mDtvSys;
    private byte mEmgFlg;
    private byte mFfts;
    private byte mFrameSyncState;
    private byte mFreqSyncState;
    private byte mGuardIntervalLength;
    private byte mPartFlg;
    private byte mSegId;
    private byte mSyncSequenceState;
    private byte mTMCCDecodingState;

    public byte getDtvSys() {
        return this.mDtvSys;
    }

    public void setDtvSys(byte dtvSys) {
        this.mDtvSys = dtvSys;
    }

    public byte getFfts() {
        return this.mFfts;
    }

    public void setFfts(byte ffts) {
        this.mFfts = ffts;
    }

    public byte getGuardIntervalLength() {
        return this.mGuardIntervalLength;
    }

    public void setGuardIntervalLength(byte guardIntervalLength) {
        this.mGuardIntervalLength = guardIntervalLength;
    }

    public byte getSegId() {
        return this.mSegId;
    }

    public void setSegId(byte segId) {
        this.mSegId = segId;
    }

    public byte getSyncSequenceState() {
        return this.mSyncSequenceState;
    }

    public void setSyncSequenceState(byte syncSequenceState) {
        this.mSyncSequenceState = syncSequenceState;
    }

    public byte getFreqSyncState() {
        return this.mFreqSyncState;
    }

    public void setFreqSyncState(byte freqSyncState) {
        this.mFreqSyncState = freqSyncState;
    }

    public byte getFrameSyncState() {
        return this.mFrameSyncState;
    }

    public void setFrameSyncState(byte frameSyncState) {
        this.mFrameSyncState = frameSyncState;
    }

    public byte getTMCCDecodingState() {
        return this.mTMCCDecodingState;
    }

    public void setTMCCDecodingState(byte tMCCDecodingState) {
        this.mTMCCDecodingState = tMCCDecodingState;
    }

    public byte getPartFlg() {
        return this.mPartFlg;
    }

    public void setPartFlg(byte partFlg) {
        this.mPartFlg = partFlg;
    }

    public byte getEmgFlg() {
        return this.mEmgFlg;
    }

    public void setEmgFlg(byte emgFlg) {
        this.mEmgFlg = emgFlg;
    }

    public TunerSyncInfo() {
    }

    private TunerSyncInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mDtvSys);
        dest.writeByte(this.mFfts);
        dest.writeByte(this.mGuardIntervalLength);
        dest.writeByte(this.mSegId);
        dest.writeByte(this.mSyncSequenceState);
        dest.writeByte(this.mFreqSyncState);
        dest.writeByte(this.mFrameSyncState);
        dest.writeByte(this.mTMCCDecodingState);
        dest.writeByte(this.mPartFlg);
        dest.writeByte(this.mEmgFlg);
    }

    public final void readFromParcel(Parcel source) {
        this.mDtvSys = source.readByte();
        this.mFfts = source.readByte();
        this.mGuardIntervalLength = source.readByte();
        this.mSegId = source.readByte();
        this.mSyncSequenceState = source.readByte();
        this.mFreqSyncState = source.readByte();
        this.mFrameSyncState = source.readByte();
        this.mTMCCDecodingState = source.readByte();
        this.mPartFlg = source.readByte();
        this.mEmgFlg = source.readByte();
    }
}
