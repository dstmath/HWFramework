package android.net;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;

public class MobileLinkQualityInfo extends LinkQualityInfo {
    private int mCdmaDbm = Integer.MAX_VALUE;
    private int mCdmaEcio = Integer.MAX_VALUE;
    private int mEvdoDbm = Integer.MAX_VALUE;
    private int mEvdoEcio = Integer.MAX_VALUE;
    private int mEvdoSnr = Integer.MAX_VALUE;
    private int mGsmErrorRate = Integer.MAX_VALUE;
    private int mLteCqi = Integer.MAX_VALUE;
    private int mLteRsrp = Integer.MAX_VALUE;
    private int mLteRsrq = Integer.MAX_VALUE;
    private int mLteRssnr = Integer.MAX_VALUE;
    private int mLteSignalStrength = Integer.MAX_VALUE;
    private int mMobileNetworkType = Integer.MAX_VALUE;
    private int mRssi = Integer.MAX_VALUE;

    @Override // android.net.LinkQualityInfo, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 3);
        dest.writeInt(this.mMobileNetworkType);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mGsmErrorRate);
        dest.writeInt(this.mCdmaDbm);
        dest.writeInt(this.mCdmaEcio);
        dest.writeInt(this.mEvdoDbm);
        dest.writeInt(this.mEvdoEcio);
        dest.writeInt(this.mEvdoSnr);
        dest.writeInt(this.mLteSignalStrength);
        dest.writeInt(this.mLteRsrp);
        dest.writeInt(this.mLteRsrq);
        dest.writeInt(this.mLteRssnr);
        dest.writeInt(this.mLteCqi);
    }

    public static MobileLinkQualityInfo createFromParcelBody(Parcel in) {
        MobileLinkQualityInfo li = new MobileLinkQualityInfo();
        li.initializeFromParcel(in);
        li.mMobileNetworkType = in.readInt();
        li.mRssi = in.readInt();
        li.mGsmErrorRate = in.readInt();
        li.mCdmaDbm = in.readInt();
        li.mCdmaEcio = in.readInt();
        li.mEvdoDbm = in.readInt();
        li.mEvdoEcio = in.readInt();
        li.mEvdoSnr = in.readInt();
        li.mLteSignalStrength = in.readInt();
        li.mLteRsrp = in.readInt();
        li.mLteRsrq = in.readInt();
        li.mLteRssnr = in.readInt();
        li.mLteCqi = in.readInt();
        return li;
    }

    @UnsupportedAppUsage
    public int getMobileNetworkType() {
        return this.mMobileNetworkType;
    }

    @UnsupportedAppUsage
    public void setMobileNetworkType(int mobileNetworkType) {
        this.mMobileNetworkType = mobileNetworkType;
    }

    public int getRssi() {
        return this.mRssi;
    }

    @UnsupportedAppUsage
    public void setRssi(int Rssi) {
        this.mRssi = Rssi;
    }

    public int getGsmErrorRate() {
        return this.mGsmErrorRate;
    }

    @UnsupportedAppUsage
    public void setGsmErrorRate(int gsmErrorRate) {
        this.mGsmErrorRate = gsmErrorRate;
    }

    public int getCdmaDbm() {
        return this.mCdmaDbm;
    }

    @UnsupportedAppUsage
    public void setCdmaDbm(int cdmaDbm) {
        this.mCdmaDbm = cdmaDbm;
    }

    public int getCdmaEcio() {
        return this.mCdmaEcio;
    }

    @UnsupportedAppUsage
    public void setCdmaEcio(int cdmaEcio) {
        this.mCdmaEcio = cdmaEcio;
    }

    public int getEvdoDbm() {
        return this.mEvdoDbm;
    }

    @UnsupportedAppUsage
    public void setEvdoDbm(int evdoDbm) {
        this.mEvdoDbm = evdoDbm;
    }

    public int getEvdoEcio() {
        return this.mEvdoEcio;
    }

    @UnsupportedAppUsage
    public void setEvdoEcio(int evdoEcio) {
        this.mEvdoEcio = evdoEcio;
    }

    public int getEvdoSnr() {
        return this.mEvdoSnr;
    }

    @UnsupportedAppUsage
    public void setEvdoSnr(int evdoSnr) {
        this.mEvdoSnr = evdoSnr;
    }

    public int getLteSignalStrength() {
        return this.mLteSignalStrength;
    }

    @UnsupportedAppUsage
    public void setLteSignalStrength(int lteSignalStrength) {
        this.mLteSignalStrength = lteSignalStrength;
    }

    public int getLteRsrp() {
        return this.mLteRsrp;
    }

    @UnsupportedAppUsage
    public void setLteRsrp(int lteRsrp) {
        this.mLteRsrp = lteRsrp;
    }

    public int getLteRsrq() {
        return this.mLteRsrq;
    }

    @UnsupportedAppUsage
    public void setLteRsrq(int lteRsrq) {
        this.mLteRsrq = lteRsrq;
    }

    public int getLteRssnr() {
        return this.mLteRssnr;
    }

    @UnsupportedAppUsage
    public void setLteRssnr(int lteRssnr) {
        this.mLteRssnr = lteRssnr;
    }

    public int getLteCqi() {
        return this.mLteCqi;
    }

    @UnsupportedAppUsage
    public void setLteCqi(int lteCqi) {
        this.mLteCqi = lteCqi;
    }
}
