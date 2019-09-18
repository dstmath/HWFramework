package android.net.wifi;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    public static final Parcelable.Creator<ScanResult> CREATOR = new Parcelable.Creator<ScanResult>() {
        public ScanResult createFromParcel(Parcel in) {
            Parcel parcel = in;
            WifiSsid wifiSsid = null;
            boolean z = true;
            if (in.readInt() == 1) {
                wifiSsid = WifiSsid.CREATOR.createFromParcel(parcel);
            }
            ScanResult scanResult = new ScanResult(wifiSsid, in.readString(), in.readString(), in.readLong(), in.readInt(), in.readString(), in.readInt(), in.readInt(), in.readLong(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), false);
            scanResult.seen = in.readLong();
            int i = 0;
            scanResult.untrusted = in.readInt() != 0;
            scanResult.numUsage = in.readInt();
            scanResult.venueName = in.readString();
            scanResult.operatorFriendlyName = in.readString();
            scanResult.flags = in.readLong();
            scanResult.internetAccessType = in.readInt();
            scanResult.networkQosLevel = in.readInt();
            scanResult.networkSecurity = in.readInt();
            scanResult.networkQosScore = in.readInt();
            scanResult.isHiLinkNetwork = in.readInt() != 0;
            scanResult.dot11vNetwork = in.readInt() != 0;
            scanResult.hilinkTag = in.readInt();
            int n = in.readInt();
            if (n != 0) {
                scanResult.informationElements = new InformationElement[n];
                for (int i2 = 0; i2 < n; i2++) {
                    scanResult.informationElements[i2] = new InformationElement();
                    scanResult.informationElements[i2].id = in.readInt();
                    scanResult.informationElements[i2].bytes = new byte[in.readInt()];
                    parcel.readByteArray(scanResult.informationElements[i2].bytes);
                }
            }
            int n2 = in.readInt();
            if (n2 != 0) {
                scanResult.anqpLines = new ArrayList();
                for (int i3 = 0; i3 < n2; i3++) {
                    scanResult.anqpLines.add(in.readString());
                }
            }
            int n3 = in.readInt();
            if (n3 != 0) {
                scanResult.anqpElements = new AnqpInformationElement[n3];
                for (int i4 = 0; i4 < n3; i4++) {
                    int vendorId = in.readInt();
                    int elementId = in.readInt();
                    byte[] payload = new byte[in.readInt()];
                    parcel.readByteArray(payload);
                    scanResult.anqpElements[i4] = new AnqpInformationElement(vendorId, elementId, payload);
                }
            }
            if (in.readInt() == 0) {
                z = false;
            }
            scanResult.isCarrierAp = z;
            scanResult.carrierApEapType = in.readInt();
            scanResult.carrierName = in.readString();
            int n4 = in.readInt();
            if (n4 != 0) {
                scanResult.radioChainInfos = new RadioChainInfo[n4];
                while (true) {
                    int i5 = i;
                    if (i5 >= n4) {
                        break;
                    }
                    scanResult.radioChainInfos[i5] = new RadioChainInfo();
                    scanResult.radioChainInfos[i5].id = in.readInt();
                    scanResult.radioChainInfos[i5].level = in.readInt();
                    i = i5 + 1;
                }
            }
            return scanResult;
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
    public String capabilities;
    public int carrierApEapType;
    public String carrierName;
    public int centerFreq0;
    public int centerFreq1;
    public int channelWidth;
    public int distanceCm;
    public int distanceSdCm;
    public boolean dot11vNetwork;
    public long flags;
    public int frequency;
    public long hessid;
    public int hilinkTag;
    public InformationElement[] informationElements;
    public int internetAccessType;
    public boolean is80211McRTTResponder;
    public boolean isCarrierAp;
    public boolean isHiLinkNetwork;
    public int level;
    public int networkQosLevel;
    public int networkQosScore;
    public int networkSecurity;
    public int numUsage;
    public CharSequence operatorFriendlyName;
    public RadioChainInfo[] radioChainInfos;
    public long seen;
    public long timestamp;
    @SystemApi
    public boolean untrusted;
    public CharSequence venueName;
    public WifiSsid wifiSsid;

    public static class InformationElement {
        public static final int EID_BSS_LOAD = 11;
        public static final int EID_ERP = 42;
        public static final int EID_EXTENDED_CAPS = 127;
        public static final int EID_EXTENDED_SUPPORTED_RATES = 50;
        public static final int EID_HT_CAP = 45;
        public static final int EID_HT_CAPABILITIES = 45;
        public static final int EID_HT_OPERATION = 61;
        public static final int EID_INTERWORKING = 107;
        public static final int EID_MDIE = 54;
        public static final int EID_RM = 70;
        public static final int EID_ROAMING_CONSORTIUM = 111;
        public static final int EID_RSN = 48;
        public static final int EID_SSID = 0;
        public static final int EID_SUPPORTED_RATES = 1;
        public static final int EID_TIM = 5;
        public static final int EID_VHT_CAPABILITIES = 191;
        public static final int EID_VHT_OPERATION = 192;
        public static final int EID_VSA = 221;
        public static final int EID_WAPI = 68;
        public byte[] bytes;
        public int id;

        public InformationElement() {
        }

        public InformationElement(InformationElement rhs) {
            this.id = rhs.id;
            this.bytes = (byte[]) rhs.bytes.clone();
        }
    }

    public static class RadioChainInfo {
        public int id;
        public int level;

        public String toString() {
            return "RadioChainInfo: id=" + this.id + ", level=" + this.level;
        }

        public boolean equals(Object otherObj) {
            boolean z = true;
            if (this == otherObj) {
                return true;
            }
            if (!(otherObj instanceof RadioChainInfo)) {
                return false;
            }
            RadioChainInfo other = (RadioChainInfo) otherObj;
            if (!(this.id == other.id && this.level == other.level)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.id), Integer.valueOf(this.level)});
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

    public ScanResult(WifiSsid wifiSsid2, String BSSID2, long hessid2, int anqpDomainId2, byte[] osuProviders, String caps, int level2, int frequency2, long tsf) {
        WifiSsid wifiSsid3 = wifiSsid2;
        byte[] bArr = osuProviders;
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        this.wifiSsid = wifiSsid3;
        this.SSID = wifiSsid3 != null ? wifiSsid2.toString() : WifiSsid.NONE;
        this.BSSID = BSSID2;
        this.hessid = hessid2;
        this.anqpDomainId = anqpDomainId2;
        if (bArr != null) {
            this.anqpElements = new AnqpInformationElement[1];
            this.anqpElements[0] = new AnqpInformationElement(AnqpInformationElement.HOTSPOT20_VENDOR_ID, 8, bArr);
        }
        this.capabilities = caps;
        this.level = level2;
        this.frequency = frequency2;
        this.timestamp = tsf;
        this.distanceCm = -1;
        this.distanceSdCm = -1;
        this.channelWidth = -1;
        this.centerFreq0 = -1;
        this.centerFreq1 = -1;
        this.flags = 0;
        this.isCarrierAp = false;
        this.carrierApEapType = -1;
        this.carrierName = null;
        this.radioChainInfos = null;
    }

    public ScanResult(WifiSsid wifiSsid2, String BSSID2, String caps, int level2, int frequency2, long tsf, int distCm, int distSdCm) {
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        this.wifiSsid = wifiSsid2;
        this.SSID = wifiSsid2 != null ? wifiSsid2.toString() : WifiSsid.NONE;
        this.BSSID = BSSID2;
        this.capabilities = caps;
        this.level = level2;
        this.frequency = frequency2;
        this.timestamp = tsf;
        this.distanceCm = distCm;
        this.distanceSdCm = distSdCm;
        this.channelWidth = -1;
        this.centerFreq0 = -1;
        this.centerFreq1 = -1;
        this.flags = 0;
        this.isCarrierAp = false;
        this.carrierApEapType = -1;
        this.carrierName = null;
        this.radioChainInfos = null;
    }

    public ScanResult(String Ssid, String BSSID2, long hessid2, int anqpDomainId2, String caps, int level2, int frequency2, long tsf, int distCm, int distSdCm, int channelWidth2, int centerFreq02, int centerFreq12, boolean is80211McRTTResponder2) {
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        this.SSID = Ssid;
        this.BSSID = BSSID2;
        this.hessid = hessid2;
        this.anqpDomainId = anqpDomainId2;
        this.capabilities = caps;
        this.level = level2;
        this.frequency = frequency2;
        this.timestamp = tsf;
        this.distanceCm = distCm;
        this.distanceSdCm = distSdCm;
        this.channelWidth = channelWidth2;
        this.centerFreq0 = centerFreq02;
        this.centerFreq1 = centerFreq12;
        if (is80211McRTTResponder2) {
            this.flags = 2;
        } else {
            this.flags = 0;
        }
        this.isCarrierAp = false;
        this.carrierApEapType = -1;
        this.carrierName = null;
        this.radioChainInfos = null;
    }

    public ScanResult(WifiSsid wifiSsid2, String Ssid, String BSSID2, long hessid2, int anqpDomainId2, String caps, int level2, int frequency2, long tsf, int distCm, int distSdCm, int channelWidth2, int centerFreq02, int centerFreq12, boolean is80211McRTTResponder2) {
        this(Ssid, BSSID2, hessid2, anqpDomainId2, caps, level2, frequency2, tsf, distCm, distSdCm, channelWidth2, centerFreq02, centerFreq12, is80211McRTTResponder2);
        this.wifiSsid = wifiSsid2;
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
            this.numUsage = source.numUsage;
            this.venueName = source.venueName;
            this.operatorFriendlyName = source.operatorFriendlyName;
            this.flags = source.flags;
            this.isCarrierAp = source.isCarrierAp;
            this.carrierApEapType = source.carrierApEapType;
            this.carrierName = source.carrierName;
            this.radioChainInfos = source.radioChainInfos;
            this.internetAccessType = source.internetAccessType;
            this.networkQosLevel = source.networkQosLevel;
            this.networkSecurity = source.networkSecurity;
            this.networkQosScore = source.networkQosScore;
            this.isHiLinkNetwork = source.isHiLinkNetwork;
            this.dot11vNetwork = source.dot11vNetwork;
            this.hilinkTag = source.hilinkTag;
        }
    }

    public ScanResult() {
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
    }

    public String toString() {
        String str;
        String str2;
        StringBuffer sb = new StringBuffer();
        sb.append("SSID: ");
        sb.append(this.wifiSsid == null ? WifiSsid.NONE : this.wifiSsid);
        sb.append(", BSSID: ");
        if (this.BSSID == null) {
            str = "<none>";
        } else {
            str = this.BSSID;
        }
        sb.append(str);
        sb.append(", capabilities: ");
        if (this.capabilities == null) {
            str2 = "<none>";
        } else {
            str2 = this.capabilities;
        }
        sb.append(str2);
        sb.append(", level: ");
        sb.append(this.level);
        sb.append(", frequency: ");
        sb.append(this.frequency);
        sb.append(", timestamp: ");
        sb.append(this.timestamp);
        sb.append(", distance: ");
        sb.append(this.distanceCm != -1 ? Integer.valueOf(this.distanceCm) : "?");
        sb.append("(cm)");
        sb.append(", distanceSd: ");
        sb.append(this.distanceSdCm != -1 ? Integer.valueOf(this.distanceSdCm) : "?");
        sb.append("(cm)");
        sb.append(", passpoint: ");
        sb.append((this.flags & 1) != 0 ? "yes" : "no");
        sb.append(", ChannelBandwidth: ");
        sb.append(this.channelWidth);
        sb.append(", centerFreq0: ");
        sb.append(this.centerFreq0);
        sb.append(", centerFreq1: ");
        sb.append(this.centerFreq1);
        sb.append(", 80211mcResponder: ");
        sb.append((this.flags & 2) != 0 ? "is supported" : "is not supported");
        sb.append(", Carrier AP: ");
        sb.append(this.isCarrierAp ? "yes" : "no");
        sb.append(", Carrier AP EAP Type: ");
        sb.append(this.carrierApEapType);
        sb.append(", Carrier name: ");
        sb.append(this.carrierName);
        sb.append(", Radio Chain Infos: ");
        sb.append(Arrays.toString(this.radioChainInfos));
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags2) {
        int i = 0;
        if (this.wifiSsid != null) {
            dest.writeInt(1);
            this.wifiSsid.writeToParcel(dest, flags2);
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
        dest.writeInt(this.numUsage);
        dest.writeString(this.venueName != null ? this.venueName.toString() : "");
        dest.writeString(this.operatorFriendlyName != null ? this.operatorFriendlyName.toString() : "");
        dest.writeLong(this.flags);
        dest.writeInt(this.internetAccessType);
        dest.writeInt(this.networkQosLevel);
        dest.writeInt(this.networkSecurity);
        dest.writeInt(this.networkQosScore);
        dest.writeInt(this.isHiLinkNetwork ? 1 : 0);
        dest.writeInt(this.dot11vNetwork ? 1 : 0);
        dest.writeInt(this.hilinkTag);
        if (this.informationElements != null) {
            dest.writeInt(this.informationElements.length);
            for (int i2 = 0; i2 < this.informationElements.length; i2++) {
                dest.writeInt(this.informationElements[i2].id);
                dest.writeInt(this.informationElements[i2].bytes.length);
                dest.writeByteArray(this.informationElements[i2].bytes);
            }
        } else {
            dest.writeInt(0);
        }
        if (this.anqpLines != null) {
            dest.writeInt(this.anqpLines.size());
            for (int i3 = 0; i3 < this.anqpLines.size(); i3++) {
                dest.writeString(this.anqpLines.get(i3));
            }
        } else {
            dest.writeInt(0);
        }
        if (this.anqpElements != null) {
            dest.writeInt(this.anqpElements.length);
            for (AnqpInformationElement element : this.anqpElements) {
                dest.writeInt(element.getVendorId());
                dest.writeInt(element.getElementId());
                dest.writeInt(element.getPayload().length);
                dest.writeByteArray(element.getPayload());
            }
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.isCarrierAp ? 1 : 0);
        dest.writeInt(this.carrierApEapType);
        dest.writeString(this.carrierName);
        if (this.radioChainInfos != null) {
            dest.writeInt(this.radioChainInfos.length);
            while (true) {
                int i4 = i;
                if (i4 < this.radioChainInfos.length) {
                    dest.writeInt(this.radioChainInfos[i4].id);
                    dest.writeInt(this.radioChainInfos[i4].level);
                    i = i4 + 1;
                } else {
                    return;
                }
            }
        } else {
            dest.writeInt(0);
        }
    }
}
