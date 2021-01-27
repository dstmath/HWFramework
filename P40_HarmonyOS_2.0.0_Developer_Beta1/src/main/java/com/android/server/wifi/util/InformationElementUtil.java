package com.android.server.wifi.util;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.net.wifi.ScanResult;
import android.util.Log;
import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hwUtil.HwInformationElementUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class InformationElementUtil {
    private static final int CENTER_FREQ_INDEX_DEFAULT = 0;
    private static final int CENTER_FREQ_INDEX_OFFSET_160M = 8;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "InformationElementUtil";

    public static ScanResult.InformationElement[] parseInformationElements(byte[] bytes) {
        if (bytes == null) {
            return new ScanResult.InformationElement[0];
        }
        ByteBuffer data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        ArrayList<ScanResult.InformationElement> infoElements = new ArrayList<>();
        boolean found_ssid = false;
        while (data.remaining() > 1) {
            int eid = data.get() & 255;
            int elementLength = data.get() & 255;
            if (elementLength > data.remaining() || (eid == 0 && found_ssid)) {
                break;
            }
            if (eid == 0) {
                found_ssid = true;
            }
            ScanResult.InformationElement ie = new ScanResult.InformationElement();
            ie.id = eid;
            ie.bytes = new byte[elementLength];
            data.get(ie.bytes);
            infoElements.add(ie);
        }
        return (ScanResult.InformationElement[]) infoElements.toArray(new ScanResult.InformationElement[infoElements.size()]);
    }

    public static RoamingConsortium getRoamingConsortiumIE(ScanResult.InformationElement[] ies) {
        RoamingConsortium roamingConsortium = new RoamingConsortium();
        if (ies != null) {
            for (ScanResult.InformationElement ie : ies) {
                if (ie.id == 111) {
                    try {
                        roamingConsortium.from(ie);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to parse Roaming Consortium IE: " + e.getMessage());
                    }
                }
            }
        }
        return roamingConsortium;
    }

    public static Vsa getHS2VendorSpecificIE(ScanResult.InformationElement[] ies) {
        Vsa vsa = new Vsa();
        if (ies != null) {
            for (ScanResult.InformationElement ie : ies) {
                if (ie.id == 221) {
                    try {
                        vsa.from(ie);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to parse Vendor Specific IE: " + e.getMessage());
                    }
                }
            }
        }
        return vsa;
    }

    public static Interworking getInterworkingIE(ScanResult.InformationElement[] ies) {
        Interworking interworking = new Interworking();
        if (ies != null) {
            for (ScanResult.InformationElement ie : ies) {
                if (ie.id == 107) {
                    try {
                        interworking.from(ie);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to parse Interworking IE: " + e.getMessage());
                    }
                }
            }
        }
        return interworking;
    }

    public static class BssLoad {
        public int capacity = 0;
        public int channelUtilization = 0;
        public int stationCount = 0;

        public void from(ScanResult.InformationElement ie) {
            if (ie.id != 11) {
                throw new IllegalArgumentException("Element id is not BSS_LOAD, : " + ie.id);
            } else if (ie.bytes.length == 5) {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                this.stationCount = data.getShort() & 65535;
                this.channelUtilization = data.get() & 255;
                this.capacity = data.getShort() & 65535;
            } else {
                throw new IllegalArgumentException("BSS Load element length is not 5: " + ie.bytes.length);
            }
        }
    }

    public static class HtOperation {
        public int secondChannelOffset = 0;

        public int getChannelWidth() {
            if (this.secondChannelOffset != 0) {
                return 1;
            }
            return 0;
        }

        public int getCenterFreq0(int primaryFrequency) {
            int i = this.secondChannelOffset;
            if (i == 0) {
                return 0;
            }
            if (i == 1) {
                return primaryFrequency + 10;
            }
            if (i == 3) {
                return primaryFrequency - 10;
            }
            Log.e("HtOperation", "Error on secondChannelOffset: " + this.secondChannelOffset);
            return 0;
        }

        public void from(ScanResult.InformationElement ie) {
            if (ie.id == 61) {
                this.secondChannelOffset = ie.bytes[1] & 3;
                return;
            }
            throw new IllegalArgumentException("Element id is not HT_OPERATION, : " + ie.id);
        }
    }

    public static class VhtOperation {
        public int centerFreqIndex1 = 0;
        public int centerFreqIndex2 = 0;
        public int channelMode = 0;

        public boolean isValid() {
            return this.channelMode != 0;
        }

        public int getChannelWidth() {
            int i;
            if (this.channelMode + 1 == 2 && (i = this.centerFreqIndex2) != 0) {
                int i2 = this.centerFreqIndex1;
                if (i2 - i == 8 || i - i2 == 8) {
                    return 3;
                }
            }
            return this.channelMode + 1;
        }

        public int getCenterFreq0() {
            return ((this.centerFreqIndex1 - 36) * 5) + WifiCommonUtils.FREQ_CH36;
        }

        public int getCenterFreq1() {
            if (this.channelMode > 1) {
                return ((this.centerFreqIndex2 - 36) * 5) + WifiCommonUtils.FREQ_CH36;
            }
            return 0;
        }

        public void from(ScanResult.InformationElement ie) {
            if (ie.id == 192) {
                this.channelMode = ie.bytes[0] & 255;
                this.centerFreqIndex1 = ie.bytes[1] & 255;
                this.centerFreqIndex2 = ie.bytes[2] & 255;
                return;
            }
            throw new IllegalArgumentException("Element id is not VHT_OPERATION, : " + ie.id);
        }
    }

    public static class Interworking {
        public NetworkDetail.Ant ant = null;
        public long hessid = 0;
        public boolean internet = false;

        public void from(ScanResult.InformationElement ie) {
            if (ie.id == 107) {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                int anOptions = data.get() & 255;
                this.ant = NetworkDetail.Ant.values()[anOptions & 15];
                this.internet = (anOptions & 16) != 0;
                if (ie.bytes.length == 1 || ie.bytes.length == 3 || ie.bytes.length == 7 || ie.bytes.length == 9) {
                    if (ie.bytes.length == 3 || ie.bytes.length == 9) {
                        ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, 2);
                    }
                    if (ie.bytes.length == 7 || ie.bytes.length == 9) {
                        this.hessid = ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, 6);
                        return;
                    }
                    return;
                }
                throw new IllegalArgumentException("Bad Interworking element length: " + ie.bytes.length);
            }
            throw new IllegalArgumentException("Element id is not INTERWORKING, : " + ie.id);
        }
    }

    public static class RoamingConsortium {
        public int anqpOICount = 0;
        private long[] roamingConsortiums = null;

        public long[] getRoamingConsortiums() {
            return this.roamingConsortiums;
        }

        public void from(ScanResult.InformationElement ie) {
            if (ie.id == 111) {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                this.anqpOICount = data.get() & 255;
                int oi12Length = data.get() & 255;
                int oi1Length = oi12Length & 15;
                int oi2Length = (oi12Length >>> 4) & 15;
                int oi3Length = ((ie.bytes.length - 2) - oi1Length) - oi2Length;
                int oiCount = 0;
                if (oi1Length > 0) {
                    oiCount = 0 + 1;
                    if (oi2Length > 0) {
                        oiCount++;
                        if (oi3Length > 0) {
                            oiCount++;
                        }
                    }
                }
                this.roamingConsortiums = new long[oiCount];
                if (oi1Length > 0) {
                    long[] jArr = this.roamingConsortiums;
                    if (jArr.length > 0) {
                        jArr[0] = ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, oi1Length);
                    }
                }
                if (oi2Length > 0) {
                    long[] jArr2 = this.roamingConsortiums;
                    if (jArr2.length > 1) {
                        jArr2[1] = ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, oi2Length);
                    }
                }
                if (oi3Length > 0) {
                    long[] jArr3 = this.roamingConsortiums;
                    if (jArr3.length > 2) {
                        jArr3[2] = ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, oi3Length);
                        return;
                    }
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("Element id is not ROAMING_CONSORTIUM, : " + ie.id);
        }
    }

    public static class Vsa {
        private static final int ANQP_DOMID_BIT = 4;
        public int anqpDomainID = 0;
        public NetworkDetail.HSRelease hsRelease = null;

        public void from(ScanResult.InformationElement ie) {
            ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            if (ie.bytes.length >= 5 && data.getInt() == 278556496) {
                int hsConf = data.get() & 255;
                int i = (hsConf >> 4) & 15;
                if (i == 0) {
                    this.hsRelease = NetworkDetail.HSRelease.R1;
                } else if (i != 1) {
                    this.hsRelease = NetworkDetail.HSRelease.Unknown;
                } else {
                    this.hsRelease = NetworkDetail.HSRelease.R2;
                }
                if ((hsConf & 4) == 0) {
                    return;
                }
                if (ie.bytes.length >= 7) {
                    this.anqpDomainID = data.getShort() & 65535;
                    return;
                }
                throw new IllegalArgumentException("HS20 indication element too short: " + ie.bytes.length);
            }
        }
    }

    public static class ExtendedCapabilities {
        private static final int RTT_RESP_ENABLE_BIT = 70;
        private static final int SSID_UTF8_BIT = 48;
        public BitSet capabilitiesBitSet;

        public boolean isStrictUtf8() {
            return this.capabilitiesBitSet.get(48);
        }

        public boolean is80211McRTTResponder() {
            return this.capabilitiesBitSet.get(RTT_RESP_ENABLE_BIT);
        }

        public ExtendedCapabilities() {
            this.capabilitiesBitSet = new BitSet();
        }

        public ExtendedCapabilities(ExtendedCapabilities other) {
            this.capabilitiesBitSet = other.capabilitiesBitSet;
        }

        public void from(ScanResult.InformationElement ie) {
            this.capabilitiesBitSet = BitSet.valueOf(ie.bytes);
        }
    }

    public static class Capabilities {
        private static final int CAP_ESS_BIT_OFFSET = 0;
        private static final int CAP_PRIVACY_BIT_OFFSET = 4;
        private static final int OWE_VENDOR_OUI_TYPE = 479883088;
        private static final int PMF_KEY_MGMT_EAP = 1;
        private static final int PMF_KEY_MGMT_EAP_SHA256 = 5;
        private static final int PMF_KEY_MGMT_FT_EAP = 3;
        private static final int PMF_KEY_MGMT_FT_PSK = 4;
        private static final int PMF_KEY_MGMT_PSK = 2;
        private static final int PMF_KEY_MGMT_PSK_SHA256 = 6;
        private static final short RSNE_VERSION = 1;
        private static final int RSN_AKM_EAP = 28053248;
        private static final int RSN_AKM_EAP_SHA256 = 95162112;
        private static final int RSN_AKM_EAP_SUITE_B_192 = 212602624;
        private static final int RSN_AKM_FT_EAP = 61607680;
        private static final int RSN_AKM_FT_PSK = 78384896;
        private static final int RSN_AKM_FT_SAE = 162270976;
        private static final int RSN_AKM_OWE = 313265920;
        private static final int RSN_AKM_PSK = 44830464;
        private static final int RSN_AKM_PSK_SHA256 = 111939328;
        private static final int RSN_AKM_SAE = 145493760;
        private static final int RSN_CIPHER_CCMP = 78384896;
        private static final int RSN_CIPHER_GCMP_256 = 162270976;
        private static final int RSN_CIPHER_NONE = 11276032;
        private static final int RSN_CIPHER_NO_GROUP_ADDRESSED = 128716544;
        private static final int RSN_CIPHER_TKIP = 44830464;
        private static final int WPA_AKM_EAP = 32657408;
        private static final int WPA_AKM_PSK = 49434624;
        private static final int WPA_CIPHER_CCMP = 82989056;
        private static final int WPA_CIPHER_NONE = 15880192;
        private static final int WPA_CIPHER_TKIP = 49434624;
        private static final int WPA_VENDOR_OUI_TYPE_ONE = 32657408;
        private static final short WPA_VENDOR_OUI_VERSION = 1;
        private static final int WPS_VENDOR_OUI_TYPE = 82989056;
        public ArrayList<Integer> groupCipher;
        public boolean isESS;
        public boolean isPrivacy;
        public boolean isWPS;
        public ArrayList<ArrayList<Integer>> keyManagement;
        public ArrayList<ArrayList<Integer>> pairwiseCipher;
        public String pmfCapabilities = "";
        public ArrayList<Integer> protocol;

        private void parseRsnElement(ScanResult.InformationElement ie) {
            ByteBuffer buf = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            try {
                if (buf.getShort() == 1) {
                    this.protocol.add(2);
                    this.groupCipher.add(Integer.valueOf(parseRsnCipher(buf.getInt())));
                    short cipherCount = buf.getShort();
                    ArrayList<Integer> rsnPairwiseCipher = new ArrayList<>();
                    for (int i = 0; i < cipherCount; i++) {
                        rsnPairwiseCipher.add(Integer.valueOf(parseRsnCipher(buf.getInt())));
                    }
                    this.pairwiseCipher.add(rsnPairwiseCipher);
                    short akmCount = buf.getShort();
                    ArrayList<Integer> rsnKeyManagement = new ArrayList<>();
                    ArrayList<Integer> pmfKeyManagement = new ArrayList<>();
                    for (int i2 = 0; i2 < akmCount; i2++) {
                        switch (buf.getInt()) {
                            case RSN_AKM_EAP /* 28053248 */:
                                rsnKeyManagement.add(2);
                                pmfKeyManagement.add(1);
                                break;
                            case 44830464:
                                rsnKeyManagement.add(1);
                                pmfKeyManagement.add(2);
                                break;
                            case RSN_AKM_FT_EAP /* 61607680 */:
                                rsnKeyManagement.add(4);
                                pmfKeyManagement.add(3);
                                break;
                            case 78384896:
                                rsnKeyManagement.add(3);
                                pmfKeyManagement.add(4);
                                break;
                            case RSN_AKM_EAP_SHA256 /* 95162112 */:
                                rsnKeyManagement.add(6);
                                pmfKeyManagement.add(5);
                                break;
                            case RSN_AKM_PSK_SHA256 /* 111939328 */:
                                rsnKeyManagement.add(5);
                                pmfKeyManagement.add(6);
                                break;
                            case RSN_AKM_SAE /* 145493760 */:
                                rsnKeyManagement.add(8);
                                break;
                            case RSN_AKM_EAP_SUITE_B_192 /* 212602624 */:
                                rsnKeyManagement.add(10);
                                break;
                            case RSN_AKM_OWE /* 313265920 */:
                                rsnKeyManagement.add(9);
                                break;
                        }
                    }
                    if (rsnKeyManagement.isEmpty()) {
                        rsnKeyManagement.add(2);
                    }
                    this.keyManagement.add(rsnKeyManagement);
                    this.pmfCapabilities = HwInformationElementUtilEx.parsePmfCapabilities(buf.getShort(), pmfKeyManagement);
                }
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse RSNE, buffer underflow");
            }
        }

        private static int parseWpaCipher(int cipher) {
            if (cipher == WPA_CIPHER_NONE) {
                return 0;
            }
            if (cipher == 49434624) {
                return 2;
            }
            if (cipher == 82989056) {
                return 3;
            }
            Log.w("IE_Capabilities", "Unknown WPA cipher suite: " + Integer.toHexString(cipher));
            return 0;
        }

        private static int parseRsnCipher(int cipher) {
            switch (cipher) {
                case RSN_CIPHER_NONE /* 11276032 */:
                    return 0;
                case 44830464:
                    return 2;
                case 78384896:
                    return 3;
                case RSN_CIPHER_NO_GROUP_ADDRESSED /* 128716544 */:
                    return 1;
                case 162270976:
                    return 4;
                default:
                    Log.w("IE_Capabilities", "Unknown RSN cipher suite: " + Integer.toHexString(cipher));
                    return 0;
            }
        }

        private static boolean isWpsElement(ScanResult.InformationElement ie) {
            try {
                return ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == 82989056;
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse VSA IE, buffer underflow");
                return false;
            }
        }

        private static boolean isWpaOneElement(ScanResult.InformationElement ie) {
            try {
                return ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == 32657408;
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse VSA IE, buffer underflow");
                return false;
            }
        }

        private void parseWpaOneElement(ScanResult.InformationElement ie) {
            ByteBuffer buf = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            try {
                buf.getInt();
                if (buf.getShort() == 1) {
                    this.protocol.add(1);
                    this.groupCipher.add(Integer.valueOf(parseWpaCipher(buf.getInt())));
                    short cipherCount = buf.getShort();
                    ArrayList<Integer> wpaPairwiseCipher = new ArrayList<>();
                    for (int i = 0; i < cipherCount; i++) {
                        wpaPairwiseCipher.add(Integer.valueOf(parseWpaCipher(buf.getInt())));
                    }
                    this.pairwiseCipher.add(wpaPairwiseCipher);
                    short akmCount = buf.getShort();
                    ArrayList<Integer> wpaKeyManagement = new ArrayList<>();
                    for (int i2 = 0; i2 < akmCount; i2++) {
                        int akm = buf.getInt();
                        if (akm == 32657408) {
                            wpaKeyManagement.add(2);
                        } else if (akm == 49434624) {
                            wpaKeyManagement.add(1);
                        }
                    }
                    if (wpaKeyManagement.isEmpty()) {
                        wpaKeyManagement.add(2);
                    }
                    this.keyManagement.add(wpaKeyManagement);
                }
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse type 1 WPA, buffer underflow");
            }
        }

        public void from(ScanResult.InformationElement[] ies, BitSet beaconCap, boolean isOweSupported) {
            this.protocol = new ArrayList<>();
            this.keyManagement = new ArrayList<>();
            this.groupCipher = new ArrayList<>();
            this.pairwiseCipher = new ArrayList<>();
            if (!(ies == null || beaconCap == null)) {
                this.pmfCapabilities = "NO RSN IE";
                this.isESS = beaconCap.get(0);
                this.isPrivacy = beaconCap.get(4);
                for (ScanResult.InformationElement ie : ies) {
                    if (ie.id == 48) {
                        parseRsnElement(ie);
                    }
                    if (ie.id == 221) {
                        if (isWpaOneElement(ie)) {
                            parseWpaOneElement(ie);
                        }
                        if (isWpsElement(ie)) {
                            this.isWPS = true;
                        }
                        if (isOweSupported && isOweElement(ie)) {
                            this.protocol.add(2);
                            this.groupCipher.add(3);
                            ArrayList<Integer> owePairwiseCipher = new ArrayList<>();
                            owePairwiseCipher.add(3);
                            this.pairwiseCipher.add(owePairwiseCipher);
                            ArrayList<Integer> oweKeyManagement = new ArrayList<>();
                            oweKeyManagement.add(12);
                            this.keyManagement.add(oweKeyManagement);
                        }
                    }
                    HwInformationElementUtilEx.parse80211KVRCap(ie);
                    if (ie.id == 68) {
                        HwInformationElementUtilEx.parseWapiElement(ie, this.protocol, this.keyManagement, this.pairwiseCipher);
                    }
                }
            }
        }

        private static boolean isOweElement(ScanResult.InformationElement ie) {
            try {
                return ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == OWE_VENDOR_OUI_TYPE;
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse VSA IE, buffer underflow");
                return false;
            }
        }

        private String protocolToString(int protocol2) {
            if (protocol2 == 0) {
                return "None";
            }
            if (protocol2 == 1) {
                return "WPA";
            }
            if (protocol2 == 2) {
                return "RSN";
            }
            if (protocol2 != 4) {
                return "?";
            }
            return "WAPI";
        }

        private String keyManagementToString(int akm) {
            switch (akm) {
                case 0:
                    return "None";
                case 1:
                    return "PSK";
                case 2:
                    return "EAP";
                case 3:
                    return "FT/PSK";
                case 4:
                    return "FT/EAP";
                case 5:
                    return "PSK-SHA256";
                case 6:
                    return "EAP-SHA256";
                case 7:
                default:
                    return "?";
                case 8:
                    return "SAE";
                case 9:
                    return "OWE";
                case 10:
                    return "EAP_SUITE_B_192";
                case 11:
                    return "FT/SAE";
                case 12:
                    return "OWE_TRANSITION";
                case 13:
                    return "CERT";
            }
        }

        private String cipherToString(int cipher) {
            if (cipher == 0) {
                return "None";
            }
            if (cipher == 2) {
                return "TKIP";
            }
            if (cipher == 3) {
                return "CCMP";
            }
            if (cipher != 4) {
                return "?";
            }
            return "GCMP-256";
        }

        public String generateCapabilitiesString() {
            StringBuilder capabilities = new StringBuilder();
            if (this.protocol.isEmpty() && this.isPrivacy) {
                capabilities.append("[WEP]");
            }
            for (int i = 0; i < this.protocol.size(); i++) {
                String capability = generateCapabilitiesStringPerProtocol(i);
                capabilities.append(generateWPA2CapabilitiesString(capability, i));
                capabilities.append(capability);
            }
            if (this.isESS) {
                capabilities.append("[ESS]");
            }
            if (this.isWPS) {
                capabilities.append("[WPS]");
            }
            return capabilities.toString();
        }

        private String generateCapabilitiesStringPerProtocol(int index) {
            StringBuilder capability = new StringBuilder();
            capability.append("[");
            capability.append(protocolToString(this.protocol.get(index).intValue()));
            if (index < this.keyManagement.size()) {
                int j = 0;
                while (j < this.keyManagement.get(index).size()) {
                    capability.append(j == 0 ? "-" : "+");
                    capability.append(keyManagementToString(this.keyManagement.get(index).get(j).intValue()));
                    j++;
                }
            }
            if (index < this.pairwiseCipher.size()) {
                int j2 = 0;
                while (j2 < this.pairwiseCipher.get(index).size()) {
                    capability.append(j2 == 0 ? "-" : "+");
                    capability.append(cipherToString(this.pairwiseCipher.get(index).get(j2).intValue()));
                    j2++;
                }
            }
            capability.append("]");
            return HwInformationElementUtilEx.generatePmfCapabilitiesString(HwInformationElementUtilEx.generateKVRCapabilitiesString(capability.toString()));
        }

        private String generateWPA2CapabilitiesString(String cap, int index) {
            StringBuilder capWpa2 = new StringBuilder();
            if (cap.contains("EAP_SUITE_B_192")) {
                return "";
            }
            if (!cap.contains("RSN-EAP") && !cap.contains("RSN-FT/EAP") && !cap.contains("RSN-PSK") && !cap.contains("RSN-FT/PSK")) {
                return "";
            }
            capWpa2.append("[");
            capWpa2.append("WPA2");
            if (index < this.keyManagement.size()) {
                int j = 0;
                while (j < this.keyManagement.get(index).size()) {
                    capWpa2.append(j == 0 ? "-" : "+");
                    capWpa2.append(keyManagementToString(this.keyManagement.get(index).get(j).intValue()));
                    if (cap.contains("SAE")) {
                        break;
                    }
                    j++;
                }
            }
            if (index < this.pairwiseCipher.size()) {
                int j2 = 0;
                while (j2 < this.pairwiseCipher.get(index).size()) {
                    capWpa2.append(j2 == 0 ? "-" : "+");
                    capWpa2.append(cipherToString(this.pairwiseCipher.get(index).get(j2).intValue()));
                    j2++;
                }
            }
            capWpa2.append("]");
            return capWpa2.toString();
        }
    }

    public static class TrafficIndicationMap {
        private static final int MAX_TIM_LENGTH = 254;
        public int mBitmapControl = 0;
        public int mDtimCount = -1;
        public int mDtimPeriod = -1;
        public int mLength = 0;
        private boolean mValid = false;

        public boolean isValid() {
            return this.mValid;
        }

        public void from(ScanResult.InformationElement ie) {
            this.mValid = false;
            if (ie != null && ie.bytes != null) {
                this.mLength = ie.bytes.length;
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                try {
                    this.mDtimCount = data.get() & 255;
                    this.mDtimPeriod = data.get() & 255;
                    this.mBitmapControl = data.get() & 255;
                    data.get();
                    if (this.mLength <= MAX_TIM_LENGTH && this.mDtimPeriod > 0) {
                        this.mValid = true;
                    }
                } catch (BufferUnderflowException e) {
                }
            }
        }
    }

    public static class WifiMode {
        public static final int MODE_11A = 1;
        public static final int MODE_11AC = 5;
        public static final int MODE_11B = 2;
        public static final int MODE_11G = 3;
        public static final int MODE_11N = 4;
        public static final int MODE_UNDEFINED = 0;

        public static int determineMode(int frequency, int maxRate, boolean foundVht, boolean foundHt, boolean foundErp) {
            if (foundVht) {
                return 5;
            }
            if (foundHt) {
                return 4;
            }
            if (foundErp) {
                return 3;
            }
            if (frequency >= 3000) {
                return 1;
            }
            if (maxRate < 24000000) {
                return 2;
            }
            return 3;
        }

        public static String toString(int mode) {
            if (mode == 1) {
                return "MODE_11A";
            }
            if (mode == 2) {
                return "MODE_11B";
            }
            if (mode == 3) {
                return "MODE_11G";
            }
            if (mode == 4) {
                return "MODE_11N";
            }
            if (mode != 5) {
                return "MODE_UNDEFINED";
            }
            return "MODE_11AC";
        }
    }

    public static class SupportedRates {
        public static final int MASK = 127;
        public ArrayList<Integer> mRates = new ArrayList<>();
        public boolean mValid = false;

        public boolean isValid() {
            return this.mValid;
        }

        public static int getRateFromByte(int byteVal) {
            switch (byteVal & 127) {
                case 2:
                    return 1000000;
                case 4:
                    return 2000000;
                case 11:
                    return 5500000;
                case 12:
                    return 6000000;
                case 18:
                    return 9000000;
                case 22:
                    return 11000000;
                case 24:
                    return 12000000;
                case 36:
                    return 18000000;
                case ISupplicantStaIfaceCallback.StatusCode.UNSUPPORTED_RSN_IE_VERSION /* 44 */:
                    return 22000000;
                case 48:
                    return 24000000;
                case ISupplicantStaIfaceCallback.ReasonCode.MESH_CHANNEL_SWITCH_UNSPECIFIED /* 66 */:
                    return 33000000;
                case ISupplicantStaIfaceCallback.StatusCode.INVALID_RSNIE /* 72 */:
                    return 36000000;
                case ISupplicantStaIfaceCallback.StatusCode.REJECT_DSE_BAND /* 96 */:
                    return 48000000;
                case 108:
                    return 54000000;
                default:
                    return -1;
            }
        }

        public void from(ScanResult.InformationElement ie) {
            this.mValid = false;
            if (ie != null && ie.bytes != null && ie.bytes.length <= 8 && ie.bytes.length >= 1) {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < ie.bytes.length; i++) {
                    try {
                        int rate = getRateFromByte(data.get());
                        if (rate > 0) {
                            this.mRates.add(Integer.valueOf(rate));
                        } else {
                            return;
                        }
                    } catch (BufferUnderflowException e) {
                        return;
                    }
                }
                this.mValid = true;
            }
        }

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            Iterator<Integer> it = this.mRates.iterator();
            while (it.hasNext()) {
                sbuf.append(String.format("%.1f", Double.valueOf(((double) it.next().intValue()) / 1000000.0d)) + ", ");
            }
            return sbuf.toString();
        }
    }
}
