package com.android.server.wifi.util;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.ReasonCode;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.StatusCode;
import android.net.wifi.ScanResult.InformationElement;
import android.util.Log;
import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.hotspot2.NetworkDetail.Ant;
import com.android.server.wifi.hotspot2.NetworkDetail.HSRelease;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.eap.AuthParam;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class InformationElementUtil {
    private static final boolean HWFLOW;
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

        public void from(InformationElement ie) {
            int i = 0;
            try {
                if (ie.id != 45) {
                    throw new IllegalArgumentException("Element id is not APCapInfo, : " + ie.id);
                } else if (ie.bytes.length < 16) {
                    throw new IllegalArgumentException("APCapInfo length smaller than 16: " + ie.bytes.length);
                } else {
                    int i2;
                    ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                    data.position(data.position() + 3);
                    this.mStream1 = data.get();
                    this.mStream2 = data.get();
                    this.mStream3 = data.get();
                    this.mStream4 = data.get();
                    if (this.mStream1 == 0) {
                        i2 = 0;
                    } else {
                        i2 = 1;
                    }
                    this.mStream1 = i2;
                    if (this.mStream2 == 0) {
                        i2 = 0;
                    } else {
                        i2 = 1;
                    }
                    this.mStream2 = i2;
                    if (this.mStream3 == 0) {
                        i2 = 0;
                    } else {
                        i2 = 1;
                    }
                    this.mStream3 = i2;
                    if (this.mStream4 != 0) {
                        i = 1;
                    }
                    this.mStream4 = i;
                    data.position(data.position() + 8);
                    this.mTxMcsSet = (data.get() >> 4) & 15;
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

        public void from(InformationElement ie) {
            if (ie.id != 11) {
                throw new IllegalArgumentException("Element id is not BSS_LOAD, : " + ie.id);
            } else if (ie.bytes.length != 5) {
                throw new IllegalArgumentException("BSS Load element length is not 5: " + ie.bytes.length);
            } else {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                this.stationCount = data.getShort() & Constants.SHORT_MASK;
                this.channelUtilization = data.get() & Constants.BYTE_MASK;
                this.capacity = data.getShort() & Constants.SHORT_MASK;
            }
        }
    }

    public static class Capabilities {
        private static final int CAP_ESS_BIT_OFFSET = 0;
        private static final int CAP_PRIVACY_BIT_OFFSET = 4;
        private static final int CAP_WAPI_BIT_OFFSET = 7;
        private static final short RSNE_VERSION = (short) 1;
        private static final int RSN_CIPHER_CCMP = 78384896;
        private static final int RSN_CIPHER_NONE = 11276032;
        private static final int RSN_CIPHER_NO_GROUP_ADDRESSED = 128716544;
        private static final int RSN_CIPHER_TKIP = 44830464;
        private static final byte WAPI_AKM_CERT = (byte) 1;
        private static final byte WAPI_AKM_PSK = (byte) 2;
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
        private static final short WPA_VENDOR_OUI_VERSION = (short) 1;
        private static final int WPS_VENDOR_OUI_TYPE = 82989056;
        public ArrayList<Integer> groupCipher;
        public boolean isESS;
        public boolean isPrivacy;
        public boolean isWPS;
        public ArrayList<ArrayList<Integer>> keyManagement;
        public ArrayList<ArrayList<Integer>> pairwiseCipher;
        public ArrayList<Integer> protocol;

        private void parseRsnElement(InformationElement ie) {
            ByteBuffer buf = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            try {
                if (buf.getShort() == (short) 1) {
                    short i;
                    this.protocol.add(Integer.valueOf(2));
                    this.groupCipher.add(Integer.valueOf(parseRsnCipher(buf.getInt())));
                    short cipherCount = buf.getShort();
                    ArrayList<Integer> rsnPairwiseCipher = new ArrayList();
                    for (i = (short) 0; i < cipherCount; i++) {
                        rsnPairwiseCipher.add(Integer.valueOf(parseRsnCipher(buf.getInt())));
                    }
                    this.pairwiseCipher.add(rsnPairwiseCipher);
                    short akmCount = buf.getShort();
                    ArrayList<Integer> rsnKeyManagement = new ArrayList();
                    for (i = (short) 0; i < akmCount; i++) {
                        switch (buf.getInt()) {
                            case WPA2_AKM_EAP /*28053248*/:
                                rsnKeyManagement.add(Integer.valueOf(2));
                                break;
                            case 44830464:
                                rsnKeyManagement.add(Integer.valueOf(1));
                                break;
                            case WPA2_AKM_FT_EAP /*61607680*/:
                                rsnKeyManagement.add(Integer.valueOf(4));
                                break;
                            case 78384896:
                                rsnKeyManagement.add(Integer.valueOf(3));
                                break;
                            case WPA2_AKM_EAP_SHA256 /*95162112*/:
                                rsnKeyManagement.add(Integer.valueOf(6));
                                break;
                            case WPA2_AKM_PSK_SHA256 /*111939328*/:
                                rsnKeyManagement.add(Integer.valueOf(5));
                                break;
                            default:
                                break;
                        }
                    }
                    if (rsnKeyManagement.isEmpty()) {
                        rsnKeyManagement.add(Integer.valueOf(2));
                    }
                    this.keyManagement.add(rsnKeyManagement);
                }
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse RSNE, buffer underflow");
            }
        }

        private static int parseWpaCipher(int cipher) {
            switch (cipher) {
                case WPA_CIPHER_NONE /*15880192*/:
                    return 0;
                case 49434624:
                    return 2;
                case 82989056:
                    return 3;
                default:
                    Log.w("IE_Capabilities", "Unknown WPA cipher suite: " + Integer.toHexString(cipher));
                    return 0;
            }
        }

        private static int parseRsnCipher(int cipher) {
            switch (cipher) {
                case RSN_CIPHER_NONE /*11276032*/:
                    return 0;
                case 44830464:
                    return 2;
                case 78384896:
                    return 3;
                case RSN_CIPHER_NO_GROUP_ADDRESSED /*128716544*/:
                    return 1;
                default:
                    Log.w("IE_Capabilities", "Unknown RSN cipher suite: " + Integer.toHexString(cipher));
                    return 0;
            }
        }

        private static boolean isWpsElement(InformationElement ie) {
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

        private static boolean isWpaOneElement(InformationElement ie) {
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

        private void parseWpaOneElement(InformationElement ie) {
            ByteBuffer buf = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            try {
                buf.getInt();
                if (buf.getShort() == (short) 1) {
                    short i;
                    this.protocol.add(Integer.valueOf(1));
                    this.groupCipher.add(Integer.valueOf(parseWpaCipher(buf.getInt())));
                    short cipherCount = buf.getShort();
                    ArrayList<Integer> wpaPairwiseCipher = new ArrayList();
                    for (i = (short) 0; i < cipherCount; i++) {
                        wpaPairwiseCipher.add(Integer.valueOf(parseWpaCipher(buf.getInt())));
                    }
                    this.pairwiseCipher.add(wpaPairwiseCipher);
                    short akmCount = buf.getShort();
                    ArrayList<Integer> wpaKeyManagement = new ArrayList();
                    for (i = (short) 0; i < akmCount; i++) {
                        switch (buf.getInt()) {
                            case 32657408:
                                wpaKeyManagement.add(Integer.valueOf(2));
                                break;
                            case 49434624:
                                wpaKeyManagement.add(Integer.valueOf(1));
                                break;
                            default:
                                break;
                        }
                    }
                    if (wpaKeyManagement.isEmpty()) {
                        wpaKeyManagement.add(Integer.valueOf(2));
                    }
                    this.keyManagement.add(wpaKeyManagement);
                }
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse type 1 WPA, buffer underflow");
            }
        }

        private void parseWapiElement(InformationElement ie) {
            if (ie.bytes != null && ie.bytes.length > 7) {
                this.protocol.add(Integer.valueOf(4));
                ArrayList<Integer> wapiKeyManagement = new ArrayList();
                ArrayList<Integer> wapiPairwiseCipher = new ArrayList();
                byte akm = ie.bytes[7];
                switch (akm) {
                    case (byte) 1:
                        Log.d(InformationElementUtil.TAG, "parseWapiElement: This is a WAPI CERT network");
                        wapiKeyManagement.add(Integer.valueOf(8));
                        break;
                    case (byte) 2:
                        Log.d(InformationElementUtil.TAG, "parseWapiElement: This is a WAPI PSK network");
                        wapiKeyManagement.add(Integer.valueOf(1));
                        break;
                    default:
                        Log.e(InformationElementUtil.TAG, "parseWapiElement: akm=" + akm + " Unknown WAPI network type");
                        break;
                }
                this.keyManagement.add(wapiKeyManagement);
                this.pairwiseCipher.add(wapiPairwiseCipher);
            }
        }

        public void from(InformationElement[] ies, BitSet beaconCap) {
            int i = 0;
            this.protocol = new ArrayList();
            this.keyManagement = new ArrayList();
            this.groupCipher = new ArrayList();
            this.pairwiseCipher = new ArrayList();
            if (ies != null && beaconCap != null) {
                this.isESS = beaconCap.get(0);
                this.isPrivacy = beaconCap.get(4);
                int length = ies.length;
                while (i < length) {
                    InformationElement ie = ies[i];
                    if (ie.id == 48) {
                        parseRsnElement(ie);
                    }
                    if (ie.id == AuthParam.PARAM_TYPE_VENDOR_SPECIFIC) {
                        if (isWpaOneElement(ie)) {
                            parseWpaOneElement(ie);
                        }
                        if (isWpsElement(ie)) {
                            this.isWPS = true;
                        }
                    }
                    if (ie.id == 68) {
                        parseWapiElement(ie);
                    }
                    i++;
                }
            }
        }

        private String protocolToString(int protocol) {
            switch (protocol) {
                case 0:
                    return "None";
                case 1:
                    return "WPA";
                case 2:
                    return "WPA2";
                case 4:
                    return "WAPI";
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
            switch (cipher) {
                case 0:
                    return "None";
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
            if (this.protocol.isEmpty() ? this.isPrivacy : false) {
                capabilities = capabilities + "[WEP]";
            }
            for (int i = 0; i < this.protocol.size(); i++) {
                int j;
                capabilities = capabilities + "[" + protocolToString(((Integer) this.protocol.get(i)).intValue());
                if (i < this.keyManagement.size()) {
                    j = 0;
                    while (j < ((ArrayList) this.keyManagement.get(i)).size()) {
                        capabilities = capabilities + (j == 0 ? "-" : "+") + keyManagementToString(((Integer) ((ArrayList) this.keyManagement.get(i)).get(j)).intValue());
                        j++;
                    }
                }
                if (i < this.pairwiseCipher.size()) {
                    j = 0;
                    while (j < ((ArrayList) this.pairwiseCipher.get(i)).size()) {
                        capabilities = capabilities + (j == 0 ? "-" : "+") + cipherToString(((Integer) ((ArrayList) this.pairwiseCipher.get(i)).get(j)).intValue());
                        j++;
                    }
                }
                capabilities = capabilities + "]";
            }
            if (this.isESS) {
                capabilities = capabilities + "[ESS]";
            }
            if (this.isWPS) {
                return capabilities + "[WPS]";
            }
            return capabilities;
        }
    }

    public static class Dot11vNetwork {
        private static final byte MASK_11V = (byte) 8;
        private static final int OFFSET_11V = 2;
        public boolean dot11vNetwork = false;

        public void from(InformationElement[] ies) {
            if (ies != null) {
                for (InformationElement ie : ies) {
                    if (127 == ie.id && ie.bytes.length > 2) {
                        boolean z;
                        if ((ie.bytes[2] & 8) == 8) {
                            z = true;
                        } else {
                            z = false;
                        }
                        this.dot11vNetwork = z;
                    }
                }
            }
        }
    }

    private static class EncryptionType {
        public ArrayList<Integer> keyManagement = new ArrayList();
        public ArrayList<Integer> pairwiseCipher = new ArrayList();
        public int protocol = 0;

        public EncryptionType(int protocol, ArrayList<Integer> keyManagement, ArrayList<Integer> pairwiseCipher) {
            this.protocol = protocol;
            this.keyManagement = keyManagement;
            this.pairwiseCipher = pairwiseCipher;
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

        public void from(InformationElement ie) {
            this.capabilitiesBitSet = BitSet.valueOf(ie.bytes);
        }
    }

    public static class HiLinkNetwork {
        private static final int[] FORMAT_HILINK = new int[]{0, 224, 252, 128, 0, 0, 0, 1, 0};
        private static final int MASK_HILINK = 255;
        public boolean isHiLinkNetwork = false;

        public void from(InformationElement[] ies) {
            if (ies != null) {
                for (InformationElement ie : ies) {
                    if (AuthParam.PARAM_TYPE_VENDOR_SPECIFIC == ie.id && checkHiLinkSection(ie.bytes)) {
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

        public void from(InformationElement ie) {
            if (ie.id != 61) {
                throw new IllegalArgumentException("Element id is not HT_OPERATION, : " + ie.id);
            }
            this.secondChannelOffset = ie.bytes[1] & 3;
        }
    }

    public static class Interworking {
        public Ant ant = null;
        public long hessid = 0;
        public boolean internet = false;

        public void from(InformationElement ie) {
            boolean z = false;
            if (ie.id != StatusCode.AUTHORIZATION_DEENABLED) {
                throw new IllegalArgumentException("Element id is not INTERWORKING, : " + ie.id);
            }
            ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            int anOptions = data.get() & Constants.BYTE_MASK;
            this.ant = Ant.values()[anOptions & 15];
            if ((anOptions & 16) != 0) {
                z = true;
            }
            this.internet = z;
            if (ie.bytes.length != 1 && ie.bytes.length != 3 && ie.bytes.length != 7 && ie.bytes.length != 9) {
                throw new IllegalArgumentException("Bad Interworking element length: " + ie.bytes.length);
            } else if (ie.bytes.length == 7 || ie.bytes.length == 9) {
                this.hessid = ByteBufferReader.readInteger(data, ByteOrder.BIG_ENDIAN, 6);
            }
        }
    }

    public static class RoamingConsortium {
        public int anqpOICount = 0;
        public long[] roamingConsortiums = null;

        public void from(InformationElement ie) {
            if (ie.id != 111) {
                throw new IllegalArgumentException("Element id is not ROAMING_CONSORTIUM, : " + ie.id);
            }
            ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            this.anqpOICount = data.get() & Constants.BYTE_MASK;
            int oi12Length = data.get() & Constants.BYTE_MASK;
            int oi1Length = oi12Length & 15;
            int oi2Length = (oi12Length >>> 4) & 15;
            int oi3Length = ((ie.bytes.length - 2) - oi1Length) - oi2Length;
            int oiCount = 0;
            if (oi1Length > 0) {
                oiCount = 1;
                if (oi2Length > 0) {
                    oiCount = 1 + 1;
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
            }
        }
    }

    public static class SupportedRates {
        public static final int MASK = 127;
        public ArrayList<Integer> mRates = new ArrayList();
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
                case ReasonCode.STA_LEAVING /*36*/:
                    return 18000000;
                case StatusCode.UNSUPPORTED_RSN_IE_VERSION /*44*/:
                    return 22000000;
                case 48:
                    return 24000000;
                case ReasonCode.MESH_CHANNEL_SWITCH_UNSPECIFIED /*66*/:
                    return 33000000;
                case StatusCode.INVALID_RSNIE /*72*/:
                    return 36000000;
                case 96:
                    return 48000000;
                case 108:
                    return 54000000;
                default:
                    return -1;
            }
        }

        public void from(InformationElement ie) {
            this.mValid = false;
            if (ie != null && ie.bytes != null && ie.bytes.length <= 8 && ie.bytes.length >= 1) {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                int i = 0;
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
            Iterator rate$iterator = this.mRates.iterator();
            while (rate$iterator.hasNext()) {
                sbuf.append(String.format("%.1f", new Object[]{Double.valueOf(((double) ((Integer) rate$iterator.next()).intValue()) / 1000000.0d)})).append(", ");
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

        public void from(InformationElement ie) {
            this.mValid = false;
            if (ie != null && ie.bytes != null) {
                this.mLength = ie.bytes.length;
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                try {
                    this.mDtimCount = data.get() & Constants.BYTE_MASK;
                    this.mDtimPeriod = data.get() & Constants.BYTE_MASK;
                    this.mBitmapControl = data.get() & Constants.BYTE_MASK;
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

        public void from(InformationElement ie) {
            if (ie.id != WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE) {
                throw new IllegalArgumentException("Element id is not VHT_OPERATION, : " + ie.id);
            }
            this.channelMode = ie.bytes[0] & Constants.BYTE_MASK;
            this.centerFreqIndex1 = ie.bytes[1] & Constants.BYTE_MASK;
            this.centerFreqIndex2 = ie.bytes[2] & Constants.BYTE_MASK;
        }
    }

    public static class Vsa {
        private static final int ANQP_DOMID_BIT = 4;
        public int anqpDomainID = 0;
        public HSRelease hsRelease = null;

        public void from(InformationElement ie) {
            ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            if (ie.bytes.length >= 5 && data.getInt() == Constants.HS20_FRAME_PREFIX) {
                int hsConf = data.get() & Constants.BYTE_MASK;
                switch ((hsConf >> 4) & 15) {
                    case 0:
                        this.hsRelease = HSRelease.R1;
                        break;
                    case 1:
                        this.hsRelease = HSRelease.R2;
                        break;
                    default:
                        this.hsRelease = HSRelease.Unknown;
                        break;
                }
                if ((hsConf & 4) == 0) {
                    return;
                }
                if (ie.bytes.length < 7) {
                    throw new IllegalArgumentException("HS20 indication element too short: " + ie.bytes.length);
                }
                this.anqpDomainID = data.getShort() & Constants.SHORT_MASK;
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

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static InformationElement[] parseInformationElements(byte[] bytes) {
        if (bytes == null) {
            return new InformationElement[0];
        }
        ByteBuffer data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        ArrayList<InformationElement> infoElements = new ArrayList();
        boolean found_ssid = false;
        while (data.remaining() > 1) {
            int eid = data.get() & Constants.BYTE_MASK;
            int elementLength = data.get() & Constants.BYTE_MASK;
            if (elementLength > data.remaining() || (eid == 0 && found_ssid)) {
                break;
            }
            if (eid == 0) {
                found_ssid = true;
            }
            InformationElement ie = new InformationElement();
            ie.id = eid;
            ie.bytes = new byte[elementLength];
            data.get(ie.bytes);
            infoElements.add(ie);
        }
        return (InformationElement[]) infoElements.toArray(new InformationElement[infoElements.size()]);
    }

    public static RoamingConsortium getRoamingConsortiumIE(InformationElement[] ies) {
        RoamingConsortium roamingConsortium = new RoamingConsortium();
        if (ies != null) {
            for (InformationElement ie : ies) {
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

    public static Vsa getHS2VendorSpecificIE(InformationElement[] ies) {
        Vsa vsa = new Vsa();
        if (ies != null) {
            for (InformationElement ie : ies) {
                if (ie.id == AuthParam.PARAM_TYPE_VENDOR_SPECIFIC) {
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

    public static Interworking getInterworkingIE(InformationElement[] ies) {
        Interworking interworking = new Interworking();
        if (ies != null) {
            for (InformationElement ie : ies) {
                if (ie.id == StatusCode.AUTHORIZATION_DEENABLED) {
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
