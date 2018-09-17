package android.net.wifi;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class WifiLinkLayerStats implements Parcelable {
    public static final Creator<WifiLinkLayerStats> CREATOR = new Creator<WifiLinkLayerStats>() {
        public WifiLinkLayerStats createFromParcel(Parcel in) {
            WifiLinkLayerStats stats = new WifiLinkLayerStats();
            stats.SSID = in.readString();
            stats.BSSID = in.readString();
            stats.on_time = in.readInt();
            stats.tx_time = in.readInt();
            stats.tx_time_per_level = in.createIntArray();
            stats.rx_time = in.readInt();
            stats.on_time_scan = in.readInt();
            return stats;
        }

        public WifiLinkLayerStats[] newArray(int size) {
            return new WifiLinkLayerStats[size];
        }
    };
    private static final String TAG = "WifiLinkLayerStats";
    public String BSSID;
    public String SSID;
    public int beacon_rx;
    public long lostmpdu_be;
    public long lostmpdu_bk;
    public long lostmpdu_vi;
    public long lostmpdu_vo;
    public int on_time;
    public int on_time_scan;
    public long retries_be;
    public long retries_bk;
    public long retries_vi;
    public long retries_vo;
    public int rssi_mgmt;
    public int rx_time;
    public long rxmpdu_be;
    public long rxmpdu_bk;
    public long rxmpdu_vi;
    public long rxmpdu_vo;
    public int status;
    public int tx_time;
    public int[] tx_time_per_level;
    public long txmpdu_be;
    public long txmpdu_bk;
    public long txmpdu_vi;
    public long txmpdu_vo;

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(" WifiLinkLayerStats: ").append(10);
        if (this.SSID != null) {
            sbuf.append(" SSID: ").append(this.SSID).append(10);
        }
        if (this.BSSID != null) {
            sbuf.append(" BSSID: ").append(this.BSSID).append(10);
        }
        sbuf.append(" my bss beacon rx: ").append(Integer.toString(this.beacon_rx)).append(10);
        sbuf.append(" RSSI mgmt: ").append(Integer.toString(this.rssi_mgmt)).append(10);
        sbuf.append(" BE : ").append(" rx=").append(Long.toString(this.rxmpdu_be)).append(" tx=").append(Long.toString(this.txmpdu_be)).append(" lost=").append(Long.toString(this.lostmpdu_be)).append(" retries=").append(Long.toString(this.retries_be)).append(10);
        sbuf.append(" BK : ").append(" rx=").append(Long.toString(this.rxmpdu_bk)).append(" tx=").append(Long.toString(this.txmpdu_bk)).append(" lost=").append(Long.toString(this.lostmpdu_bk)).append(" retries=").append(Long.toString(this.retries_bk)).append(10);
        sbuf.append(" VI : ").append(" rx=").append(Long.toString(this.rxmpdu_vi)).append(" tx=").append(Long.toString(this.txmpdu_vi)).append(" lost=").append(Long.toString(this.lostmpdu_vi)).append(" retries=").append(Long.toString(this.retries_vi)).append(10);
        sbuf.append(" VO : ").append(" rx=").append(Long.toString(this.rxmpdu_vo)).append(" tx=").append(Long.toString(this.txmpdu_vo)).append(" lost=").append(Long.toString(this.lostmpdu_vo)).append(" retries=").append(Long.toString(this.retries_vo)).append(10);
        sbuf.append(" on_time : ").append(Integer.toString(this.on_time)).append(" rx_time=").append(Integer.toString(this.rx_time)).append(" scan_time=").append(Integer.toString(this.on_time_scan)).append(10).append(" tx_time=").append(Integer.toString(this.tx_time)).append(" tx_time_per_level=").append(Arrays.toString(this.tx_time_per_level));
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public String getPrintableSsid() {
        if (this.SSID == null) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        int length = this.SSID.length();
        if (length > 2 && this.SSID.charAt(0) == '\"' && this.SSID.charAt(length - 1) == '\"') {
            return this.SSID.substring(1, length - 1);
        }
        if (length > 3 && this.SSID.charAt(0) == 'P' && this.SSID.charAt(1) == '\"' && this.SSID.charAt(length - 1) == '\"') {
            return WifiSsid.createFromAsciiEncoded(this.SSID.substring(2, length - 1)).toString();
        }
        return this.SSID;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.SSID);
        dest.writeString(this.BSSID);
        dest.writeInt(this.on_time);
        dest.writeInt(this.tx_time);
        dest.writeIntArray(this.tx_time_per_level);
        dest.writeInt(this.rx_time);
        dest.writeInt(this.on_time_scan);
    }
}
