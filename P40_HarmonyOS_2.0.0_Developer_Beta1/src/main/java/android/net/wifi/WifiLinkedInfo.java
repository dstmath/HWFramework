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
            linkedInfo.ipAddress = in.readInt();
            linkedInfo.supplicantState = in.readString();
            boolean z = false;
            linkedInfo.isHidden = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            }
            linkedInfo.isDataRestricted = z;
            linkedInfo.rxLinkSpeedMbps = in.readInt();
            linkedInfo.txLinkSpeedMbps = in.readInt();
            linkedInfo.chload = in.readInt();
            linkedInfo.snr = in.readInt();
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
    private int chload;
    private int frequency;
    private int ipAddress;
    private boolean isDataRestricted;
    private boolean isHidden;
    private int linkSpeed;
    private String macAddress;
    private int networkId;
    private int rssi;
    private int rxLinkSpeedMbps;
    private int snr;
    private String ssid;
    private String supplicantState;
    private int txLinkSpeedMbps;

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
            this.ipAddress = source.ipAddress;
            this.supplicantState = source.supplicantState;
            this.isHidden = source.isHidden;
            this.isDataRestricted = source.isDataRestricted;
            this.rxLinkSpeedMbps = source.rxLinkSpeedMbps;
            this.txLinkSpeedMbps = source.txLinkSpeedMbps;
            this.chload = source.chload;
            this.snr = source.snr;
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
        this.ipAddress = 0;
        this.supplicantState = SupplicantState.UNINITIALIZED.name();
        this.isHidden = false;
        this.isDataRestricted = false;
        this.rxLinkSpeedMbps = -1;
        this.txLinkSpeedMbps = -1;
        this.chload = -1;
        this.snr = -1;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.networkId);
        dest.writeString(this.ssid);
        dest.writeString(this.bssid);
        dest.writeInt(this.rssi);
        dest.writeInt(this.band);
        dest.writeInt(this.linkSpeed);
        dest.writeInt(this.frequency);
        dest.writeString(this.macAddress);
        dest.writeInt(this.ipAddress);
        dest.writeString(this.supplicantState);
        dest.writeInt(this.isHidden ? 1 : 0);
        dest.writeInt(this.isDataRestricted ? 1 : 0);
        dest.writeInt(this.rxLinkSpeedMbps);
        dest.writeInt(this.txLinkSpeedMbps);
        dest.writeInt(this.chload);
        dest.writeInt(this.snr);
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
            linkedInfo.ipAddress = wifiInfo.getIpAddress();
            linkedInfo.supplicantState = wifiInfo.getSupplicantState().name();
            linkedInfo.isHidden = wifiInfo.getHiddenSSID();
            linkedInfo.isDataRestricted = wifiInfo.getMeteredHint();
            linkedInfo.rxLinkSpeedMbps = wifiInfo.getRxLinkSpeedMbps();
            linkedInfo.txLinkSpeedMbps = wifiInfo.getTxLinkSpeedMbps();
            linkedInfo.chload = wifiInfo.getChload();
            linkedInfo.snr = wifiInfo.getSnr();
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
        sbuf.append("MacAddress: " + SafeDisplayUtil.safeDisplayBssid(this.macAddress) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("SupplicantState: " + this.supplicantState + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("IsHidden: " + String.valueOf(this.isHidden) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("IsDataRestricted: " + String.valueOf(this.isDataRestricted) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("RxLinkSpeedMbps: " + String.valueOf(this.rxLinkSpeedMbps) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("TxLinkSpeedMbps: " + String.valueOf(this.txLinkSpeedMbps) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("Chload: " + String.valueOf(this.chload) + SmsManager.REGEX_PREFIX_DELIMITER);
        StringBuilder sb = new StringBuilder();
        sb.append("Snr: ");
        sb.append(String.valueOf(this.snr));
        sbuf.append(sb.toString());
        return sbuf.toString();
    }
}
