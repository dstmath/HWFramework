package android.net.wifi;

import android.net.wifi.hwUtil.SafeDisplayUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.security.KeyChain;
import android.telephony.SmsManager;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public class WifiScanInfo implements Parcelable {
    public static final Parcelable.Creator<WifiScanInfo> CREATOR = new Parcelable.Creator<WifiScanInfo>() {
        /* class android.net.wifi.WifiScanInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiScanInfo createFromParcel(Parcel in) {
            WifiScanInfo scanInfo = new WifiScanInfo();
            scanInfo.ssid = in.readString();
            scanInfo.bssid = in.readString();
            scanInfo.capabilities = in.readString();
            scanInfo.securityType = in.readInt();
            scanInfo.rssi = in.readInt();
            scanInfo.frequency = in.readInt();
            scanInfo.channelWidth = in.readInt();
            scanInfo.timestamp = in.readLong();
            return scanInfo;
        }

        @Override // android.os.Parcelable.Creator
        public WifiScanInfo[] newArray(int size) {
            return new WifiScanInfo[size];
        }
    };
    private static final String TAG = "WifiScanInfo";
    private String bssid;
    private String capabilities;
    private int channelWidth;
    private long flags;
    private int frequency;
    private int ieId;
    private Map<Integer, byte[]> ieMap;
    private byte[] informationElements;
    private boolean isHilink;
    private int rssi;
    private int securityType;
    private String ssid;
    private long timestamp;

    public WifiScanInfo(WifiScanInfo source) {
        this.ieMap = new HashMap();
        if (source != null) {
            this.ssid = source.ssid;
            this.bssid = source.bssid;
            this.capabilities = source.capabilities;
            this.securityType = source.securityType;
            this.rssi = source.rssi;
            this.frequency = source.frequency;
            this.channelWidth = source.channelWidth;
            this.timestamp = source.timestamp;
            this.informationElements = source.informationElements;
            this.flags = source.flags;
            this.isHilink = source.isHilink;
        }
    }

    public WifiScanInfo() {
        this.ieMap = new HashMap();
        this.ssid = "<unknown ssid>";
        this.bssid = null;
        this.capabilities = null;
        this.securityType = -1;
        this.rssi = -127;
        this.frequency = 0;
        this.channelWidth = -1;
        this.timestamp = -1;
        this.ieId = -1;
        this.informationElements = null;
        this.flags = 0;
        this.isHilink = false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags2) {
        dest.writeString(this.ssid);
        dest.writeString(this.bssid);
        dest.writeString(this.capabilities);
        dest.writeInt(this.securityType);
        dest.writeInt(this.rssi);
        dest.writeInt(this.frequency);
        dest.writeInt(this.channelWidth);
        dest.writeLong(this.timestamp);
        dest.writeLong((long) flags2);
        dest.writeInt(this.isHilink ? 1 : 0);
        Map<Integer, byte[]> map = this.ieMap;
        if (map == null || map.isEmpty()) {
            dest.writeInt(-1);
            return;
        }
        dest.writeInt(this.ieMap.size());
        for (Map.Entry<Integer, byte[]> ie : this.ieMap.entrySet()) {
            dest.writeInt(ie.getKey().intValue());
            dest.writeByteArray(ie.getValue());
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static WifiScanInfo fromScanResult(ScanResult scanResult) {
        WifiScanInfo scanInfo = new WifiScanInfo();
        if (scanResult != null) {
            scanInfo.ssid = scanResult.SSID;
            scanInfo.bssid = scanResult.BSSID;
            scanInfo.capabilities = scanResult.capabilities;
            scanInfo.securityType = getSecurityTypeFromCapabilities(scanResult.capabilities);
            scanInfo.rssi = scanResult.level;
            scanInfo.frequency = scanResult.frequency;
            scanInfo.channelWidth = scanResult.channelWidth;
            scanInfo.timestamp = scanResult.timestamp;
            scanInfo.flags = scanResult.flags;
            scanInfo.isHilink = scanResult.isHiLinkNetwork;
            if (scanResult.informationElements == null || scanInfo.ieMap == null) {
                scanInfo.ieMap.clear();
            } else {
                for (int i = 0; i < scanResult.informationElements.length; i++) {
                    scanInfo.ieMap.put(Integer.valueOf(scanResult.informationElements[i].id), scanResult.informationElements[i].bytes);
                }
            }
        }
        return scanInfo;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("SSID: " + SafeDisplayUtil.safeDisplaySsid(this.ssid) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("BSSID: " + SafeDisplayUtil.safeDisplayBssid(this.bssid) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("Capabilities: " + this.capabilities + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("SecurityType: " + String.valueOf(this.securityType) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("Rssi: " + String.valueOf(this.rssi) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("Frequency: " + String.valueOf(this.frequency) + SmsManager.REGEX_PREFIX_DELIMITER);
        sbuf.append("ChannelWidth: " + String.valueOf(this.channelWidth) + SmsManager.REGEX_PREFIX_DELIMITER);
        StringBuilder sb = new StringBuilder();
        sb.append("Timestamp: ");
        sb.append(String.valueOf(this.timestamp));
        sbuf.append(sb.toString());
        return sbuf.toString();
    }

    private static int getSecurityTypeFromCapabilities(String capabilities2) {
        if (TextUtils.isEmpty(capabilities2)) {
            return 0;
        }
        if (capabilities2.contains("SAE")) {
            return 4;
        }
        if (capabilities2.contains("PSK")) {
            return 2;
        }
        if (capabilities2.contains("SUITE_B_192")) {
            return 5;
        }
        if (capabilities2.contains("EAP")) {
            return 3;
        }
        if (capabilities2.contains("WEP")) {
            return 1;
        }
        if (capabilities2.contains("OWE")) {
            return 6;
        }
        if (capabilities2.contains(KeyChain.EXTRA_CERTIFICATE)) {
            return 7;
        }
        if (capabilities2.contains(WifiConfiguration.WAPI_PSK_FLAG)) {
            return 8;
        }
        return 0;
    }
}
