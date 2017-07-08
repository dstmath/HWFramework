package com.android.server.wifi.hotspot2;

import android.net.wifi.ScanResult.InformationElement;
import android.util.Log;
import com.android.server.wifi.anqp.ANQPElement;
import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.anqp.RawByteElement;
import com.android.server.wifi.anqp.VenueNameElement.VenueGroup;
import com.android.server.wifi.anqp.VenueNameElement.VenueType;
import com.android.server.wifi.anqp.eap.EAP;
import com.android.server.wifi.util.ApConfigUtil;
import com.android.server.wifi.util.InformationElementUtil.APCapInfo;
import com.android.server.wifi.util.InformationElementUtil.BssLoad;
import com.android.server.wifi.util.InformationElementUtil.ExtendedCapabilities;
import com.android.server.wifi.util.InformationElementUtil.HtOperation;
import com.android.server.wifi.util.InformationElementUtil.Interworking;
import com.android.server.wifi.util.InformationElementUtil.RoamingConsortium;
import com.android.server.wifi.util.InformationElementUtil.SupportedRates;
import com.android.server.wifi.util.InformationElementUtil.TrafficIndicationMap;
import com.android.server.wifi.util.InformationElementUtil.VhtOperation;
import com.android.server.wifi.util.InformationElementUtil.Vsa;
import com.android.server.wifi.util.InformationElementUtil.WifiMode;
import com.google.protobuf.nano.Extension;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NetworkDetail {
    private static final boolean DBG = true;
    private static final boolean HWFLOW = false;
    private static final String TAG = "NetworkDetail:";
    private static final boolean VDBG = false;
    private final Map<ANQPElementType, ANQPElement> mANQPElements;
    private final int mAnqpDomainID;
    private final int mAnqpOICount;
    private final Ant mAnt;
    private final long mBSSID;
    private final String mCapabilities;
    private final int mCapacity;
    private final int mCenterfreq0;
    private final int mCenterfreq1;
    private final int mChannelUtilization;
    private final int mChannelWidth;
    private int mDtimInterval;
    private final ExtendedCapabilities mExtendedCapabilities;
    private final long mHESSID;
    private final HSRelease mHSRelease;
    private final boolean mInternet;
    private final int mMaxRate;
    private final int mPrimaryFreq;
    private final long[] mRoamingConsortiums;
    private final String mSSID;
    private final int mStationCount;
    private int mStream1;
    private int mStream2;
    private int mStream3;
    private int mStream4;
    private int mTxMcsSet;
    private final VenueGroup mVenueGroup;
    private final VenueType mVenueType;
    private final int mWifiMode;

    public enum Ant {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.NetworkDetail.Ant.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.NetworkDetail.Ant.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.NetworkDetail.Ant.<clinit>():void");
        }
    }

    public enum HSRelease {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.NetworkDetail.HSRelease.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.NetworkDetail.HSRelease.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.NetworkDetail.HSRelease.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.NetworkDetail.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.NetworkDetail.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.NetworkDetail.<clinit>():void");
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

    public int getPrimaryFreq() {
        return this.mPrimaryFreq;
    }

    public NetworkDetail(String bssid, InformationElement[] infoElements, List<String> anqpLines, int freq) {
        this(bssid, infoElements, anqpLines, freq, null);
    }

    public NetworkDetail(String bssid, InformationElement[] infoElements, List<String> anqpLines, int freq, String cap) {
        this.mDtimInterval = -1;
        this.mStream1 = 0;
        this.mStream2 = 0;
        this.mStream3 = 0;
        this.mStream4 = 0;
        this.mTxMcsSet = 0;
        if (infoElements == null) {
            throw new IllegalArgumentException("Null information elements");
        }
        this.mBSSID = Utils.parseMac(bssid);
        this.mCapabilities = cap;
        String str = null;
        byte[] bArr = null;
        BssLoad bssLoad = new BssLoad();
        Interworking interworking = new Interworking();
        RoamingConsortium roamingConsortium = new RoamingConsortium();
        Vsa vsa = new Vsa();
        HtOperation htOperation = new HtOperation();
        VhtOperation vhtOperation = new VhtOperation();
        ExtendedCapabilities extendedCapabilities = new ExtendedCapabilities();
        TrafficIndicationMap trafficIndicationMap = new TrafficIndicationMap();
        SupportedRates supportedRates = new SupportedRates();
        SupportedRates extendedSupportedRates = new SupportedRates();
        APCapInfo apCapInfo = new APCapInfo();
        RuntimeException exception = null;
        ArrayList<Integer> iesFound = new ArrayList();
        try {
            for (InformationElement ie : infoElements) {
                iesFound.add(Integer.valueOf(ie.id));
                switch (ie.id) {
                    case ApConfigUtil.SUCCESS /*0*/:
                        bArr = ie.bytes;
                        break;
                    case Extension.TYPE_DOUBLE /*1*/:
                        supportedRates.from(ie);
                        break;
                    case Extension.TYPE_INT32 /*5*/:
                        trafficIndicationMap.from(ie);
                        break;
                    case Extension.TYPE_MESSAGE /*11*/:
                        bssLoad.from(ie);
                        break;
                    case EAP.EAP_Link /*45*/:
                        apCapInfo.from(ie);
                        break;
                    case EAP.EAP_AKAPrim /*50*/:
                        extendedSupportedRates.from(ie);
                        break;
                    case 61:
                        htOperation.from(ie);
                        break;
                    case 107:
                        interworking.from(ie);
                        break;
                    case 111:
                        roamingConsortium.from(ie);
                        break;
                    case SupportedRates.MASK /*127*/:
                        extendedCapabilities.from(ie);
                        break;
                    case 192:
                        vhtOperation.from(ie);
                        break;
                    case EAP.VendorSpecific /*221*/:
                        vsa.from(ie);
                        break;
                    default:
                        break;
                }
            }
        } catch (RuntimeException e) {
            Log.d(Utils.hs2LogTag(getClass()), "Caught " + e);
            if (bArr == null) {
                throw new IllegalArgumentException("Malformed IE string (no SSID)", e);
            }
            exception = e;
        }
        if (bArr != null) {
            try {
                str = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bArr)).toString();
            } catch (CharacterCodingException e2) {
                str = null;
            }
            if (str == null) {
                if (!extendedCapabilities.isStrictUtf8() || r10 == null) {
                    String str2 = new String(bArr, StandardCharsets.ISO_8859_1);
                } else {
                    throw new IllegalArgumentException("Failed to decode SSID in dubious IE string");
                }
            }
        }
        this.mStream1 = apCapInfo.getStream1();
        this.mStream2 = apCapInfo.getStream2();
        this.mStream3 = apCapInfo.getStream3();
        this.mStream4 = apCapInfo.getStream4();
        this.mTxMcsSet = apCapInfo.getTxMcsSet();
        this.mSSID = str;
        this.mHESSID = interworking.hessid;
        this.mStationCount = bssLoad.stationCount;
        this.mChannelUtilization = bssLoad.channelUtilization;
        this.mCapacity = bssLoad.capacity;
        this.mAnt = interworking.ant;
        this.mInternet = interworking.internet;
        this.mVenueGroup = interworking.venueGroup;
        this.mVenueType = interworking.venueType;
        this.mHSRelease = vsa.hsRelease;
        this.mAnqpDomainID = vsa.anqpDomainID;
        this.mAnqpOICount = roamingConsortium.anqpOICount;
        this.mRoamingConsortiums = roamingConsortium.roamingConsortiums;
        this.mExtendedCapabilities = extendedCapabilities;
        this.mANQPElements = SupplicantBridge.parseANQPLines(anqpLines);
        this.mPrimaryFreq = freq;
        if (vhtOperation.isValid()) {
            this.mChannelWidth = vhtOperation.getChannelWidth();
            this.mCenterfreq0 = vhtOperation.getCenterFreq0();
            this.mCenterfreq1 = vhtOperation.getCenterFreq1();
        } else {
            this.mChannelWidth = htOperation.getChannelWidth();
            this.mCenterfreq0 = htOperation.getCenterFreq0(this.mPrimaryFreq);
            this.mCenterfreq1 = 0;
        }
        this.mDtimInterval = trafficIndicationMap.mDtimPeriod;
        int maxRateB = 0;
        if (extendedSupportedRates.isValid()) {
            maxRateB = ((Integer) extendedSupportedRates.mRates.get(extendedSupportedRates.mRates.size() - 1)).intValue();
        }
        if (supportedRates.isValid()) {
            int maxRateA = ((Integer) supportedRates.mRates.get(supportedRates.mRates.size() - 1)).intValue();
            if (maxRateA > maxRateB) {
                maxRateB = maxRateA;
            }
            this.mMaxRate = maxRateB;
            this.mWifiMode = WifiMode.determineMode(this.mPrimaryFreq, this.mMaxRate, vhtOperation.isValid(), iesFound.contains(Integer.valueOf(61)), iesFound.contains(Integer.valueOf(42)));
            return;
        }
        this.mWifiMode = 0;
        this.mMaxRate = 0;
        Log.w("WifiMode", this.mSSID + ", Invalid SupportedRates!!!");
    }

    private static ByteBuffer getAndAdvancePayload(ByteBuffer data, int plLength) {
        ByteBuffer payload = data.duplicate().order(data.order());
        payload.limit(payload.position() + plLength);
        data.position(data.position() + plLength);
        return payload;
    }

    private NetworkDetail(NetworkDetail base, Map<ANQPElementType, ANQPElement> anqpElements) {
        this.mDtimInterval = -1;
        this.mStream1 = 0;
        this.mStream2 = 0;
        this.mStream3 = 0;
        this.mStream4 = 0;
        this.mTxMcsSet = 0;
        this.mSSID = base.mSSID;
        this.mBSSID = base.mBSSID;
        this.mCapabilities = base.mCapabilities;
        this.mHESSID = base.mHESSID;
        this.mStationCount = base.mStationCount;
        this.mChannelUtilization = base.mChannelUtilization;
        this.mCapacity = base.mCapacity;
        this.mAnt = base.mAnt;
        this.mInternet = base.mInternet;
        this.mVenueGroup = base.mVenueGroup;
        this.mVenueType = base.mVenueType;
        this.mHSRelease = base.mHSRelease;
        this.mAnqpDomainID = base.mAnqpDomainID;
        this.mAnqpOICount = base.mAnqpOICount;
        this.mRoamingConsortiums = base.mRoamingConsortiums;
        this.mExtendedCapabilities = new ExtendedCapabilities(base.mExtendedCapabilities);
        this.mANQPElements = anqpElements;
        this.mChannelWidth = base.mChannelWidth;
        this.mPrimaryFreq = base.mPrimaryFreq;
        this.mCenterfreq0 = base.mCenterfreq0;
        this.mCenterfreq1 = base.mCenterfreq1;
        this.mDtimInterval = base.mDtimInterval;
        this.mWifiMode = base.mWifiMode;
        this.mMaxRate = base.mMaxRate;
    }

    public NetworkDetail complete(Map<ANQPElementType, ANQPElement> anqpElements) {
        return new NetworkDetail(this, anqpElements);
    }

    public boolean queriable(List<ANQPElementType> queryElements) {
        if (this.mAnt == null) {
            return HWFLOW;
        }
        if (Constants.hasBaseANQPElements(queryElements)) {
            return DBG;
        }
        return (Constants.hasR2Elements(queryElements) && this.mHSRelease == HSRelease.R2) ? DBG : HWFLOW;
    }

    public boolean has80211uInfo() {
        return (this.mAnt == null && this.mRoamingConsortiums == null && this.mHSRelease == null) ? HWFLOW : DBG;
    }

    public boolean hasInterworking() {
        return this.mAnt != null ? DBG : HWFLOW;
    }

    public String getSSID() {
        return this.mSSID;
    }

    public String getCap() {
        return this.mCapabilities;
    }

    private static String getCapString(String cap) {
        if (cap == null) {
            return "NULL";
        }
        String capStr;
        if (cap.contains("WEP")) {
            capStr = "WEP";
        } else if (cap.contains("PSK")) {
            capStr = "PSK";
        } else if (cap.contains("EAP")) {
            capStr = "EAP";
        } else {
            capStr = "NONE";
        }
        return capStr;
    }

    public String getTrimmedSSID() {
        for (int n = 0; n < this.mSSID.length(); n++) {
            if (this.mSSID.charAt(n) != '\u0000') {
                return this.mSSID;
            }
        }
        return "";
    }

    public long getHESSID() {
        return this.mHESSID;
    }

    public long getBSSID() {
        return this.mBSSID;
    }

    public int getStationCount() {
        return this.mStationCount;
    }

    public int getChannelUtilization() {
        return this.mChannelUtilization;
    }

    public int getCapacity() {
        return this.mCapacity;
    }

    public boolean isInterworking() {
        return this.mAnt != null ? DBG : HWFLOW;
    }

    public Ant getAnt() {
        return this.mAnt;
    }

    public boolean isInternet() {
        return this.mInternet;
    }

    public VenueGroup getVenueGroup() {
        return this.mVenueGroup;
    }

    public VenueType getVenueType() {
        return this.mVenueType;
    }

    public HSRelease getHSRelease() {
        return this.mHSRelease;
    }

    public int getAnqpDomainID() {
        return this.mAnqpDomainID;
    }

    public byte[] getOsuProviders() {
        byte[] bArr = null;
        if (this.mANQPElements == null) {
            return null;
        }
        ANQPElement osuProviders = (ANQPElement) this.mANQPElements.get(ANQPElementType.HSOSUProviders);
        if (osuProviders != null) {
            bArr = ((RawByteElement) osuProviders).getPayload();
        }
        return bArr;
    }

    public int getAnqpOICount() {
        return this.mAnqpOICount;
    }

    public long[] getRoamingConsortiums() {
        return this.mRoamingConsortiums;
    }

    public Long getExtendedCapabilities() {
        return this.mExtendedCapabilities.extendedCapabilities;
    }

    public Map<ANQPElementType, ANQPElement> getANQPElements() {
        return this.mANQPElements;
    }

    public int getChannelWidth() {
        return this.mChannelWidth;
    }

    public int getCenterfreq0() {
        return this.mCenterfreq0;
    }

    public int getCenterfreq1() {
        return this.mCenterfreq1;
    }

    public int getWifiMode() {
        return this.mWifiMode;
    }

    public int getDtimInterval() {
        return this.mDtimInterval;
    }

    public boolean is80211McResponderSupport() {
        return this.mExtendedCapabilities.is80211McRTTResponder;
    }

    public boolean isSSID_UTF8() {
        return this.mExtendedCapabilities.isStrictUtf8();
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return DBG;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return HWFLOW;
        }
        NetworkDetail that = (NetworkDetail) thatObject;
        boolean ret = HWFLOW;
        if (getSSID().equals(that.getSSID()) && getBSSID() == that.getBSSID()) {
            if (getCapString(getCap()).equals(getCapString(that.getCap()))) {
                ret = DBG;
            } else {
                Log.d(TAG, "CapChanged: " + getCap() + ", that Cap: " + that.getCap());
            }
        }
        return ret;
    }

    public int hashCode() {
        return ((((this.mSSID + getCapString(this.mCapabilities)).hashCode() * 31) + ((int) (this.mBSSID >>> 32))) * 31) + ((int) this.mBSSID);
    }

    public String toString() {
        return String.format("NetworkInfo{SSID='%s', HESSID=%x, BSSID=%x, StationCount=%d, ChannelUtilization=%d, Capacity=%d, Ant=%s, Internet=%s, VenueGroup=%s, VenueType=%s, HSRelease=%s, AnqpDomainID=%d, AnqpOICount=%d, RoamingConsortiums=%s}", new Object[]{this.mSSID, Long.valueOf(this.mHESSID), Long.valueOf(this.mBSSID), Integer.valueOf(this.mStationCount), Integer.valueOf(this.mChannelUtilization), Integer.valueOf(this.mCapacity), this.mAnt, Boolean.valueOf(this.mInternet), this.mVenueGroup, this.mVenueType, this.mHSRelease, Integer.valueOf(this.mAnqpDomainID), Integer.valueOf(this.mAnqpOICount), Utils.roamingConsortiumsToString(this.mRoamingConsortiums)});
    }

    public String toKeyString() {
        if (this.mHESSID != 0) {
            return String.format("'%s':%012x (%012x)", new Object[]{this.mSSID, Long.valueOf(this.mBSSID), Long.valueOf(this.mHESSID)});
        }
        return String.format("'%s':%012x", new Object[]{this.mSSID, Long.valueOf(this.mBSSID)});
    }

    public String getBSSIDString() {
        return toMACString(this.mBSSID);
    }

    public static String toMACString(long mac) {
        StringBuilder sb = new StringBuilder();
        boolean first = DBG;
        for (int n = 5; n >= 0; n--) {
            if (first) {
                first = HWFLOW;
            } else {
                sb.append(':');
            }
            sb.append(String.format("%02x", new Object[]{Long.valueOf((mac >>> (n * 8)) & 255)}));
        }
        return sb.toString();
    }
}
