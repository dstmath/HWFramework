package com.android.server.wifi.util;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hardware.wifi.supplicant.V1_0.WpsConfigMethods;
import android.net.wifi.ScanResult;
import android.util.Log;
import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class InformationElementUtil {
    /* access modifiers changed from: private */
    public static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "InformationElementUtil";

    public static class APCapInfo {
        private int mStream1 = 0;
        private int mStream2 = 0;
        private int mStream3 = 0;
        private int mStream4 = 0;
        private int mTxMcsSet = 0;

        public int getTxMcsSet() {
            return this.mTxMcsSet;
        }

        public int getStream1() {
            return this.mStream1;
        }

        public int getStream2() {
            return this.mStream2;
        }

        public int getStream3() {
            return this.mStream3;
        }

        public int getStream4() {
            return this.mStream4;
        }

        public void from(ScanResult.InformationElement ie) {
            try {
                if (ie.id != 45) {
                    throw new IllegalArgumentException("Element id is not APCapInfo, : " + ie.id);
                } else if (ie.bytes.length >= 16) {
                    ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                    data.position(data.position() + 3);
                    this.mStream1 = data.get();
                    this.mStream2 = data.get();
                    this.mStream3 = data.get();
                    this.mStream4 = data.get();
                    int i = 1;
                    this.mStream1 = this.mStream1 == 0 ? 0 : 1;
                    this.mStream2 = this.mStream2 == 0 ? 0 : 1;
                    this.mStream3 = this.mStream3 == 0 ? 0 : 1;
                    if (this.mStream4 == 0) {
                        i = 0;
                    }
                    this.mStream4 = i;
                    data.position(data.position() + 8);
                    this.mTxMcsSet = (data.get() >> 4) & 15;
                } else {
                    throw new IllegalArgumentException("APCapInfo length smaller than 16: " + ie.bytes.length);
                }
            } catch (IllegalArgumentException e) {
                if (InformationElementUtil.HWFLOW) {
                    Log.d(InformationElementUtil.TAG, "APCapInfo" + e);
                }
            }
        }
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

    public static class Capabilities {
        private static final int CAP_ESS_BIT_OFFSET = 0;
        private static final int CAP_PRIVACY_BIT_OFFSET = 4;
        private static final int CAP_WAPI_BIT_OFFSET = 7;
        private static final byte MASK_11V = 8;
        private static final int OFFSET_11V = 2;
        private static final int PMF_C = 1;
        private static final int PMF_INVALID = 3;
        private static final int PMF_KEY_MGMT_EAP = 1;
        private static final int PMF_KEY_MGMT_EAP_SHA256 = 5;
        private static final int PMF_KEY_MGMT_FT_EAP = 3;
        private static final int PMF_KEY_MGMT_FT_PSK = 4;
        private static final int PMF_KEY_MGMT_PSK = 2;
        private static final int PMF_KEY_MGMT_PSK_SHA256 = 6;
        private static final int PMF_NONE = 0;
        private static final int PMF_R = 2;
        private static final short RSNE_VERSION = 1;
        private static final int RSN_CIPHER_CCMP = 78384896;
        private static final int RSN_CIPHER_NONE = 11276032;
        private static final int RSN_CIPHER_NO_GROUP_ADDRESSED = 128716544;
        private static final int RSN_CIPHER_TKIP = 44830464;
        private static final int RSN_MFPC_BIT_OFFSET = 7;
        private static final int RSN_MFPR_BIT_OFFSET = 6;
        private static final byte WAPI_AKM_CERT = 1;
        private static final byte WAPI_AKM_PSK = 2;
        private static final int WPA2_AKM_EAP = 28053248;
        private static final int WPA2_AKM_EAP_SHA256 = 95162112;
        private static final int WPA2_AKM_FT_EAP = 61607680;
        private static final int WPA2_AKM_FT_PSK = 78384896;
        private static final int WPA2_AKM_PSK = 44830464;
        private static final int WPA2_AKM_PSK_SHA256 = 111939328;
        private static final int WPA_AKM_EAP = 32657408;
        private static final int WPA_AKM_PSK = 49434624;
        private static final int WPA_CIPHER_CCMP = 82989056;
        private static final int WPA_CIPHER_NONE = 15880192;
        private static final int WPA_CIPHER_TKIP = 49434624;
        private static final int WPA_VENDOR_OUI_TYPE_ONE = 32657408;
        private static final short WPA_VENDOR_OUI_VERSION = 1;
        private static final int WPS_VENDOR_OUI_TYPE = 82989056;
        public ArrayList<Integer> groupCipher;
        private boolean is80211K = false;
        private boolean is80211R = false;
        private boolean is80211V = false;
        public boolean isESS;
        public boolean isPrivacy;
        public boolean isWPS;
        public ArrayList<ArrayList<Integer>> keyManagement;
        public ArrayList<ArrayList<Integer>> pairwiseCipher;
        public String pmfCapabilities = "";
        private int pmfType = 0;
        public ArrayList<Integer> protocol;

        private void parseRsnElement(ScanResult.InformationElement ie) {
            int i;
            ByteBuffer buf = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            try {
                if (buf.getShort() == 1) {
                    this.protocol.add(2);
                    this.groupCipher.add(Integer.valueOf(parseRsnCipher(buf.getInt())));
                    short cipherCount = buf.getShort();
                    ArrayList<Integer> rsnPairwiseCipher = new ArrayList<>();
                    for (int i2 = 0; i2 < cipherCount; i2++) {
                        rsnPairwiseCipher.add(Integer.valueOf(parseRsnCipher(buf.getInt())));
                    }
                    this.pairwiseCipher.add(rsnPairwiseCipher);
                    short akmCount = buf.getShort();
                    ArrayList<Integer> rsnKeyManagement = new ArrayList<>();
                    ArrayList<Integer> pmfKeyManagement = new ArrayList<>();
                    for (int i3 = 0; i3 < akmCount; i3++) {
                        switch (buf.getInt()) {
                            case WPA2_AKM_EAP /*28053248*/:
                                rsnKeyManagement.add(2);
                                pmfKeyManagement.add(1);
                                break;
                            case 44830464:
                                rsnKeyManagement.add(1);
                                pmfKeyManagement.add(2);
                                break;
                            case WPA2_AKM_FT_EAP /*61607680*/:
                                rsnKeyManagement.add(4);
                                pmfKeyManagement.add(3);
                                break;
                            case 78384896:
                                rsnKeyManagement.add(3);
                                pmfKeyManagement.add(4);
                                break;
                            case WPA2_AKM_EAP_SHA256 /*95162112*/:
                                rsnKeyManagement.add(6);
                                pmfKeyManagement.add(5);
                                break;
                            case WPA2_AKM_PSK_SHA256 /*111939328*/:
                                rsnKeyManagement.add(5);
                                pmfKeyManagement.add(6);
                                break;
                        }
                    }
                    if (rsnKeyManagement.isEmpty()) {
                        rsnKeyManagement.add(2);
                    }
                    this.keyManagement.add(rsnKeyManagement);
                    this.pmfCapabilities = "";
                    short pmf = buf.getShort();
                    if ((pmf & WpsConfigMethods.PUSHBUTTON) != 0) {
                        this.pmfCapabilities += "C:1 ";
                        this.pmfType = 1;
                        i = 0;
                    } else {
                        this.pmfCapabilities += "C:0 ";
                        i = 0;
                        this.pmfType = 0;
                    }
                    if ((pmf & 64) != 0) {
                        this.pmfCapabilities += "R:1 ";
                        if (this.pmfType == 0) {
                            this.pmfType = 3;
                        } else {
                            this.pmfType = 2;
                        }
                    } else {
                        this.pmfCapabilities += "R:0 ";
                    }
                    this.pmfCapabilities += "K:";
                    if (pmfKeyManagement.isEmpty()) {
                        this.pmfCapabilities += "NONE";
                    } else {
                        int rsnKeyCount = pmfKeyManagement.size();
                        while (i < rsnKeyCount) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(this.pmfCapabilities);
                            sb.append(i == 0 ? "" : "+");
                            sb.append(String.valueOf(pmfKeyManagement.get(i)));
                            this.pmfCapabilities = sb.toString();
                            i++;
                        }
                    }
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
            if (cipher == RSN_CIPHER_NONE) {
                return 0;
            }
            if (cipher == 44830464) {
                return 2;
            }
            if (cipher == 78384896) {
                return 3;
            }
            if (cipher == RSN_CIPHER_NO_GROUP_ADDRESSED) {
                return 1;
            }
            Log.w("IE_Capabilities", "Unknown RSN cipher suite: " + Integer.toHexString(cipher));
            return 0;
        }

        private static boolean isWpsElement(ScanResult.InformationElement ie) {
            boolean z = false;
            try {
                if (ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == 82989056) {
                    z = true;
                }
                return z;
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse VSA IE, buffer underflow");
                return false;
            }
        }

        private static boolean isWpaOneElement(ScanResult.InformationElement ie) {
            boolean z = false;
            try {
                if (ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == 32657408) {
                    z = true;
                }
                return z;
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

        private void parseWapiElement(ScanResult.InformationElement ie) {
            if (ie.bytes != null && ie.bytes.length > 7) {
                this.protocol.add(4);
                ArrayList<Integer> wapiKeyManagement = new ArrayList<>();
                ArrayList<Integer> wapiPairwiseCipher = new ArrayList<>();
                byte akm = ie.bytes[7];
                switch (akm) {
                    case 1:
                        Log.d(InformationElementUtil.TAG, "parseWapiElement: This is a WAPI CERT network");
                        wapiKeyManagement.add(8);
                        break;
                    case 2:
                        Log.d(InformationElementUtil.TAG, "parseWapiElement: This is a WAPI PSK network");
                        wapiKeyManagement.add(1);
                        break;
                    default:
                        Log.e(InformationElementUtil.TAG, "parseWapiElement: akm=" + akm + " Unknown WAPI network type");
                        break;
                }
                this.keyManagement.add(wapiKeyManagement);
                this.pairwiseCipher.add(wapiPairwiseCipher);
            }
        }

        public void from(ScanResult.InformationElement[] ies, BitSet beaconCap) {
            this.protocol = new ArrayList<>();
            this.keyManagement = new ArrayList<>();
            this.groupCipher = new ArrayList<>();
            this.pairwiseCipher = new ArrayList<>();
            if (ies != null && beaconCap != null) {
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
                    }
                    if (ie.id == 70) {
                        this.is80211K = true;
                    }
                    if (ie.id == 127 && ie.bytes.length > 2) {
                        this.is80211V = (ie.bytes[2] & 8) == 8;
                    }
                    if (ie.id == 54) {
                        this.is80211R = true;
                    }
                    if (ie.id == 68) {
                        parseWapiElement(ie);
                    }
                }
            }
        }

        private String protocolToString(int protocol2) {
            if (protocol2 == 4) {
                return "WAPI";
            }
            switch (protocol2) {
                case 0:
                    return "None";
                case 1:
                    return "WPA";
                case 2:
                    return "WPA2";
                default:
                    return "?";
            }
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
                case 8:
                    return "CERT";
                default:
                    return "?";
            }
        }

        private String cipherToString(int cipher) {
            if (cipher == 0) {
                return "None";
            }
            switch (cipher) {
                case 2:
                    return "TKIP";
                case 3:
                    return "CCMP";
                default:
                    return "?";
            }
        }

        public String generateCapabilitiesString() {
            String capabilities = "";
            if (this.protocol.isEmpty() && this.isPrivacy) {
                capabilities = capabilities + "[WEP]";
            }
            String capabilities2 = capabilities;
            for (int i = 0; i < this.protocol.size(); i++) {
                String capabilities3 = capabilities2 + "[" + protocolToString(this.protocol.get(i).intValue());
                if (i < this.keyManagement.size()) {
                    String capabilities4 = capabilities3;
                    int j = 0;
                    while (j < this.keyManagement.get(i).size()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(capabilities4);
                        sb.append(j == 0 ? "-" : "+");
                        sb.append(keyManagementToString(((Integer) this.keyManagement.get(i).get(j)).intValue()));
                        capabilities4 = sb.toString();
                        j++;
                    }
                    capabilities3 = capabilities4;
                }
                if (i < this.pairwiseCipher.size()) {
                    String capabilities5 = capabilities3;
                    int j2 = 0;
                    while (j2 < this.pairwiseCipher.get(i).size()) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(capabilities5);
                        sb2.append(j2 == 0 ? "-" : "+");
                        sb2.append(cipherToString(((Integer) this.pairwiseCipher.get(i).get(j2)).intValue()));
                        capabilities5 = sb2.toString();
                        j2++;
                    }
                    capabilities3 = capabilities5;
                }
                capabilities2 = capabilities3 + "]";
            }
            if (this.isESS != 0) {
                capabilities2 = capabilities2 + "[ESS]";
            }
            if (this.isWPS) {
                capabilities2 = capabilities2 + "[WPS]";
            }
            if (this.is80211K) {
                capabilities2 = capabilities2 + "[K]";
            }
            if (this.is80211V) {
                capabilities2 = capabilities2 + "[V]";
            }
            if (this.is80211R) {
                capabilities2 = capabilities2 + "[R]";
            }
            if (this.pmfType == 1) {
                capabilities2 = capabilities2 + "[PMFC]";
            }
            if (this.pmfType == 2) {
                capabilities2 = capabilities2 + "[PMFR]";
            }
            if (this.pmfType != 3) {
                return capabilities2;
            }
            return capabilities2 + "[PMFE]";
        }
    }

    public static class Dot11vNetwork {
        private static final byte MASK_11V = 8;
        private static final int OFFSET_11V = 2;
        public boolean dot11vNetwork = false;

        public void from(ScanResult.InformationElement[] ies) {
            if (ies != null) {
                for (ScanResult.InformationElement ie : ies) {
                    if (127 == ie.id && ie.bytes.length > 2) {
                        this.dot11vNetwork = (ie.bytes[2] & 8) == 8;
                    }
                }
            }
        }
    }

    private static class EncryptionType {
        public ArrayList<Integer> keyManagement = new ArrayList<>();
        public ArrayList<Integer> pairwiseCipher = new ArrayList<>();
        public int protocol = 0;

        public EncryptionType(int protocol2, ArrayList<Integer> keyManagement2, ArrayList<Integer> pairwiseCipher2) {
            this.protocol = protocol2;
            this.keyManagement = keyManagement2;
            this.pairwiseCipher = pairwiseCipher2;
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

    public static class HiLinkNetwork {
        private static final int[] FORMAT_HILINK = {0, 224, 252, 128, 0, 0, 0, 1, 0};
        private static final int[] FORMAT_HILINK_OUI = {0, 224, 252, 64, 0, 0, 0};
        private static final int HILINK_OUI_HEAD_LEN = 9;
        private static final int LOGO_ID = 249;
        private static final int MASK_HILINK = 255;
        private static final int NORMAL_WIFI = 0;
        public boolean isHiLinkNetwork = false;

        public int parseHiLogoTag(ScanResult.InformationElement[] ies) {
            int element;
            if (ies == null) {
                return 0;
            }
            int length = ies.length;
            for (int i = 0; i < length; i++) {
                ScanResult.InformationElement ie = ies[i];
                if (221 == ie.id && checkHiLinkOUISection(ie.bytes)) {
                    int index = 9;
                    while (index < ie.bytes.length && index >= 0) {
                        Log.d(InformationElementUtil.TAG, "element:" + element);
                        if (element == LOGO_ID) {
                            return ie.bytes[index + 2];
                        }
                        try {
                            index += ie.bytes[index + 1] + 2;
                        } catch (IndexOutOfBoundsException e) {
                            Log.w(InformationElementUtil.TAG, "the information elements is invalid");
                            return 0;
                        }
                    }
                }
            }
            return 0;
        }

        private boolean checkHiLinkOUISection(byte[] bytes) {
            if (bytes == null || bytes.length < FORMAT_HILINK_OUI.length) {
                Log.w(InformationElementUtil.TAG, "the information elements's length is invalid");
                return false;
            }
            for (int index = 0; index < FORMAT_HILINK_OUI.length; index++) {
                if ((bytes[index] & 255) != FORMAT_HILINK_OUI[index]) {
                    return false;
                }
            }
            return true;
        }

        public void from(ScanResult.InformationElement[] ies) {
            if (ies != null) {
                for (ScanResult.InformationElement ie : ies) {
                    if (221 == ie.id && checkHiLinkSection(ie.bytes)) {
                        this.isHiLinkNetwork = true;
                    }
                }
            }
        }

        private boolean checkHiLinkSection(byte[] bytes) {
            if (bytes == null || bytes.length < FORMAT_HILINK.length) {
                return false;
            }
            for (int index = 0; index < FORMAT_HILINK.length; index++) {
                if ((bytes[index] & 255) != FORMAT_HILINK[index]) {
                    return false;
                }
            }
            return true;
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
            if (this.secondChannelOffset == 0) {
                return 0;
            }
            if (this.secondChannelOffset == 1) {
                return primaryFrequency + 10;
            }
            if (this.secondChannelOffset == 3) {
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

    public static class Interworking {
        public NetworkDetail.Ant ant = null;
        public long hessid = 0;
        public boolean internet = false;

        public void from(ScanResult.InformationElement ie) {
            if (ie.id == 107) {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                int anOptions = data.get() & Constants.BYTE_MASK;
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
                int oi12Length = data.get() & Constants.BYTE_MASK;
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
                if (oi1Length > 0 && this.roamingConsortiums.length > 0) {
                    this.roamingConsortiums[0] = ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, oi1Length);
                }
                if (oi2Length > 0 && this.roamingConsortiums.length > 1) {
                    this.roamingConsortiums[1] = ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, oi2Length);
                }
                if (oi3Length > 0 && this.roamingConsortiums.length > 2) {
                    this.roamingConsortiums[2] = ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, oi3Length);
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("Element id is not ROAMING_CONSORTIUM, : " + ie.id);
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
                case ISupplicantStaIfaceCallback.ReasonCode.STA_LEAVING:
                    return 18000000;
                case ISupplicantStaIfaceCallback.StatusCode.UNSUPPORTED_RSN_IE_VERSION:
                    return 22000000;
                case 48:
                    return 24000000;
                case ISupplicantStaIfaceCallback.ReasonCode.MESH_CHANNEL_SWITCH_UNSPECIFIED:
                    return 33000000;
                case ISupplicantStaIfaceCallback.StatusCode.INVALID_RSNIE:
                    return 36000000;
                case ISupplicantStaIfaceCallback.StatusCode.REJECT_DSE_BAND:
                    return 48000000;
                case 108:
                    return 54000000;
                default:
                    return -1;
            }
        }

        public void from(ScanResult.InformationElement ie) {
            int i = 0;
            this.mValid = false;
            if (ie != null && ie.bytes != null && ie.bytes.length <= 8 && ie.bytes.length >= 1) {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                while (i < ie.bytes.length) {
                    try {
                        int rate = getRateFromByte(data.get());
                        if (rate > 0) {
                            this.mRates.add(Integer.valueOf(rate));
                            i++;
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
                sbuf.append(String.format("%.1f", new Object[]{Double.valueOf(((double) it.next().intValue()) / 1000000.0d)}) + ", ");
            }
            return sbuf.toString();
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

    public static class VhtOperation {
        public int centerFreqIndex1 = 0;
        public int centerFreqIndex2 = 0;
        public int channelMode = 0;

        public boolean isValid() {
            return this.channelMode != 0;
        }

        public int getChannelWidth() {
            return this.channelMode + 1;
        }

        public int getCenterFreq0() {
            return ((this.centerFreqIndex1 - 36) * 5) + 5180;
        }

        public int getCenterFreq1() {
            if (this.channelMode > 1) {
                return ((this.centerFreqIndex2 - 36) * 5) + 5180;
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

    public static class Vsa {
        private static final int ANQP_DOMID_BIT = 4;
        public int anqpDomainID = 0;
        public NetworkDetail.HSRelease hsRelease = null;

        public void from(ScanResult.InformationElement ie) {
            ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            if (ie.bytes.length >= 5 && data.getInt() == 278556496) {
                int hsConf = data.get() & Constants.BYTE_MASK;
                switch ((hsConf >> 4) & 15) {
                    case 0:
                        this.hsRelease = NetworkDetail.HSRelease.R1;
                        break;
                    case 1:
                        this.hsRelease = NetworkDetail.HSRelease.R2;
                        break;
                    default:
                        this.hsRelease = NetworkDetail.HSRelease.Unknown;
                        break;
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
            switch (mode) {
                case 1:
                    return "MODE_11A";
                case 2:
                    return "MODE_11B";
                case 3:
                    return "MODE_11G";
                case 4:
                    return "MODE_11N";
                case 5:
                    return "MODE_11AC";
                default:
                    return "MODE_UNDEFINED";
            }
        }
    }

    public static ScanResult.InformationElement[] parseInformationElements(byte[] bytes) {
        boolean found_ssid = false;
        if (bytes == null) {
            return new ScanResult.InformationElement[0];
        }
        ByteBuffer data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        ArrayList<ScanResult.InformationElement> infoElements = new ArrayList<>();
        while (data.remaining() > 1) {
            int eid = data.get() & Constants.BYTE_MASK;
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
}
