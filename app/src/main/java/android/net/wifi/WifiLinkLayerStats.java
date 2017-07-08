package android.net.wifi;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class WifiLinkLayerStats implements Parcelable {
    public static final Creator<WifiLinkLayerStats> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiLinkLayerStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiLinkLayerStats.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiLinkLayerStats.<clinit>():void");
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(" WifiLinkLayerStats: ").append('\n');
        if (this.SSID != null) {
            sbuf.append(" SSID: ").append(this.SSID).append('\n');
        }
        if (this.BSSID != null) {
            sbuf.append(" BSSID: ").append(this.BSSID).append('\n');
        }
        sbuf.append(" my bss beacon rx: ").append(Integer.toString(this.beacon_rx)).append('\n');
        sbuf.append(" RSSI mgmt: ").append(Integer.toString(this.rssi_mgmt)).append('\n');
        sbuf.append(" BE : ").append(" rx=").append(Long.toString(this.rxmpdu_be)).append(" tx=").append(Long.toString(this.txmpdu_be)).append(" lost=").append(Long.toString(this.lostmpdu_be)).append(" retries=").append(Long.toString(this.retries_be)).append('\n');
        sbuf.append(" BK : ").append(" rx=").append(Long.toString(this.rxmpdu_bk)).append(" tx=").append(Long.toString(this.txmpdu_bk)).append(" lost=").append(Long.toString(this.lostmpdu_bk)).append(" retries=").append(Long.toString(this.retries_bk)).append('\n');
        sbuf.append(" VI : ").append(" rx=").append(Long.toString(this.rxmpdu_vi)).append(" tx=").append(Long.toString(this.txmpdu_vi)).append(" lost=").append(Long.toString(this.lostmpdu_vi)).append(" retries=").append(Long.toString(this.retries_vi)).append('\n');
        sbuf.append(" VO : ").append(" rx=").append(Long.toString(this.rxmpdu_vo)).append(" tx=").append(Long.toString(this.txmpdu_vo)).append(" lost=").append(Long.toString(this.lostmpdu_vo)).append(" retries=").append(Long.toString(this.retries_vo)).append('\n');
        sbuf.append(" on_time : ").append(Integer.toString(this.on_time)).append(" rx_time=").append(Integer.toString(this.rx_time)).append(" scan_time=").append(Integer.toString(this.on_time_scan)).append('\n').append(" tx_time=").append(Integer.toString(this.tx_time)).append(" tx_time_per_level=").append(Arrays.toString(this.tx_time_per_level));
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
