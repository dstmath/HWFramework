package android.net.wifi;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class ScanResult implements Parcelable {
    public static final int AP_TYPE_INTERNET_ACCESS = 3;
    public static final int AP_TYPE_NO_INTERNET = 1;
    public static final int AP_TYPE_PORTAL = 2;
    public static final int AP_TYPE_UNKOWN = 0;
    public static final int CHANNEL_WIDTH_160MHZ = 3;
    public static final int CHANNEL_WIDTH_20MHZ = 0;
    public static final int CHANNEL_WIDTH_40MHZ = 1;
    public static final int CHANNEL_WIDTH_80MHZ = 2;
    public static final int CHANNEL_WIDTH_80MHZ_PLUS_MHZ = 4;
    public static final int CIPHER_CCMP = 3;
    public static final int CIPHER_NONE = 0;
    public static final int CIPHER_NO_GROUP_ADDRESSED = 1;
    public static final int CIPHER_TKIP = 2;
    public static final Creator<ScanResult> CREATOR = new Creator<ScanResult>() {
        public ScanResult createFromParcel(Parcel in) {
            int i;
            WifiSsid wifiSsid = null;
            if (in.readInt() == 1) {
                wifiSsid = (WifiSsid) WifiSsid.CREATOR.createFromParcel(in);
            }
            ScanResult sr = new ScanResult(wifiSsid, in.readString(), in.readString(), in.readLong(), in.readInt(), in.readString(), in.readInt(), in.readInt(), in.readLong(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), false);
            sr.seen = in.readLong();
            sr.untrusted = in.readInt() != 0;
            sr.numConnection = in.readInt();
            sr.numUsage = in.readInt();
            sr.numIpConfigFailures = in.readInt();
            sr.venueName = in.readString();
            sr.operatorFriendlyName = in.readString();
            sr.flags = in.readLong();
            sr.internetAccessType = in.readInt();
            sr.networkQosLevel = in.readInt();
            sr.networkSecurity = in.readInt();
            sr.networkQosScore = in.readInt();
            sr.isHiLinkNetwork = in.readInt() != 0;
            sr.dot11vNetwork = in.readInt() != 0;
            int n = in.readInt();
            if (n != 0) {
                sr.informationElements = new InformationElement[n];
                for (i = 0; i < n; i++) {
                    sr.informationElements[i] = new InformationElement();
                    sr.informationElements[i].id = in.readInt();
                    sr.informationElements[i].bytes = new byte[in.readInt()];
                    in.readByteArray(sr.informationElements[i].bytes);
                }
            }
            n = in.readInt();
            if (n != 0) {
                sr.anqpLines = new ArrayList();
                for (i = 0; i < n; i++) {
                    sr.anqpLines.add(in.readString());
                }
            }
            n = in.readInt();
            if (n != 0) {
                sr.anqpElements = new AnqpInformationElement[n];
                for (i = 0; i < n; i++) {
                    int vendorId = in.readInt();
                    int elementId = in.readInt();
                    byte[] payload = new byte[in.readInt()];
                    in.readByteArray(payload);
                    sr.anqpElements[i] = new AnqpInformationElement(vendorId, elementId, payload);
                }
            }
            return sr;
        }

        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };
    public static final long FLAG_80211mc_RESPONDER = 2;
    public static final long FLAG_PASSPOINT_NETWORK = 1;
    public static final int KEY_MGMT_CERT = 8;
    public static final int KEY_MGMT_EAP = 2;
    public static final int KEY_MGMT_EAP_SHA256 = 6;
    public static final int KEY_MGMT_FT_EAP = 4;
    public static final int KEY_MGMT_FT_PSK = 3;
    public static final int KEY_MGMT_NONE = 0;
    public static final int KEY_MGMT_OSEN = 7;
    public static final int KEY_MGMT_PSK = 1;
    public static final int KEY_MGMT_PSK_SHA256 = 5;
    public static final int PROTOCOL_NONE = 0;
    public static final int PROTOCOL_OSEN = 3;
    public static final int PROTOCOL_WAPI = 4;
    public static final int PROTOCOL_WPA = 1;
    public static final int PROTOCOL_WPA2 = 2;
    public static final int QOS_LEVEL_GOOD = 3;
    public static final int QOS_LEVEL_NORMAL = 2;
    public static final int QOS_LEVEL_POOR = 1;
    public static final int QOS_LEVEL_UNKOWN = 0;
    public static final int UNSPECIFIED = -1;
    public String BSSID;
    public String SSID;
    public int anqpDomainId;
    public AnqpInformationElement[] anqpElements;
    public List<String> anqpLines;
    public long blackListTimestamp;
    public byte[] bytes;
    public String capabilities;
    public int centerFreq0;
    public int centerFreq1;
    public int channelWidth;
    public int distanceCm;
    public int distanceSdCm;
    public boolean dot11vNetwork;
    public long flags;
    public int frequency;
    public long hessid;
    public InformationElement[] informationElements;
    public int internetAccessType;
    public boolean is80211McRTTResponder;
    public boolean isHiLinkNetwork;
    public int level;
    public int networkQosLevel;
    public int networkQosScore;
    public int networkSecurity;
    public int numConnection;
    public int numIpConfigFailures;
    public int numUsage;
    public CharSequence operatorFriendlyName;
    public long seen;
    public long timestamp;
    public boolean untrusted;
    public CharSequence venueName;
    public WifiSsid wifiSsid;

    public static class InformationElement {
        public static final int EID_BSS_LOAD = 11;
        public static final int EID_ERP = 42;
        public static final int EID_EXTENDED_CAPS = 127;
        public static final int EID_EXTENDED_SUPPORTED_RATES = 50;
        public static final int EID_HT_CAP = 45;
        public static final int EID_HT_OPERATION = 61;
        public static final int EID_INTERWORKING = 107;
        public static final int EID_ROAMING_CONSORTIUM = 111;
        public static final int EID_RSN = 48;
        public static final int EID_SSID = 0;
        public static final int EID_SUPPORTED_RATES = 1;
        public static final int EID_TIM = 5;
        public static final int EID_VHT_OPERATION = 192;
        public static final int EID_VSA = 221;
        public static final int EID_WAPI = 68;
        public byte[] bytes;
        public int id;

        public InformationElement(InformationElement rhs) {
            this.id = rhs.id;
            this.bytes = (byte[]) rhs.bytes.clone();
        }
    }

    public void averageRssi(int previousRssi, long previousSeen, int maxAge) {
        if (this.seen == 0) {
            this.seen = System.currentTimeMillis();
        }
        long age = this.seen - previousSeen;
        if (previousSeen > 0 && age > 0 && age < ((long) (maxAge / 2))) {
            double alpha = 0.5d - (((double) age) / ((double) maxAge));
            this.level = (int) ((((double) this.level) * (1.0d - alpha)) + (((double) previousRssi) * alpha));
        }
    }

    public void setFlag(long flag) {
        this.flags |= flag;
    }

    public void clearFlag(long flag) {
        this.flags &= ~flag;
    }

    public boolean is80211mcResponder() {
        return (this.flags & 2) != 0;
    }

    public boolean isPasspointNetwork() {
        return (this.flags & 1) != 0;
    }

    public boolean is24GHz() {
        return is24GHz(this.frequency);
    }

    public static boolean is24GHz(int freq) {
        return freq > 2400 && freq < 2500;
    }

    public boolean is5GHz() {
        return is5GHz(this.frequency);
    }

    public static boolean is5GHz(int freq) {
        return freq > 4900 && freq < 5900;
    }

    public ScanResult(WifiSsid wifiSsid, String BSSID, long hessid, int anqpDomainId, byte[] osuProviders, String caps, int level, int frequency, long tsf) {
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        this.wifiSsid = wifiSsid;
        this.SSID = wifiSsid != null ? wifiSsid.toString() : WifiSsid.NONE;
        this.BSSID = BSSID;
        this.hessid = hessid;
        this.anqpDomainId = anqpDomainId;
        if (osuProviders != null) {
            this.anqpElements = new AnqpInformationElement[1];
            this.anqpElements[0] = new AnqpInformationElement(AnqpInformationElement.HOTSPOT20_VENDOR_ID, 8, osuProviders);
        }
        this.capabilities = caps;
        this.level = level;
        this.frequency = frequency;
        this.timestamp = tsf;
        this.distanceCm = -1;
        this.distanceSdCm = -1;
        this.channelWidth = -1;
        this.centerFreq0 = -1;
        this.centerFreq1 = -1;
        this.flags = 0;
    }

    public ScanResult(WifiSsid wifiSsid, String BSSID, String caps, int level, int frequency, long tsf, int distCm, int distSdCm) {
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        this.wifiSsid = wifiSsid;
        this.SSID = wifiSsid != null ? wifiSsid.toString() : WifiSsid.NONE;
        this.BSSID = BSSID;
        this.capabilities = caps;
        this.level = level;
        this.frequency = frequency;
        this.timestamp = tsf;
        this.distanceCm = distCm;
        this.distanceSdCm = distSdCm;
        this.channelWidth = -1;
        this.centerFreq0 = -1;
        this.centerFreq1 = -1;
        this.flags = 0;
    }

    public ScanResult(String Ssid, String BSSID, long hessid, int anqpDomainId, String caps, int level, int frequency, long tsf, int distCm, int distSdCm, int channelWidth, int centerFreq0, int centerFreq1, boolean is80211McRTTResponder) {
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        this.SSID = Ssid;
        this.BSSID = BSSID;
        this.hessid = hessid;
        this.anqpDomainId = anqpDomainId;
        this.capabilities = caps;
        this.level = level;
        this.frequency = frequency;
        this.timestamp = tsf;
        this.distanceCm = distCm;
        this.distanceSdCm = distSdCm;
        this.channelWidth = channelWidth;
        this.centerFreq0 = centerFreq0;
        this.centerFreq1 = centerFreq1;
        if (is80211McRTTResponder) {
            this.flags = 2;
        } else {
            this.flags = 0;
        }
    }

    public ScanResult(WifiSsid wifiSsid, String Ssid, String BSSID, long hessid, int anqpDomainId, String caps, int level, int frequency, long tsf, int distCm, int distSdCm, int channelWidth, int centerFreq0, int centerFreq1, boolean is80211McRTTResponder) {
        this(Ssid, BSSID, hessid, anqpDomainId, caps, level, frequency, tsf, distCm, distSdCm, channelWidth, centerFreq0, centerFreq1, is80211McRTTResponder);
        this.wifiSsid = wifiSsid;
    }

    public ScanResult(ScanResult source) {
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        if (source != null) {
            this.wifiSsid = source.wifiSsid;
            this.SSID = source.SSID;
            this.BSSID = source.BSSID;
            this.hessid = source.hessid;
            this.anqpDomainId = source.anqpDomainId;
            this.informationElements = source.informationElements;
            this.anqpElements = source.anqpElements;
            this.capabilities = source.capabilities;
            this.level = source.level;
            this.frequency = source.frequency;
            this.channelWidth = source.channelWidth;
            this.centerFreq0 = source.centerFreq0;
            this.centerFreq1 = source.centerFreq1;
            this.timestamp = source.timestamp;
            this.distanceCm = source.distanceCm;
            this.distanceSdCm = source.distanceSdCm;
            this.seen = source.seen;
            this.untrusted = source.untrusted;
            this.numConnection = source.numConnection;
            this.numUsage = source.numUsage;
            this.numIpConfigFailures = source.numIpConfigFailures;
            this.venueName = source.venueName;
            this.operatorFriendlyName = source.operatorFriendlyName;
            this.flags = source.flags;
            this.internetAccessType = source.internetAccessType;
            this.networkQosLevel = source.networkQosLevel;
            this.networkSecurity = source.networkSecurity;
            this.networkQosScore = source.networkQosScore;
            this.isHiLinkNetwork = source.isHiLinkNetwork;
            this.dot11vNetwork = source.dot11vNetwork;
        }
    }

    public ScanResult() {
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        String none = "<none>";
        StringBuffer append = sb.append("SSID: ").append(this.wifiSsid == null ? WifiSsid.NONE : this.wifiSsid).append(", BSSID: ").append(this.BSSID == null ? none : this.BSSID).append(", capabilities: ");
        if (this.capabilities != null) {
            none = this.capabilities;
        }
        append.append(none).append(", level: ").append(this.level).append(", frequency: ").append(this.frequency).append(", timestamp: ").append(this.timestamp);
        sb.append(", distance: ").append(this.distanceCm != -1 ? Integer.valueOf(this.distanceCm) : "?").append("(cm)");
        sb.append(", distanceSd: ").append(this.distanceSdCm != -1 ? Integer.valueOf(this.distanceSdCm) : "?").append("(cm)");
        sb.append(", passpoint: ");
        sb.append((this.flags & 1) != 0 ? "yes" : "no");
        sb.append(", ChannelBandwidth: ").append(this.channelWidth);
        sb.append(", centerFreq0: ").append(this.centerFreq0);
        sb.append(", centerFreq1: ").append(this.centerFreq1);
        sb.append(", 80211mcResponder: ");
        sb.append((this.flags & 2) != 0 ? "is supported" : "is not supported");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2;
        int i3 = 1;
        int i4 = 0;
        if (this.wifiSsid != null) {
            dest.writeInt(1);
            this.wifiSsid.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.SSID);
        dest.writeString(this.BSSID);
        dest.writeLong(this.hessid);
        dest.writeInt(this.anqpDomainId);
        dest.writeString(this.capabilities);
        dest.writeInt(this.level);
        dest.writeInt(this.frequency);
        dest.writeLong(this.timestamp);
        dest.writeInt(this.distanceCm);
        dest.writeInt(this.distanceSdCm);
        dest.writeInt(this.channelWidth);
        dest.writeInt(this.centerFreq0);
        dest.writeInt(this.centerFreq1);
        dest.writeLong(this.seen);
        dest.writeInt(this.untrusted ? 1 : 0);
        dest.writeInt(this.numConnection);
        dest.writeInt(this.numUsage);
        dest.writeInt(this.numIpConfigFailures);
        dest.writeString(this.venueName != null ? this.venueName.toString() : ProxyInfo.LOCAL_EXCL_LIST);
        dest.writeString(this.operatorFriendlyName != null ? this.operatorFriendlyName.toString() : ProxyInfo.LOCAL_EXCL_LIST);
        dest.writeLong(this.flags);
        dest.writeInt(this.internetAccessType);
        dest.writeInt(this.networkQosLevel);
        dest.writeInt(this.networkSecurity);
        dest.writeInt(this.networkQosScore);
        if (this.isHiLinkNetwork) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.dot11vNetwork) {
            i3 = 0;
        }
        dest.writeInt(i3);
        if (this.informationElements != null) {
            dest.writeInt(this.informationElements.length);
            for (i2 = 0; i2 < this.informationElements.length; i2++) {
                dest.writeInt(this.informationElements[i2].id);
                dest.writeInt(this.informationElements[i2].bytes.length);
                dest.writeByteArray(this.informationElements[i2].bytes);
            }
        } else {
            dest.writeInt(0);
        }
        if (this.anqpLines != null) {
            dest.writeInt(this.anqpLines.size());
            for (i2 = 0; i2 < this.anqpLines.size(); i2++) {
                dest.writeString((String) this.anqpLines.get(i2));
            }
        } else {
            dest.writeInt(0);
        }
        if (this.anqpElements != null) {
            dest.writeInt(this.anqpElements.length);
            AnqpInformationElement[] anqpInformationElementArr = this.anqpElements;
            i3 = anqpInformationElementArr.length;
            while (i4 < i3) {
                AnqpInformationElement element = anqpInformationElementArr[i4];
                dest.writeInt(element.getVendorId());
                dest.writeInt(element.getElementId());
                dest.writeInt(element.getPayload().length);
                dest.writeByteArray(element.getPayload());
                i4++;
            }
            return;
        }
        dest.writeInt(0);
    }
}
