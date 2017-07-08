package android.net.wifi;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
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
    public static final Creator<ScanResult> CREATOR = null;
    public static final long FLAG_80211mc_RESPONDER = 2;
    public static final long FLAG_PASSPOINT_NETWORK = 1;
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
    public int isAutoJoinCandidate;
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
        public byte[] bytes;
        public int id;

        public InformationElement(InformationElement rhs) {
            this.id = rhs.id;
            this.bytes = (byte[]) rhs.bytes.clone();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.ScanResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.ScanResult.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.ScanResult.<clinit>():void");
    }

    public void clearFlag(long r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.ScanResult.clearFlag(long):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.ScanResult.clearFlag(long):void");
    }

    public void averageRssi(int previousRssi, long previousSeen, int maxAge) {
        if (this.seen == 0) {
            this.seen = System.currentTimeMillis();
        }
        long age = this.seen - previousSeen;
        if (previousSeen > 0 && age > 0 && age < ((long) (maxAge / QOS_LEVEL_NORMAL))) {
            double alpha = 0.5d - (((double) age) / ((double) maxAge));
            this.level = (int) ((((double) this.level) * (1.0d - alpha)) + (((double) previousRssi) * alpha));
        }
    }

    public void setFlag(long flag) {
        this.flags |= flag;
    }

    public boolean is80211mcResponder() {
        return (this.flags & FLAG_80211mc_RESPONDER) != 0;
    }

    public boolean isPasspointNetwork() {
        return (this.flags & FLAG_PASSPOINT_NETWORK) != 0;
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
            this.anqpElements = new AnqpInformationElement[QOS_LEVEL_POOR];
            this.anqpElements[QOS_LEVEL_UNKOWN] = new AnqpInformationElement(AnqpInformationElement.HOTSPOT20_VENDOR_ID, 8, osuProviders);
        }
        this.capabilities = caps;
        this.level = level;
        this.frequency = frequency;
        this.timestamp = tsf;
        this.distanceCm = UNSPECIFIED;
        this.distanceSdCm = UNSPECIFIED;
        this.channelWidth = UNSPECIFIED;
        this.centerFreq0 = UNSPECIFIED;
        this.centerFreq1 = UNSPECIFIED;
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
        this.channelWidth = UNSPECIFIED;
        this.centerFreq0 = UNSPECIFIED;
        this.centerFreq1 = UNSPECIFIED;
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
            this.flags = FLAG_80211mc_RESPONDER;
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
            this.isAutoJoinCandidate = source.isAutoJoinCandidate;
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
        sb.append(", distance: ").append(this.distanceCm != UNSPECIFIED ? Integer.valueOf(this.distanceCm) : "?").append("(cm)");
        sb.append(", distanceSd: ").append(this.distanceSdCm != UNSPECIFIED ? Integer.valueOf(this.distanceSdCm) : "?").append("(cm)");
        sb.append(", passpoint: ");
        sb.append((this.flags & FLAG_PASSPOINT_NETWORK) != 0 ? "yes" : "no");
        sb.append(", ChannelBandwidth: ").append(this.channelWidth);
        sb.append(", centerFreq0: ").append(this.centerFreq0);
        sb.append(", centerFreq1: ").append(this.centerFreq1);
        sb.append(", 80211mcResponder: ");
        sb.append((this.flags & FLAG_80211mc_RESPONDER) != 0 ? "is supported" : "is not supported");
        return sb.toString();
    }

    public int describeContents() {
        return QOS_LEVEL_UNKOWN;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2;
        int i3 = QOS_LEVEL_POOR;
        int i4 = QOS_LEVEL_UNKOWN;
        if (this.wifiSsid != null) {
            dest.writeInt(QOS_LEVEL_POOR);
            this.wifiSsid.writeToParcel(dest, flags);
        } else {
            dest.writeInt(QOS_LEVEL_UNKOWN);
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
        dest.writeInt(this.untrusted ? QOS_LEVEL_POOR : QOS_LEVEL_UNKOWN);
        dest.writeInt(this.numConnection);
        dest.writeInt(this.numUsage);
        dest.writeInt(this.numIpConfigFailures);
        dest.writeInt(this.isAutoJoinCandidate);
        dest.writeString(this.venueName != null ? this.venueName.toString() : ProxyInfo.LOCAL_EXCL_LIST);
        dest.writeString(this.operatorFriendlyName != null ? this.operatorFriendlyName.toString() : ProxyInfo.LOCAL_EXCL_LIST);
        dest.writeLong(this.flags);
        dest.writeInt(this.internetAccessType);
        dest.writeInt(this.networkQosLevel);
        dest.writeInt(this.networkSecurity);
        dest.writeInt(this.networkQosScore);
        if (this.isHiLinkNetwork) {
            i = QOS_LEVEL_POOR;
        } else {
            i = QOS_LEVEL_UNKOWN;
        }
        dest.writeInt(i);
        if (!this.dot11vNetwork) {
            i3 = QOS_LEVEL_UNKOWN;
        }
        dest.writeInt(i3);
        if (this.informationElements != null) {
            dest.writeInt(this.informationElements.length);
            for (i2 = QOS_LEVEL_UNKOWN; i2 < this.informationElements.length; i2 += QOS_LEVEL_POOR) {
                dest.writeInt(this.informationElements[i2].id);
                dest.writeInt(this.informationElements[i2].bytes.length);
                dest.writeByteArray(this.informationElements[i2].bytes);
            }
        } else {
            dest.writeInt(QOS_LEVEL_UNKOWN);
        }
        if (this.anqpLines != null) {
            dest.writeInt(this.anqpLines.size());
            for (i2 = QOS_LEVEL_UNKOWN; i2 < this.anqpLines.size(); i2 += QOS_LEVEL_POOR) {
                dest.writeString((String) this.anqpLines.get(i2));
            }
        } else {
            dest.writeInt(QOS_LEVEL_UNKOWN);
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
                i4 += QOS_LEVEL_POOR;
            }
            return;
        }
        dest.writeInt(QOS_LEVEL_UNKOWN);
    }
}
