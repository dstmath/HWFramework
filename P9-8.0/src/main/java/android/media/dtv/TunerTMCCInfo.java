package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TunerTMCCInfo implements Parcelable {
    public static final Creator<TunerTMCCInfo> CREATOR = new Creator<TunerTMCCInfo>() {
        public TunerTMCCInfo createFromParcel(Parcel source) {
            return new TunerTMCCInfo(source, null);
        }

        public TunerTMCCInfo[] newArray(int size) {
            return new TunerTMCCInfo[size];
        }
    };
    public static final String TAG = "TunerTMCCInfo";
    private byte mCntdwn;
    private byte mCr0;
    private byte mCr1;
    private byte mCr2;
    private byte mEmgFlg;
    private byte mFfts;
    private byte mFrmlck;
    private byte mInterLen0;
    private byte mInterLen1;
    private byte mInterLen2;
    private byte mMap0;
    private byte mMap1;
    private byte mMap2;
    private byte mPartFlg;
    private byte mPhcor;
    private byte mSeg0;
    private byte mSeg1;
    private byte mSeg2;
    private byte mSystemIdtf;
    private TunerSyncInfo mTunerSyncInfo;

    /* synthetic */ TunerTMCCInfo(Parcel in, TunerTMCCInfo -this1) {
        this(in);
    }

    public TunerTMCCInfo() {
        this.mTunerSyncInfo = new TunerSyncInfo();
    }

    private TunerTMCCInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.mTunerSyncInfo);
        dest.writeByte(this.mSystemIdtf);
        dest.writeByte(this.mCntdwn);
        dest.writeByte(this.mEmgFlg);
        dest.writeByte(this.mPartFlg);
        dest.writeByte(this.mMap0);
        dest.writeByte(this.mMap1);
        dest.writeByte(this.mMap2);
        dest.writeByte(this.mCr0);
        dest.writeByte(this.mCr1);
        dest.writeByte(this.mCr2);
        dest.writeByte(this.mInterLen0);
        dest.writeByte(this.mInterLen1);
        dest.writeByte(this.mInterLen2);
        dest.writeByte(this.mSeg0);
        dest.writeByte(this.mSeg1);
        dest.writeByte(this.mSeg2);
        dest.writeByte(this.mPhcor);
        dest.writeByte(this.mFfts);
        dest.writeByte(this.mFrmlck);
    }

    public final void readFromParcel(Parcel source) {
        this.mTunerSyncInfo = (TunerSyncInfo) source.readValue(TunerSyncInfo.class.getClassLoader());
        this.mSystemIdtf = source.readByte();
        this.mCntdwn = source.readByte();
        this.mEmgFlg = source.readByte();
        this.mPartFlg = source.readByte();
        this.mMap0 = source.readByte();
        this.mMap1 = source.readByte();
        this.mMap2 = source.readByte();
        this.mCr0 = source.readByte();
        this.mCr1 = source.readByte();
        this.mCr2 = source.readByte();
        this.mInterLen0 = source.readByte();
        this.mInterLen1 = source.readByte();
        this.mInterLen2 = source.readByte();
        this.mSeg0 = source.readByte();
        this.mSeg1 = source.readByte();
        this.mSeg2 = source.readByte();
        this.mPhcor = source.readByte();
        this.mFfts = source.readByte();
        this.mFrmlck = source.readByte();
    }

    public TunerSyncInfo getTunerSyncInfo() {
        return this.mTunerSyncInfo;
    }

    public void setTunerSyncInfo(TunerSyncInfo tunerSyncInfo) {
        this.mTunerSyncInfo = tunerSyncInfo;
    }

    public byte getSystemIdtf() {
        return this.mSystemIdtf;
    }

    public void setSystemIdtf(byte systemIdtf) {
        this.mSystemIdtf = systemIdtf;
    }

    public byte getCntdwn() {
        return this.mCntdwn;
    }

    public void setCntdwn(byte cntdwn) {
        this.mCntdwn = cntdwn;
    }

    public byte getEmgFlg() {
        return this.mEmgFlg;
    }

    public void setEmgFlg(byte emgFlg) {
        this.mEmgFlg = emgFlg;
    }

    public byte getPartFlg() {
        return this.mPartFlg;
    }

    public void setPartFlg(byte partFlg) {
        this.mPartFlg = partFlg;
    }

    public byte getMap0() {
        return this.mMap0;
    }

    public void setMap0(byte map0) {
        this.mMap0 = map0;
    }

    public byte getMap1() {
        return this.mMap1;
    }

    public void setMap1(byte map1) {
        this.mMap1 = map1;
    }

    public byte getMap2() {
        return this.mMap2;
    }

    public void setMap2(byte map2) {
        this.mMap2 = map2;
    }

    public byte getCr0() {
        return this.mCr0;
    }

    public void setCr0(byte cr0) {
        this.mCr0 = cr0;
    }

    public byte getCr1() {
        return this.mCr1;
    }

    public void setCr1(byte cr1) {
        this.mCr1 = cr1;
    }

    public byte getCr2() {
        return this.mCr2;
    }

    public void setCr2(byte cr2) {
        this.mCr2 = cr2;
    }

    public byte getInterLen0() {
        return this.mInterLen0;
    }

    public void setInterLen0(byte interLen0) {
        this.mInterLen0 = interLen0;
    }

    public byte getInterLen1() {
        return this.mInterLen1;
    }

    public void setInterLen1(byte interLen1) {
        this.mInterLen1 = interLen1;
    }

    public byte getInterLen2() {
        return this.mInterLen2;
    }

    public void setInterLen2(byte interLen2) {
        this.mInterLen2 = interLen2;
    }

    public byte getSeg0() {
        return this.mSeg0;
    }

    public void setSeg0(byte seg0) {
        this.mSeg0 = seg0;
    }

    public byte getSeg1() {
        return this.mSeg1;
    }

    public void setSeg1(byte seg1) {
        this.mSeg1 = seg1;
    }

    public byte getSeg2() {
        return this.mSeg2;
    }

    public void setSeg2(byte seg2) {
        this.mSeg2 = seg2;
    }

    public byte getPhcor() {
        return this.mPhcor;
    }

    public void setPhcor(byte phcor) {
        this.mPhcor = phcor;
    }

    public byte getFfts() {
        return this.mFfts;
    }

    public void setFfts(byte ffts) {
        this.mFfts = ffts;
    }

    public byte getFrmlck() {
        return this.mFrmlck;
    }

    public void setFrmlck(byte frmlck) {
        this.mFrmlck = frmlck;
    }
}
