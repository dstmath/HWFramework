package android.net;

import android.os.Parcel;

public class WifiLinkQualityInfo extends LinkQualityInfo {
    private String mBssid;
    private int mRssi = Integer.MAX_VALUE;
    private long mTxBad = Long.MAX_VALUE;
    private long mTxGood = Long.MAX_VALUE;
    private int mType = Integer.MAX_VALUE;

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 2);
        dest.writeInt(this.mType);
        dest.writeInt(this.mRssi);
        dest.writeLong(this.mTxGood);
        dest.writeLong(this.mTxBad);
        dest.writeString(this.mBssid);
    }

    public static WifiLinkQualityInfo createFromParcelBody(Parcel in) {
        WifiLinkQualityInfo li = new WifiLinkQualityInfo();
        li.initializeFromParcel(in);
        li.mType = in.readInt();
        li.mRssi = in.readInt();
        li.mTxGood = in.readLong();
        li.mTxBad = in.readLong();
        li.mBssid = in.readString();
        return li;
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String getBssid() {
        return this.mBssid;
    }

    public void setBssid(String bssid) {
        this.mBssid = bssid;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public long getTxGood() {
        return this.mTxGood;
    }

    public void setTxGood(long txGood) {
        this.mTxGood = txGood;
    }

    public long getTxBad() {
        return this.mTxBad;
    }

    public void setTxBad(long txBad) {
        this.mTxBad = txBad;
    }
}
