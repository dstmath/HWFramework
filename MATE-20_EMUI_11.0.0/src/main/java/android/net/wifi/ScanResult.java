package android.net.wifi;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.net.wifi.hwUtil.SafeDisplayUtil;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ScanResult implements Parcelable {
    public static final int CHANNEL_WIDTH_160MHZ = 3;
    public static final int CHANNEL_WIDTH_20MHZ = 0;
    public static final int CHANNEL_WIDTH_40MHZ = 1;
    public static final int CHANNEL_WIDTH_80MHZ = 2;
    public static final int CHANNEL_WIDTH_80MHZ_PLUS_MHZ = 4;
    public static final int CIPHER_CCMP = 3;
    public static final int CIPHER_GCMP_256 = 4;
    public static final int CIPHER_NONE = 0;
    public static final int CIPHER_NO_GROUP_ADDRESSED = 1;
    public static final int CIPHER_TKIP = 2;
    @UnsupportedAppUsage
    public static final Parcelable.Creator<ScanResult> CREATOR = new Parcelable.Creator<ScanResult>() {
        /* class android.net.wifi.ScanResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ScanResult createFromParcel(Parcel in) {
            WifiSsid wifiSsid = null;
            boolean z = true;
            if (in.readInt() == 1) {
                wifiSsid = WifiSsid.CREATOR.createFromParcel(in);
            }
            ScanResult sr = new ScanResult(wifiSsid, in.readString(), in.readString(), in.readLong(), in.readInt(), in.readString(), in.readInt(), in.readInt(), in.readLong(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), false);
            sr.seen = in.readLong();
            sr.untrusted = in.readInt() != 0;
            sr.numUsage = in.readInt();
            sr.venueName = in.readString();
            sr.operatorFriendlyName = in.readString();
            sr.flags = in.readLong();
            sr.isHiLinkNetwork = in.readInt() != 0;
            HwScanResultUtil.createFromParcelForWifiproParams(sr, in);
            sr.hilinkTag = in.readInt();
            sr.supportedWifiCategory = in.readInt();
            int n = in.readInt();
            if (n != 0) {
                sr.informationElements = new InformationElement[n];
                for (int i = 0; i < n; i++) {
                    sr.informationElements[i] = new InformationElement();
                    sr.informationElements[i].id = in.readInt();
                    sr.informationElements[i].bytes = new byte[in.readInt()];
                    in.readByteArray(sr.informationElements[i].bytes);
                }
            }
            int n2 = in.readInt();
            if (n2 != 0) {
                sr.anqpLines = new ArrayList();
                for (int i2 = 0; i2 < n2; i2++) {
                    sr.anqpLines.add(in.readString());
                }
            }
            int n3 = in.readInt();
            if (n3 != 0) {
                sr.anqpElements = new AnqpInformationElement[n3];
                for (int i3 = 0; i3 < n3; i3++) {
                    int vendorId = in.readInt();
                    int elementId = in.readInt();
                    byte[] payload = new byte[in.readInt()];
                    in.readByteArray(payload);
                    sr.anqpElements[i3] = new AnqpInformationElement(vendorId, elementId, payload);
                }
            }
            if (in.readInt() == 0) {
                z = false;
            }
            sr.isCarrierAp = z;
            sr.carrierApEapType = in.readInt();
            sr.carrierName = in.readString();
            int n4 = in.readInt();
            if (n4 != 0) {
                sr.radioChainInfos = new RadioChainInfo[n4];
                for (int i4 = 0; i4 < n4; i4++) {
                    sr.radioChainInfos[i4] = new RadioChainInfo();
                    sr.radioChainInfos[i4].id = in.readInt();
                    sr.radioChainInfos[i4].level = in.readInt();
                }
            }
            return sr;
        }

        @Override // android.os.Parcelable.Creator
        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };
    public static final long FLAG_80211mc_RESPONDER = 2;
    public static final long FLAG_PASSPOINT_NETWORK = 1;
    public static final int KEY_MGMT_CERT = 13;
    public static final int KEY_MGMT_EAP = 2;
    public static final int KEY_MGMT_EAP_SHA256 = 6;
    public static final int KEY_MGMT_EAP_SUITE_B_192 = 10;
    public static final int KEY_MGMT_FT_EAP = 4;
    public static final int KEY_MGMT_FT_PSK = 3;
    public static final int KEY_MGMT_FT_SAE = 11;
    public static final int KEY_MGMT_NONE = 0;
    public static final int KEY_MGMT_OSEN = 7;
    public static final int KEY_MGMT_OWE = 9;
    public static final int KEY_MGMT_OWE_TRANSITION = 12;
    public static final int KEY_MGMT_PSK = 1;
    public static final int KEY_MGMT_PSK_SHA256 = 5;
    public static final int KEY_MGMT_SAE = 8;
    public static final int PROTOCOL_NONE = 0;
    public static final int PROTOCOL_OSEN = 3;
    public static final int PROTOCOL_RSN = 2;
    public static final int PROTOCOL_WAPI = 4;
    public static final int PROTOCOL_WPA = 1;
    public static final int UNSPECIFIED = -1;
    private static final int WIFI_CATEGORY_DEFAULT = 1;
    public String BSSID;
    public String SSID;
    @UnsupportedAppUsage
    public int anqpDomainId;
    public AnqpInformationElement[] anqpElements;
    @UnsupportedAppUsage
    public List<String> anqpLines;
    public String capabilities;
    public int carrierApEapType;
    public String carrierName;
    public int centerFreq0;
    public int centerFreq1;
    public int channelWidth;
    @UnsupportedAppUsage
    public int distanceCm;
    @UnsupportedAppUsage
    public int distanceSdCm;
    public boolean dot11vNetwork;
    @UnsupportedAppUsage
    public long flags;
    public int frequency;
    @UnsupportedAppUsage
    public long hessid;
    public int hilinkTag;
    @UnsupportedAppUsage
    public InformationElement[] informationElements;
    public int internetAccessType;
    @UnsupportedAppUsage
    public boolean is80211McRTTResponder;
    public boolean isCarrierAp;
    public boolean isHiLinkNetwork;
    public int level;
    public int networkQosLevel;
    public int networkQosScore;
    public int networkSecurity;
    @UnsupportedAppUsage
    public int numUsage;
    public CharSequence operatorFriendlyName;
    public RadioChainInfo[] radioChainInfos;
    @UnsupportedAppUsage
    public long seen;
    public int supportedWifiCategory;
    public long timestamp;
    @SystemApi
    public boolean untrusted;
    public CharSequence venueName;
    @UnsupportedAppUsage
    public WifiSsid wifiSsid;

    public static class RadioChainInfo {
        public int id;
        public int level;

        public String toString() {
            return "RadioChainInfo: id=" + this.id + ", level=" + this.level;
        }

        public boolean equals(Object otherObj) {
            if (this == otherObj) {
                return true;
            }
            if (!(otherObj instanceof RadioChainInfo)) {
                return false;
            }
            RadioChainInfo other = (RadioChainInfo) otherObj;
            if (this.id == other.id && this.level == other.level) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.id), Integer.valueOf(this.level));
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

    public static class InformationElement {
        @UnsupportedAppUsage
        public static final int EID_BSS_LOAD = 11;
        @UnsupportedAppUsage
        public static final int EID_ERP = 42;
        @UnsupportedAppUsage
        public static final int EID_EXTENDED_CAPS = 127;
        @UnsupportedAppUsage
        public static final int EID_EXTENDED_SUPPORTED_RATES = 50;
        public static final int EID_HT_CAP = 45;
        public static final int EID_HT_CAPABILITIES = 45;
        @UnsupportedAppUsage
        public static final int EID_HT_OPERATION = 61;
        @UnsupportedAppUsage
        public static final int EID_INTERWORKING = 107;
        public static final int EID_MDIE = 54;
        public static final int EID_RM = 70;
        @UnsupportedAppUsage
        public static final int EID_ROAMING_CONSORTIUM = 111;
        @UnsupportedAppUsage
        public static final int EID_RSN = 48;
        @UnsupportedAppUsage
        public static final int EID_SSID = 0;
        @UnsupportedAppUsage
        public static final int EID_SUPPORTED_RATES = 1;
        @UnsupportedAppUsage
        public static final int EID_TIM = 5;
        public static final int EID_VHT_CAPABILITIES = 191;
        @UnsupportedAppUsage
        public static final int EID_VHT_OPERATION = 192;
        @UnsupportedAppUsage
        public static final int EID_VSA = 221;
        @UnsupportedAppUsage
        public static final int EID_WAPI = 68;
        @UnsupportedAppUsage
        public byte[] bytes;
        @UnsupportedAppUsage
        public int id;

        public InformationElement() {
        }

        public InformationElement(InformationElement rhs) {
            this.id = rhs.id;
            this.bytes = (byte[]) rhs.bytes.clone();
        }
    }

    public ScanResult(WifiSsid wifiSsid2, String BSSID2, long hessid2, int anqpDomainId2, byte[] osuProviders, String caps, int level2, int frequency2, long tsf) {
        this.supportedWifiCategory = 1;
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        this.wifiSsid = wifiSsid2;
        this.SSID = wifiSsid2 != null ? wifiSsid2.toString() : "<unknown ssid>";
        this.BSSID = BSSID2;
        this.hessid = hessid2;
        this.anqpDomainId = anqpDomainId2;
        if (osuProviders != null) {
            this.anqpElements = new AnqpInformationElement[1];
            this.anqpElements[0] = new AnqpInformationElement(AnqpInformationElement.HOTSPOT20_VENDOR_ID, 8, osuProviders);
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
        this.supportedWifiCategory = 1;
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
        this.wifiSsid = wifiSsid2;
        this.SSID = wifiSsid2 != null ? wifiSsid2.toString() : "<unknown ssid>";
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
        this.supportedWifiCategory = 1;
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
        this.supportedWifiCategory = 1;
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
            this.isHiLinkNetwork = source.isHiLinkNetwork;
            HwScanResultUtil.scanResultWifiproParams(this, source);
            this.hilinkTag = source.hilinkTag;
            this.supportedWifiCategory = source.supportedWifiCategory;
        }
    }

    public ScanResult() {
        this.supportedWifiCategory = 1;
        this.isHiLinkNetwork = false;
        this.dot11vNetwork = false;
    }

    public int getSupportedWifiCategory() {
        return this.supportedWifiCategory;
    }

    public void setSupportedWifiCategory(int supportedWifiCategory2) {
        this.supportedWifiCategory = supportedWifiCategory2;
    }

    public String toString() {
        String str;
        StringBuffer sb = new StringBuffer();
        sb.append("SSID: ");
        WifiSsid wifiSsid2 = this.wifiSsid;
        sb.append(wifiSsid2 == null ? "<unknown ssid>" : SafeDisplayUtil.safeDisplaySsid(wifiSsid2.toString()));
        sb.append(", BSSID: ");
        String str2 = this.BSSID;
        if (str2 == null) {
            str = "<none>";
        } else {
            str = SafeDisplayUtil.safeDisplayBssid(str2);
        }
        sb.append(str);
        sb.append(", capabilities: ");
        String str3 = this.capabilities;
        if (str3 == null) {
            str3 = "<none>";
        }
        sb.append(str3);
        sb.append(", level: ");
        sb.append(this.level);
        sb.append(", frequency: ");
        sb.append(this.frequency);
        sb.append(", timestamp: ");
        sb.append(this.timestamp);
        sb.append(", distance: ");
        int i = this.distanceCm;
        Object obj = "?";
        sb.append(i != -1 ? Integer.valueOf(i) : obj);
        sb.append("(cm)");
        sb.append(", distanceSd: ");
        int i2 = this.distanceSdCm;
        if (i2 != -1) {
            obj = Integer.valueOf(i2);
        }
        sb.append(obj);
        sb.append("(cm)");
        sb.append(", passpoint: ");
        String str4 = "yes";
        sb.append((this.flags & 1) != 0 ? str4 : "no");
        sb.append(", ChannelBandwidth: ");
        sb.append(this.channelWidth);
        sb.append(", centerFreq0: ");
        sb.append(this.centerFreq0);
        sb.append(", centerFreq1: ");
        sb.append(this.centerFreq1);
        sb.append(", 80211mcResponder: ");
        sb.append((this.flags & 2) != 0 ? "is supported" : "is not supported");
        sb.append(", Carrier AP: ");
        if (!this.isCarrierAp) {
            str4 = "no";
        }
        sb.append(str4);
        sb.append(", Carrier AP EAP Type: ");
        sb.append(this.carrierApEapType);
        sb.append(", Carrier name: ");
        sb.append(this.carrierName);
        sb.append(", Radio Chain Infos: ");
        sb.append(Arrays.toString(this.radioChainInfos));
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags2) {
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
        CharSequence charSequence = this.venueName;
        String str = "";
        dest.writeString(charSequence != null ? charSequence.toString() : str);
        CharSequence charSequence2 = this.operatorFriendlyName;
        if (charSequence2 != null) {
            str = charSequence2.toString();
        }
        dest.writeString(str);
        dest.writeLong(this.flags);
        dest.writeInt(this.isHiLinkNetwork ? 1 : 0);
        HwScanResultUtil.writeToParcelForWifiproParams(this, dest);
        dest.writeInt(this.hilinkTag);
        dest.writeInt(this.supportedWifiCategory);
        InformationElement[] informationElementArr = this.informationElements;
        if (informationElementArr != null) {
            dest.writeInt(informationElementArr.length);
            int i = 0;
            while (true) {
                InformationElement[] informationElementArr2 = this.informationElements;
                if (i >= informationElementArr2.length) {
                    break;
                }
                dest.writeInt(informationElementArr2[i].id);
                dest.writeInt(this.informationElements[i].bytes.length);
                dest.writeByteArray(this.informationElements[i].bytes);
                i++;
            }
        } else {
            dest.writeInt(0);
        }
        List<String> list = this.anqpLines;
        if (list != null) {
            dest.writeInt(list.size());
            for (int i2 = 0; i2 < this.anqpLines.size(); i2++) {
                dest.writeString(this.anqpLines.get(i2));
            }
        } else {
            dest.writeInt(0);
        }
        AnqpInformationElement[] anqpInformationElementArr = this.anqpElements;
        if (anqpInformationElementArr != null) {
            dest.writeInt(anqpInformationElementArr.length);
            AnqpInformationElement[] anqpInformationElementArr2 = this.anqpElements;
            for (AnqpInformationElement element : anqpInformationElementArr2) {
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
        RadioChainInfo[] radioChainInfoArr = this.radioChainInfos;
        if (radioChainInfoArr != null) {
            dest.writeInt(radioChainInfoArr.length);
            int i3 = 0;
            while (true) {
                RadioChainInfo[] radioChainInfoArr2 = this.radioChainInfos;
                if (i3 < radioChainInfoArr2.length) {
                    dest.writeInt(radioChainInfoArr2[i3].id);
                    dest.writeInt(this.radioChainInfos[i3].level);
                    i3++;
                } else {
                    return;
                }
            }
        } else {
            dest.writeInt(0);
        }
    }
}
