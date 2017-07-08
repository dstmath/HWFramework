package android.net;

import android.os.Parcel;
import android.preference.Preference;

public class WifiLinkQualityInfo extends LinkQualityInfo {
    private String mBssid;
    private int mRssi;
    private long mTxBad;
    private long mTxGood;
    private int mType;

    public WifiLinkQualityInfo() {
        this.mType = Preference.DEFAULT_ORDER;
        this.mRssi = Preference.DEFAULT_ORDER;
        this.mTxGood = LinkQualityInfo.UNKNOWN_LONG;
        this.mTxBad = LinkQualityInfo.UNKNOWN_LONG;
    }

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
