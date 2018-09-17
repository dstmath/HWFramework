package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TunerBperInfo implements Parcelable {
    public static final byte BBER_CIRCUIT_BPER1 = (byte) 0;
    public static final byte BBER_CIRCUIT_BPER2 = (byte) 1;
    public static final byte BPERTYPE_BERD = (byte) 2;
    public static final byte BPERTYPE_BERV = (byte) 0;
    public static final byte BPERTYPE_PER = (byte) 1;
    public static final Creator<TunerBperInfo> CREATOR = new Creator<TunerBperInfo>() {
        public TunerBperInfo createFromParcel(Parcel source) {
            return new TunerBperInfo(source);
        }

        public TunerBperInfo[] newArray(int size) {
            return new TunerBperInfo[size];
        }
    };
    public static final byte MONITOR_MODE_NO_WAIT = (byte) 1;
    public static final byte MONITOR_MODE_WAIT_UPDATE = (byte) 0;
    private byte mBberCircuit;
    private int mBerlenrd0;
    private int mBerlenrd1;
    private int mBerlenrd2;
    private int mBerrd0;
    private int mBerrd1;
    private int mBerrd2;
    private byte mBperType;
    private byte mFrmlck;
    private byte mMap0;
    private byte mMap1;
    private byte mMap2;
    private byte mTimeout0;
    private byte mTimeout1;
    private byte mTimeout2;
    private TunerBperPara mTunerBperPara;
    private byte mTunerMonitorGetmode;
    private TunerSyncInfo mTunerSyncInfo;
    private TunerTMCCInfo mTunerTMCCInfo;
    private int mVal0;
    private int mVal1;
    private int mVal2;

    public TunerBperInfo() {
        this.mTunerSyncInfo = new TunerSyncInfo();
        this.mTunerTMCCInfo = new TunerTMCCInfo();
        this.mTunerBperPara = new TunerBperPara();
    }

    public TunerBperInfo(Parcel source) {
        readFromParcel(source);
    }

    public byte getTunerMonitorGetmode() {
        return this.mTunerMonitorGetmode;
    }

    public void setTunerMonitorGetmode(byte tunerMonitorGetmode) {
        this.mTunerMonitorGetmode = tunerMonitorGetmode;
    }

    public byte getBberCircuit() {
        return this.mBberCircuit;
    }

    public void setBberCircuit(byte bberCircuit) {
        this.mBberCircuit = bberCircuit;
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

    public byte getBperType() {
        return this.mBperType;
    }

    public void setBperType(byte bperType) {
        this.mBperType = bperType;
    }

    public byte getFrmlck() {
        return this.mFrmlck;
    }

    public void setFrmlck(byte frmlck) {
        this.mFrmlck = frmlck;
    }

    public byte getTimeout0() {
        return this.mTimeout0;
    }

    public void setTimeout0(byte timeout0) {
        this.mTimeout0 = timeout0;
    }

    public byte getTimeout1() {
        return this.mTimeout1;
    }

    public void setTimeout1(byte timeout1) {
        this.mTimeout1 = timeout1;
    }

    public byte getTimeout2() {
        return this.mTimeout2;
    }

    public void setTimeout2(byte timeout2) {
        this.mTimeout2 = timeout2;
    }

    public int getBerrd0() {
        return this.mBerrd0;
    }

    public void setBerrd0(int berrd0) {
        this.mBerrd0 = berrd0;
    }

    public int getBerrd1() {
        return this.mBerrd1;
    }

    public void setBerrd1(int berrd1) {
        this.mBerrd1 = berrd1;
    }

    public int getBerrd2() {
        return this.mBerrd2;
    }

    public void setBerrd2(int berrd2) {
        this.mBerrd2 = berrd2;
    }

    public int getBerlenrd0() {
        return this.mBerlenrd0;
    }

    public void setBerlenrd0(int berlenrd0) {
        this.mBerlenrd0 = berlenrd0;
    }

    public int getBerlenrd1() {
        return this.mBerlenrd1;
    }

    public void setBerlenrd1(int berlenrd1) {
        this.mBerlenrd1 = berlenrd1;
    }

    public int getBerlenrd2() {
        return this.mBerlenrd2;
    }

    public void setBerlenrd2(int berlenrd2) {
        this.mBerlenrd2 = berlenrd2;
    }

    public int getVal0() {
        return this.mVal0;
    }

    public void setVal0(int val0) {
        this.mVal0 = val0;
    }

    public int getVal1() {
        return this.mVal1;
    }

    public void setVal1(int val1) {
        this.mVal1 = val1;
    }

    public int getVal2() {
        return this.mVal2;
    }

    public void setVal2(int val2) {
        this.mVal2 = val2;
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

    public TunerBperPara getTunerBperPara() {
        return this.mTunerBperPara;
    }

    public void setTunerBperPara(TunerBperPara tunerBperPara) {
        this.mTunerBperPara = tunerBperPara;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mTunerMonitorGetmode);
        dest.writeByte(this.mBberCircuit);
        dest.writeByte(this.mTimeout0);
        dest.writeByte(this.mTimeout1);
        dest.writeByte(this.mTimeout2);
        dest.writeByte(this.mBperType);
        dest.writeInt(this.mVal0);
        dest.writeInt(this.mVal1);
        dest.writeInt(this.mVal2);
        dest.writeByte(this.mFrmlck);
        dest.writeByte(this.mMap0);
        dest.writeByte(this.mMap1);
        dest.writeByte(this.mMap2);
        dest.writeInt(this.mBerrd0);
        dest.writeInt(this.mBerrd1);
        dest.writeInt(this.mBerrd2);
        dest.writeInt(this.mBerlenrd0);
        dest.writeInt(this.mBerlenrd1);
        dest.writeInt(this.mBerlenrd2);
        dest.writeValue(this.mTunerSyncInfo);
        dest.writeValue(this.mTunerTMCCInfo);
        dest.writeValue(this.mTunerBperPara);
    }

    public final void readFromParcel(Parcel source) {
        this.mTunerMonitorGetmode = source.readByte();
        this.mBberCircuit = source.readByte();
        this.mTimeout0 = source.readByte();
        this.mTimeout1 = source.readByte();
        this.mTimeout2 = source.readByte();
        this.mBperType = source.readByte();
        this.mVal0 = source.readInt();
        this.mVal1 = source.readInt();
        this.mVal2 = source.readInt();
        this.mFrmlck = source.readByte();
        this.mMap0 = source.readByte();
        this.mMap1 = source.readByte();
        this.mMap2 = source.readByte();
        this.mBerrd0 = source.readInt();
        this.mBerrd1 = source.readInt();
        this.mBerrd2 = source.readInt();
        this.mBerlenrd0 = source.readInt();
        this.mBerlenrd1 = source.readInt();
        this.mBerlenrd2 = source.readInt();
        this.mTunerSyncInfo = (TunerSyncInfo) source.readValue(TunerSyncInfo.class.getClassLoader());
        this.mTunerTMCCInfo = (TunerTMCCInfo) source.readValue(TunerTMCCInfo.class.getClassLoader());
        this.mTunerBperPara = (TunerBperPara) source.readValue(TunerBperPara.class.getClassLoader());
    }
}
