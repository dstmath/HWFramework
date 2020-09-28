package android.net.wifi;

import android.net.wifi.hwUtil.SafeDisplayUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;

public class WifiLinkedInfo implements Parcelable {
    public static final Parcelable.Creator<WifiLinkedInfo> CREATOR = new Parcelable.Creator<WifiLinkedInfo>() {
        /* class android.net.wifi.WifiLinkedInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiLinkedInfo createFromParcel(Parcel in) {
            WifiLinkedInfo linkedInfo = new WifiLinkedInfo();
            linkedInfo.networkId = in.readInt();
            linkedInfo.ssid = in.readString();
            linkedInfo.bssid = in.readString();
            linkedInfo.rssi = in.readInt();
            linkedInfo.band = in.readInt();
            linkedInfo.linkSpeed = in.readInt();
            linkedInfo.frequency = in.readInt();
            linkedInfo.macAddress = in.readString();
            return linkedInfo;
        }

        @Override // android.os.Parcelable.Creator
        public WifiLinkedInfo[] newArray(int size) {
            return new WifiLinkedInfo[size];
        }
    };
    private static final String TAG = "WifiLinkedInfo";
    private int band;
    private String bssid;
    private int frequency;
    private int linkSpeed;
    private String macAddress;
    private int networkId;
    private int rssi;
    private String ssid;

    public WifiLinkedInfo(WifiLinkedInfo source) {
        if (source != null) {
            this.networkId = source.networkId;
            this.ssid = source.ssid;
            this.bssid = source.bssid;
            this.rssi = source.rssi;
            this.band = source.band;
            this.linkSpeed = source.linkSpeed;
            this.frequency = source.frequency;
            this.macAddress = source.macAddress;
        }
    }

    public WifiLinkedInfo() {
        this.networkId = -1;
        this.ssid = "<unknown ssid>";
        this.bssid = null;
        this.rssi = -127;
        this.band = -1;
        this.linkSpeed = 0;
        this.frequency = 0;
        this.macAddress = "02:00:00:00:00:00";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.networkId);
        dest.writeString(this.ssid);
        dest.writeString(this.bssid);
        dest.writeInt(this.rssi);
        dest.writeInt(this.band);
        dest.writeInt(this.linkSpeed);
        dest.writeLong((long) this.frequency);
        dest.writeString(this.macAddress);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static WifiLinkedInfo fromWifiInfo(WifiInfo wifiInfo) {
        WifiLinkedInfo linkedInfo = new WifiLinkedInfo();
        if (wifiInfo != null) {
            linkedInfo.networkId = wifiInfo.getNetworkId();
            linkedInfo.ssid = wifiInfo.getSSID();
            linkedInfo.bssid = wifiInfo.getBSSID();
            linkedInfo.rssi = wifiInfo.getRssi();
            linkedInfo.linkSpeed = wifiInfo.getLinkSpeed();
            linkedInfo.frequency = wifiInfo.getFrequency();
            if (linkedInfo.frequency >= 4900) {
                linkedInfo.band = 1;
            } else {
                linkedInfo.band = 0;
            }
            linkedInfo.macAddress = wifiInfo.getMacAddress();
        }
        return linkedInfo;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("NetworkId: " + String.valueOf(this.networkId) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("SSID: " + SafeDisplayUtil.safeDisplaySsid(this.ssid) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("BSSID: " + SafeDisplayUtil.safeDisplayBssid(this.bssid) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("Rssi: " + String.valueOf(this.rssi) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("Band: " + String.valueOf(this.band) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("LinkSpeed: " + String.valueOf(this.linkSpeed) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("Frequency: " + String.valueOf(this.frequency) + SmsManager.REGEX_PREFIX_DELIMITER);
        StringBuilder sb = new StringBuilder();
        sb.append("MacAddress: ");
        sb.append(SafeDisplayUtil.safeDisplayBssid(this.macAddress));
        sbuf.append(sb.toString());
        return sbuf.toString();
    }
}
