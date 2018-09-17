package com.android.server.wifi.util;

import android.net.wifi.ScanResult.InformationElement;
import android.util.Log;
import com.android.server.wifi.HwWifiCHRConst;
import com.android.server.wifi.anqp.CivicLocationElement;
import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.anqp.VenueNameElement;
import com.android.server.wifi.anqp.VenueNameElement.VenueGroup;
import com.android.server.wifi.anqp.VenueNameElement.VenueType;
import com.android.server.wifi.anqp.eap.EAP;
import com.android.server.wifi.hotspot2.NetworkDetail.Ant;
import com.android.server.wifi.hotspot2.NetworkDetail.HSRelease;
import com.google.protobuf.nano.Extension;
import java.net.ProtocolException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class InformationElementUtil {
    private static final boolean HWFLOW = false;
    private static final String TAG = "InformationElementUtil";

    public static class APCapInfo {
        private int mStream1;
        private int mStream2;
        private int mStream3;
        private int mStream4;
        private int mTxMcsSet;

        public APCapInfo() {
            this.mStream1 = 0;
            this.mStream2 = 0;
            this.mStream3 = 0;
            this.mStream4 = 0;
            this.mTxMcsSet = 0;
        }

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
        public int capacity;
        public int channelUtilization;
        public int stationCount;

        public BssLoad() {
            this.stationCount = 0;
            this.channelUtilization = 0;
            this.capacity = 0;
        }

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
        private static final short RSNE_VERSION = (short) 1;
        private static final int WPA2_AKM_EAP = 28053248;
        private static final int WPA2_AKM_EAP_SHA256 = 95162112;
        private static final int WPA2_AKM_FT_EAP = 61607680;
        private static final int WPA2_AKM_FT_PSK = 78384896;
        private static final int WPA2_AKM_PSK = 44830464;
        private static final int WPA2_AKM_PSK_SHA256 = 111939328;
        private static final int WPA_AKM_EAP = 32657408;
        private static final int WPA_AKM_PSK = 49434624;
        private static final int WPA_VENDOR_OUI_TYPE_ONE = 32657408;
        private static final short WPA_VENDOR_OUI_VERSION = (short) 1;

        private static String parseRsnElement(InformationElement ie) {
            ByteBuffer buf = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            try {
                if (buf.getShort() != WPA_VENDOR_OUI_VERSION) {
                    return null;
                }
                short i;
                buf.getInt();
                String security = "[WPA2";
                short cipherCount = buf.getShort();
                for (i = (short) 0; i < cipherCount; i++) {
                    buf.getInt();
                }
                short akmCount = buf.getShort();
                if (akmCount == (short) 0) {
                    security = security + "-EAP";
                }
                boolean found = InformationElementUtil.HWFLOW;
                for (i = (short) 0; i < akmCount; i++) {
                    switch (buf.getInt()) {
                        case WPA2_AKM_EAP /*28053248*/:
                            security = security + (found ? "+" : "-") + "EAP";
                            found = true;
                            break;
                        case WPA2_AKM_PSK /*44830464*/:
                            security = security + (found ? "+" : "-") + "PSK";
                            found = true;
                            break;
                        case WPA2_AKM_FT_EAP /*61607680*/:
                            security = security + (found ? "+" : "-") + "FT/EAP";
                            found = true;
                            break;
                        case WPA2_AKM_FT_PSK /*78384896*/:
                            security = security + (found ? "+" : "-") + "FT/PSK";
                            found = true;
                            break;
                        case WPA2_AKM_EAP_SHA256 /*95162112*/:
                            security = security + (found ? "+" : "-") + "EAP-SHA256";
                            found = true;
                            break;
                        case WPA2_AKM_PSK_SHA256 /*111939328*/:
                            security = security + (found ? "+" : "-") + "PSK-SHA256";
                            found = true;
                            break;
                        default:
                            break;
                    }
                }
                return security + "]";
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse RSNE, buffer underflow");
                return null;
            }
        }

        private static boolean isWpaOneElement(InformationElement ie) {
            boolean z = InformationElementUtil.HWFLOW;
            try {
                if (ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == WPA_VENDOR_OUI_TYPE_ONE) {
                    z = true;
                }
                return z;
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse VSA IE, buffer underflow");
                return InformationElementUtil.HWFLOW;
            }
        }

        private static String parseWpaOneElement(InformationElement ie) {
            ByteBuffer buf = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            try {
                buf.getInt();
                String security = "[WPA";
                if (buf.getShort() != WPA_VENDOR_OUI_VERSION) {
                    return null;
                }
                short i;
                buf.getInt();
                short cipherCount = buf.getShort();
                for (i = (short) 0; i < cipherCount; i++) {
                    buf.getInt();
                }
                short akmCount = buf.getShort();
                if (akmCount == (short) 0) {
                    security = security + "-EAP";
                }
                boolean found = InformationElementUtil.HWFLOW;
                for (i = (short) 0; i < akmCount; i++) {
                    switch (buf.getInt()) {
                        case WPA_VENDOR_OUI_TYPE_ONE /*32657408*/:
                            security = security + (found ? "+" : "-") + "EAP";
                            found = true;
                            break;
                        case WPA_AKM_PSK /*49434624*/:
                            security = security + (found ? "+" : "-") + "PSK";
                            found = true;
                            break;
                        default:
                            break;
                    }
                }
                return security + "]";
            } catch (BufferUnderflowException e) {
                Log.e("IE_Capabilities", "Couldn't parse type 1 WPA, buffer underflow");
                return null;
            }
        }

        public static String buildCapabilities(InformationElement[] ies, BitSet beaconCap) {
            int i = CAP_ESS_BIT_OFFSET;
            String capabilities = "";
            boolean rsneFound = InformationElementUtil.HWFLOW;
            boolean wpaFound = InformationElementUtil.HWFLOW;
            if (ies == null || beaconCap == null) {
                return capabilities;
            }
            boolean ess = beaconCap.get(CAP_ESS_BIT_OFFSET);
            boolean privacy = beaconCap.get(CAP_PRIVACY_BIT_OFFSET);
            int length = ies.length;
            while (i < length) {
                InformationElement ie = ies[i];
                if (ie.id == 48) {
                    rsneFound = true;
                    capabilities = capabilities + parseRsnElement(ie);
                }
                if (ie.id == EAP.VendorSpecific && isWpaOneElement(ie)) {
                    wpaFound = true;
                    capabilities = capabilities + parseWpaOneElement(ie);
                }
                i++;
            }
            if (!(rsneFound || wpaFound || !privacy)) {
                capabilities = capabilities + "[WEP]";
            }
            if (ess) {
                capabilities = capabilities + "[ESS]";
            }
            return capabilities;
        }
    }

    public static class ExtendedCapabilities {
        private static final int RTT_RESP_ENABLE_BIT = 70;
        private static final long SSID_UTF8_BIT = 281474976710656L;
        public Long extendedCapabilities;
        public boolean is80211McRTTResponder;

        public ExtendedCapabilities() {
            this.extendedCapabilities = null;
            this.is80211McRTTResponder = InformationElementUtil.HWFLOW;
        }

        public ExtendedCapabilities(ExtendedCapabilities other) {
            this.extendedCapabilities = null;
            this.is80211McRTTResponder = InformationElementUtil.HWFLOW;
            this.extendedCapabilities = other.extendedCapabilities;
            this.is80211McRTTResponder = other.is80211McRTTResponder;
        }

        public boolean isStrictUtf8() {
            return (this.extendedCapabilities == null || (this.extendedCapabilities.longValue() & SSID_UTF8_BIT) == 0) ? InformationElementUtil.HWFLOW : true;
        }

        public void from(InformationElement ie) {
            boolean z = InformationElementUtil.HWFLOW;
            this.extendedCapabilities = Long.valueOf(Constants.getInteger(ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN), ByteOrder.LITTLE_ENDIAN, ie.bytes.length));
            if (ie.bytes.length < 9) {
                this.is80211McRTTResponder = InformationElementUtil.HWFLOW;
                return;
            }
            if ((ie.bytes[8] & 64) != 0) {
                z = true;
            }
            this.is80211McRTTResponder = z;
        }
    }

    public static class HtOperation {
        public int secondChannelOffset;

        public HtOperation() {
            this.secondChannelOffset = 0;
        }

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
        public Ant ant;
        public long hessid;
        public boolean internet;
        public VenueGroup venueGroup;
        public VenueType venueType;

        public Interworking() {
            this.ant = null;
            this.internet = InformationElementUtil.HWFLOW;
            this.venueGroup = null;
            this.venueType = null;
            this.hessid = 0;
        }

        public void from(InformationElement ie) {
            boolean z = InformationElementUtil.HWFLOW;
            if (ie.id != 107) {
                throw new IllegalArgumentException("Element id is not INTERWORKING, : " + ie.id);
            }
            ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            int anOptions = data.get() & Constants.BYTE_MASK;
            this.ant = Ant.values()[anOptions & 15];
            if ((anOptions & 16) != 0) {
                z = true;
            }
            this.internet = z;
            if (ie.bytes.length == 3 || ie.bytes.length == 9) {
                try {
                    ByteBuffer vinfo = data.duplicate();
                    vinfo.limit(vinfo.position() + 2);
                    VenueNameElement vne = new VenueNameElement(ANQPElementType.ANQPVenueName, vinfo);
                    this.venueGroup = vne.getGroup();
                    this.venueType = vne.getType();
                } catch (ProtocolException e) {
                }
            } else if (!(ie.bytes.length == 1 || ie.bytes.length == 7)) {
                throw new IllegalArgumentException("Bad Interworking element length: " + ie.bytes.length);
            }
            if (ie.bytes.length == 7 || ie.bytes.length == 9) {
                this.hessid = Constants.getInteger(data, ByteOrder.BIG_ENDIAN, 6);
            }
        }
    }

    public static class RoamingConsortium {
        public int anqpOICount;
        public long[] roamingConsortiums;

        public RoamingConsortium() {
            this.anqpOICount = 0;
            this.roamingConsortiums = null;
        }

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
                this.roamingConsortiums[0] = Constants.getInteger(data, ByteOrder.BIG_ENDIAN, oi1Length);
            }
            if (oi2Length > 0 && this.roamingConsortiums.length > 1) {
                this.roamingConsortiums[1] = Constants.getInteger(data, ByteOrder.BIG_ENDIAN, oi2Length);
            }
            if (oi3Length > 0 && this.roamingConsortiums.length > 2) {
                this.roamingConsortiums[2] = Constants.getInteger(data, ByteOrder.BIG_ENDIAN, oi3Length);
            }
        }
    }

    public static class SupportedRates {
        public static final int MASK = 127;
        public ArrayList<Integer> mRates;
        public boolean mValid;

        public SupportedRates() {
            this.mValid = InformationElementUtil.HWFLOW;
            this.mRates = new ArrayList();
        }

        public boolean isValid() {
            return this.mValid;
        }

        public static int getRateFromByte(int byteVal) {
            switch (byteVal & MASK) {
                case Extension.TYPE_FLOAT /*2*/:
                    return 1000000;
                case Extension.TYPE_UINT64 /*4*/:
                    return 2000000;
                case Extension.TYPE_MESSAGE /*11*/:
                    return 5500000;
                case Extension.TYPE_BYTES /*12*/:
                    return 6000000;
                case Extension.TYPE_SINT64 /*18*/:
                    return 9000000;
                case CivicLocationElement.ADDITIONAL_LOCATION /*22*/:
                    return 11000000;
                case EAP.EAP_3Com /*24*/:
                    return 12000000;
                case CivicLocationElement.BRANCH_ROAD /*36*/:
                    return 18000000;
                case EAP.EAP_ZLXEAP /*44*/:
                    return 22000000;
                case EAP.EAP_SAKE /*48*/:
                    return 24000000;
                case 66:
                    return 33000000;
                case 72:
                    return 36000000;
                case HwWifiCHRConst.WIFI_SCAN_FAILED_EX /*96*/:
                    return 48000000;
                case 108:
                    return 54000000;
                default:
                    return -1;
            }
        }

        public void from(InformationElement ie) {
            this.mValid = InformationElementUtil.HWFLOW;
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
        public int mBitmapControl;
        public int mDtimCount;
        public int mDtimPeriod;
        public int mLength;
        private boolean mValid;

        public TrafficIndicationMap() {
            this.mValid = InformationElementUtil.HWFLOW;
            this.mLength = 0;
            this.mDtimCount = -1;
            this.mDtimPeriod = -1;
            this.mBitmapControl = 0;
        }

        public boolean isValid() {
            return this.mValid;
        }

        public void from(InformationElement ie) {
            this.mValid = InformationElementUtil.HWFLOW;
            if (ie != null && ie.bytes != null) {
                this.mLength = ie.bytes.length;
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                try {
                    this.mDtimCount = data.get() & Constants.BYTE_MASK;
                    this.mDtimPeriod = data.get() & Constants.BYTE_MASK;
                    this.mBitmapControl = data.get() & Constants.BYTE_MASK;
                    data.get();
                    if (this.mLength <= MAX_TIM_LENGTH) {
                        this.mValid = true;
                    }
                } catch (BufferUnderflowException e) {
                }
            }
        }
    }

    public static class VhtOperation {
        public int centerFreqIndex1;
        public int centerFreqIndex2;
        public int channelMode;

        public VhtOperation() {
            this.channelMode = 0;
            this.centerFreqIndex1 = 0;
            this.centerFreqIndex2 = 0;
        }

        public boolean isValid() {
            return this.channelMode != 0 ? true : InformationElementUtil.HWFLOW;
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
            if (ie.id != 192) {
                throw new IllegalArgumentException("Element id is not VHT_OPERATION, : " + ie.id);
            }
            this.channelMode = ie.bytes[0] & Constants.BYTE_MASK;
            this.centerFreqIndex1 = ie.bytes[1] & Constants.BYTE_MASK;
            this.centerFreqIndex2 = ie.bytes[2] & Constants.BYTE_MASK;
        }
    }

    public static class Vsa {
        private static final int ANQP_DOMID_BIT = 4;
        public int anqpDomainID;
        public HSRelease hsRelease;

        public Vsa() {
            this.hsRelease = null;
            this.anqpDomainID = 0;
        }

        public void from(InformationElement ie) {
            ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
            if (ie.bytes.length >= 5 && data.getInt() == Constants.HS20_FRAME_PREFIX) {
                int hsConf = data.get() & Constants.BYTE_MASK;
                switch ((hsConf >> ANQP_DOMID_BIT) & 15) {
                    case ApConfigUtil.SUCCESS /*0*/:
                        this.hsRelease = HSRelease.R1;
                        break;
                    case Extension.TYPE_DOUBLE /*1*/:
                        this.hsRelease = HSRelease.R2;
                        break;
                    default:
                        this.hsRelease = HSRelease.Unknown;
                        break;
                }
                if ((hsConf & ANQP_DOMID_BIT) == 0) {
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
                return MODE_11AC;
            }
            if (foundHt) {
                return MODE_11N;
            }
            if (foundErp) {
                return MODE_11G;
            }
            if (frequency >= 3000) {
                return MODE_11A;
            }
            if (maxRate < 24000000) {
                return MODE_11B;
            }
            return MODE_11G;
        }

        public static String toString(int mode) {
            switch (mode) {
                case MODE_11A /*1*/:
                    return "MODE_11A";
                case MODE_11B /*2*/:
                    return "MODE_11B";
                case MODE_11G /*3*/:
                    return "MODE_11G";
                case MODE_11N /*4*/:
                    return "MODE_11N";
                case MODE_11AC /*5*/:
                    return "MODE_11AC";
                default:
                    return "MODE_UNDEFINED";
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.util.InformationElementUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.util.InformationElementUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.util.InformationElementUtil.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static InformationElement[] parseInformationElements(byte[] bytes) {
        if (bytes == null) {
            return new InformationElement[0];
        }
        ByteBuffer data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        ArrayList<InformationElement> infoElements = new ArrayList();
        boolean found_ssid = HWFLOW;
        while (data.remaining() > 1) {
            int eid = data.get() & Constants.BYTE_MASK;
            int elementLength = data.get() & Constants.BYTE_MASK;
            if (elementLength <= data.remaining() && !(eid == 0 && found_ssid)) {
                if (eid == 0) {
                    found_ssid = true;
                }
                InformationElement ie = new InformationElement();
                ie.id = eid;
                ie.bytes = new byte[elementLength];
                data.get(ie.bytes);
                infoElements.add(ie);
            }
        }
        return (InformationElement[]) infoElements.toArray(new InformationElement[infoElements.size()]);
    }
}
