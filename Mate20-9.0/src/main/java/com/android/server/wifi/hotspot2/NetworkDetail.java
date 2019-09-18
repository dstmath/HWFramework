package com.android.server.wifi.hotspot2;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.net.wifi.ScanResult;
import android.util.Log;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.RawByteElement;
import com.android.server.wifi.hotspot2.anqp.eap.AuthParam;
import com.android.server.wifi.util.InformationElementUtil;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NetworkDetail {
    private static final boolean DBG = false;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "NetworkDetail:";
    private final Map<Constants.ANQPElementType, ANQPElement> mANQPElements;
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
    private final InformationElementUtil.ExtendedCapabilities mExtendedCapabilities;
    private final long mHESSID;
    private final HSRelease mHSRelease;
    private final boolean mInternet;
    private final boolean mIsHiddenSsid;
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
    private final int mWifiMode;

    public enum Ant {
        Private,
        PrivateWithGuest,
        ChargeablePublic,
        FreePublic,
        Personal,
        EmergencyOnly,
        Resvd6,
        Resvd7,
        Resvd8,
        Resvd9,
        Resvd10,
        Resvd11,
        Resvd12,
        Resvd13,
        TestOrExperimental,
        Wildcard
    }

    public enum HSRelease {
        R1,
        R2,
        Unknown
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

    public NetworkDetail(String bssid, ScanResult.InformationElement[] infoElements, List<String> anqpLines, int freq) {
        this(bssid, infoElements, anqpLines, freq, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b4, code lost:
        r25 = r5;
        r26 = r6;
        r6 = r16;
        r4 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00d3, code lost:
        r5 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0100, code lost:
        r26 = r6;
        r6 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x012c, code lost:
        r7 = r7 + 1;
        r20 = r2;
        r17 = r4;
        r18 = r5;
        r16 = r6;
        r0 = r24;
        r5 = r25;
        r6 = r26;
        r2 = r40;
        r4 = r43;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x02fc  */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x019b  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01a1  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01f6  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0259  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x026c  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0283  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x028f  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x02a8  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x02b4  */
    public NetworkDetail(String bssid, ScanResult.InformationElement[] infoElements, List<String> list, int freq, String cap) {
        ArrayList<Integer> iesFound;
        boolean isHiddenSsid;
        String ssid;
        byte[] ssidOctets;
        InformationElementUtil.SupportedRates supportedRates;
        InformationElementUtil.APCapInfo apCapInfo;
        InformationElementUtil.SupportedRates extendedSupportedRates;
        byte[] ssidOctets2;
        boolean isHiddenSsid2;
        String ssid2;
        InformationElementUtil.SupportedRates supportedRates2;
        String ssid3;
        String ssid4;
        ArrayList<Integer> iesFound2;
        String ssid5;
        boolean isHiddenSsid3;
        InformationElementUtil.SupportedRates supportedRates3;
        ScanResult.InformationElement[] informationElementArr = infoElements;
        this.mDtimInterval = -1;
        this.mStream1 = 0;
        this.mStream2 = 0;
        this.mStream3 = 0;
        this.mStream4 = 0;
        this.mTxMcsSet = 0;
        if (informationElementArr != null) {
            this.mBSSID = Utils.parseMac(bssid);
            this.mCapabilities = cap;
            String ssid6 = null;
            boolean isHiddenSsid4 = false;
            InformationElementUtil.BssLoad bssLoad = new InformationElementUtil.BssLoad();
            InformationElementUtil.Interworking interworking = new InformationElementUtil.Interworking();
            InformationElementUtil.RoamingConsortium roamingConsortium = new InformationElementUtil.RoamingConsortium();
            InformationElementUtil.Vsa vsa = new InformationElementUtil.Vsa();
            InformationElementUtil.HtOperation htOperation = new InformationElementUtil.HtOperation();
            InformationElementUtil.VhtOperation vhtOperation = new InformationElementUtil.VhtOperation();
            InformationElementUtil.ExtendedCapabilities extendedCapabilities = new InformationElementUtil.ExtendedCapabilities();
            InformationElementUtil.TrafficIndicationMap trafficIndicationMap = new InformationElementUtil.TrafficIndicationMap();
            InformationElementUtil.SupportedRates supportedRates4 = new InformationElementUtil.SupportedRates();
            InformationElementUtil.SupportedRates extendedSupportedRates2 = new InformationElementUtil.SupportedRates();
            InformationElementUtil.APCapInfo apCapInfo2 = new InformationElementUtil.APCapInfo();
            RuntimeException exception = null;
            ArrayList<Integer> iesFound3 = new ArrayList<>();
            try {
                int length = informationElementArr.length;
                ssidOctets = null;
                int i = 0;
                while (i < length) {
                    try {
                        int i2 = length;
                        ScanResult.InformationElement ie = informationElementArr[i];
                        iesFound2 = iesFound3;
                        try {
                            iesFound2.add(Integer.valueOf(ie.id));
                            switch (ie.id) {
                                case 0:
                                    ssid = ssid6;
                                    isHiddenSsid = isHiddenSsid4;
                                    supportedRates = supportedRates4;
                                    extendedSupportedRates = extendedSupportedRates2;
                                    apCapInfo = apCapInfo2;
                                    ssidOctets = ie.bytes;
                                    break;
                                case 1:
                                    ssid = ssid6;
                                    extendedSupportedRates = extendedSupportedRates2;
                                    apCapInfo = apCapInfo2;
                                    isHiddenSsid = isHiddenSsid4;
                                    supportedRates = supportedRates4;
                                    try {
                                        supportedRates.from(ie);
                                        break;
                                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e) {
                                        e = e;
                                        String hs2LogTag = Utils.hs2LogTag(getClass());
                                        StringBuilder sb = new StringBuilder();
                                        iesFound = iesFound2;
                                        sb.append("Caught ");
                                        sb.append(e);
                                        Log.d(hs2LogTag, sb.toString());
                                        if (ssidOctets != null) {
                                        }
                                    }
                                    break;
                                case 5:
                                    ssid = ssid6;
                                    extendedSupportedRates = extendedSupportedRates2;
                                    apCapInfo = apCapInfo2;
                                    trafficIndicationMap.from(ie);
                                    break;
                                case 11:
                                    ssid = ssid6;
                                    extendedSupportedRates = extendedSupportedRates2;
                                    apCapInfo = apCapInfo2;
                                    bssLoad.from(ie);
                                    break;
                                case 45:
                                    extendedSupportedRates = extendedSupportedRates2;
                                    ssid = ssid6;
                                    apCapInfo = apCapInfo2;
                                    try {
                                        apCapInfo.from(ie);
                                        break;
                                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e2) {
                                        e = e2;
                                        isHiddenSsid = isHiddenSsid4;
                                        supportedRates = supportedRates4;
                                        String hs2LogTag2 = Utils.hs2LogTag(getClass());
                                        StringBuilder sb2 = new StringBuilder();
                                        iesFound = iesFound2;
                                        sb2.append("Caught ");
                                        sb2.append(e);
                                        Log.d(hs2LogTag2, sb2.toString());
                                        if (ssidOctets != null) {
                                        }
                                    }
                                    break;
                                case 50:
                                    extendedSupportedRates = extendedSupportedRates2;
                                    try {
                                        extendedSupportedRates.from(ie);
                                        ssid = ssid6;
                                        isHiddenSsid = isHiddenSsid4;
                                        supportedRates = supportedRates4;
                                        break;
                                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e3) {
                                        e = e3;
                                        ssid5 = ssid6;
                                        isHiddenSsid3 = isHiddenSsid4;
                                        supportedRates3 = supportedRates4;
                                        apCapInfo = apCapInfo2;
                                        String hs2LogTag22 = Utils.hs2LogTag(getClass());
                                        StringBuilder sb22 = new StringBuilder();
                                        iesFound = iesFound2;
                                        sb22.append("Caught ");
                                        sb22.append(e);
                                        Log.d(hs2LogTag22, sb22.toString());
                                        if (ssidOctets != null) {
                                        }
                                    }
                                    break;
                                case 61:
                                    htOperation.from(ie);
                                    break;
                                case ISupplicantStaIfaceCallback.StatusCode.AUTHORIZATION_DEENABLED:
                                    interworking.from(ie);
                                    break;
                                case 111:
                                    roamingConsortium.from(ie);
                                    break;
                                case 127:
                                    extendedCapabilities.from(ie);
                                    break;
                                case WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE:
                                    vhtOperation.from(ie);
                                    break;
                                case AuthParam.PARAM_TYPE_VENDOR_SPECIFIC /*221*/:
                                    try {
                                        vsa.from(ie);
                                        break;
                                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e4) {
                                        e = e4;
                                        ssid5 = ssid6;
                                        isHiddenSsid3 = isHiddenSsid4;
                                        supportedRates3 = supportedRates4;
                                        extendedSupportedRates = extendedSupportedRates2;
                                        apCapInfo = apCapInfo2;
                                        String hs2LogTag222 = Utils.hs2LogTag(getClass());
                                        StringBuilder sb222 = new StringBuilder();
                                        iesFound = iesFound2;
                                        sb222.append("Caught ");
                                        sb222.append(e);
                                        Log.d(hs2LogTag222, sb222.toString());
                                        if (ssidOctets != null) {
                                            exception = e;
                                            ssidOctets2 = ssidOctets;
                                            if (ssidOctets2 == null) {
                                            }
                                            this.mStream1 = apCapInfo.getStream1();
                                            this.mStream2 = apCapInfo.getStream2();
                                            this.mStream3 = apCapInfo.getStream3();
                                            this.mStream4 = apCapInfo.getStream4();
                                            this.mTxMcsSet = apCapInfo.getTxMcsSet();
                                            this.mSSID = ssid2;
                                            InformationElementUtil.APCapInfo aPCapInfo = apCapInfo;
                                            InformationElementUtil.SupportedRates supportedRates5 = supportedRates;
                                            this.mHESSID = interworking.hessid;
                                            this.mIsHiddenSsid = isHiddenSsid2;
                                            this.mStationCount = bssLoad.stationCount;
                                            this.mChannelUtilization = bssLoad.channelUtilization;
                                            this.mCapacity = bssLoad.capacity;
                                            this.mAnt = interworking.ant;
                                            this.mInternet = interworking.internet;
                                            this.mHSRelease = vsa.hsRelease;
                                            this.mAnqpDomainID = vsa.anqpDomainID;
                                            this.mAnqpOICount = roamingConsortium.anqpOICount;
                                            this.mRoamingConsortiums = roamingConsortium.getRoamingConsortiums();
                                            this.mExtendedCapabilities = extendedCapabilities;
                                            this.mANQPElements = null;
                                            this.mPrimaryFreq = freq;
                                            if (!vhtOperation.isValid()) {
                                            }
                                            if (trafficIndicationMap.isValid()) {
                                            }
                                            int maxRateB = 0;
                                            if (!extendedSupportedRates.isValid()) {
                                            }
                                            supportedRates2 = supportedRates5;
                                            if (!supportedRates2.isValid()) {
                                            }
                                        } else {
                                            InformationElementUtil.APCapInfo aPCapInfo2 = apCapInfo;
                                            InformationElementUtil.SupportedRates supportedRates6 = supportedRates;
                                            ArrayList<Integer> arrayList = iesFound;
                                            throw new IllegalArgumentException("Malformed IE string (no SSID)", e);
                                        }
                                    }
                                    break;
                                default:
                                    ssid = ssid6;
                                    isHiddenSsid = isHiddenSsid4;
                                    supportedRates = supportedRates4;
                                    extendedSupportedRates = extendedSupportedRates2;
                                    apCapInfo = apCapInfo2;
                                    break;
                            }
                        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e5) {
                            e = e5;
                            ssid = ssid6;
                            isHiddenSsid = isHiddenSsid4;
                            supportedRates = supportedRates4;
                            extendedSupportedRates = extendedSupportedRates2;
                            apCapInfo = apCapInfo2;
                            String hs2LogTag2222 = Utils.hs2LogTag(getClass());
                            StringBuilder sb2222 = new StringBuilder();
                            iesFound = iesFound2;
                            sb2222.append("Caught ");
                            sb2222.append(e);
                            Log.d(hs2LogTag2222, sb2222.toString());
                            if (ssidOctets != null) {
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e6) {
                        e = e6;
                        ssid = ssid6;
                        isHiddenSsid = isHiddenSsid4;
                        supportedRates = supportedRates4;
                        extendedSupportedRates = extendedSupportedRates2;
                        apCapInfo = apCapInfo2;
                        iesFound2 = iesFound3;
                        String hs2LogTag22222 = Utils.hs2LogTag(getClass());
                        StringBuilder sb22222 = new StringBuilder();
                        iesFound = iesFound2;
                        sb22222.append("Caught ");
                        sb22222.append(e);
                        Log.d(hs2LogTag22222, sb22222.toString());
                        if (ssidOctets != null) {
                        }
                    }
                }
                ssid = ssid6;
                isHiddenSsid = isHiddenSsid4;
                supportedRates = supportedRates4;
                extendedSupportedRates = extendedSupportedRates2;
                apCapInfo = apCapInfo2;
                iesFound = iesFound3;
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e7) {
                e = e7;
                ssid = null;
                isHiddenSsid = false;
                supportedRates = supportedRates4;
                extendedSupportedRates = extendedSupportedRates2;
                apCapInfo = apCapInfo2;
                iesFound2 = iesFound3;
                ssidOctets = null;
                String hs2LogTag222222 = Utils.hs2LogTag(getClass());
                StringBuilder sb222222 = new StringBuilder();
                iesFound = iesFound2;
                sb222222.append("Caught ");
                sb222222.append(e);
                Log.d(hs2LogTag222222, sb222222.toString());
                if (ssidOctets != null) {
                }
            }
            ssidOctets2 = ssidOctets;
            if (ssidOctets2 == null) {
                try {
                    ssid3 = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(ssidOctets2)).toString();
                } catch (CharacterCodingException e8) {
                    ssid3 = null;
                }
                if (ssid3 == null) {
                    if (!extendedCapabilities.isStrictUtf8()) {
                    } else if (exception == null) {
                        String str = ssid3;
                    } else {
                        String str2 = ssid3;
                        throw new IllegalArgumentException("Failed to decode SSID in dubious IE string");
                    }
                    ssid4 = new String(ssidOctets2, StandardCharsets.ISO_8859_1);
                } else {
                    ssid4 = ssid3;
                }
                int length2 = ssidOctets2.length;
                int i3 = 0;
                while (true) {
                    if (i3 >= length2) {
                        ssid2 = ssid4;
                        isHiddenSsid2 = true;
                    } else if (ssidOctets2[i3] != 0) {
                        isHiddenSsid2 = false;
                        ssid2 = ssid4;
                    } else {
                        i3++;
                    }
                }
            } else {
                ssid2 = ssid;
                isHiddenSsid2 = isHiddenSsid;
            }
            this.mStream1 = apCapInfo.getStream1();
            this.mStream2 = apCapInfo.getStream2();
            this.mStream3 = apCapInfo.getStream3();
            this.mStream4 = apCapInfo.getStream4();
            this.mTxMcsSet = apCapInfo.getTxMcsSet();
            this.mSSID = ssid2;
            InformationElementUtil.APCapInfo aPCapInfo3 = apCapInfo;
            InformationElementUtil.SupportedRates supportedRates52 = supportedRates;
            this.mHESSID = interworking.hessid;
            this.mIsHiddenSsid = isHiddenSsid2;
            this.mStationCount = bssLoad.stationCount;
            this.mChannelUtilization = bssLoad.channelUtilization;
            this.mCapacity = bssLoad.capacity;
            this.mAnt = interworking.ant;
            this.mInternet = interworking.internet;
            this.mHSRelease = vsa.hsRelease;
            this.mAnqpDomainID = vsa.anqpDomainID;
            this.mAnqpOICount = roamingConsortium.anqpOICount;
            this.mRoamingConsortiums = roamingConsortium.getRoamingConsortiums();
            this.mExtendedCapabilities = extendedCapabilities;
            this.mANQPElements = null;
            this.mPrimaryFreq = freq;
            if (!vhtOperation.isValid()) {
                this.mChannelWidth = vhtOperation.getChannelWidth();
                this.mCenterfreq0 = vhtOperation.getCenterFreq0();
                this.mCenterfreq1 = vhtOperation.getCenterFreq1();
            } else {
                this.mChannelWidth = htOperation.getChannelWidth();
                this.mCenterfreq0 = htOperation.getCenterFreq0(this.mPrimaryFreq);
                this.mCenterfreq1 = 0;
            }
            if (trafficIndicationMap.isValid()) {
                this.mDtimInterval = trafficIndicationMap.mDtimPeriod;
            }
            int maxRateB2 = 0;
            if (!extendedSupportedRates.isValid()) {
                String str3 = ssid2;
                byte[] bArr = ssidOctets2;
                maxRateB2 = extendedSupportedRates.mRates.get(extendedSupportedRates.mRates.size() - 1).intValue();
            } else {
                byte[] bArr2 = ssidOctets2;
            }
            supportedRates2 = supportedRates52;
            if (!supportedRates2.isValid()) {
                boolean z = isHiddenSsid2;
                int maxRateA = supportedRates2.mRates.get(supportedRates2.mRates.size() - 1).intValue();
                this.mMaxRate = maxRateA > maxRateB2 ? maxRateA : maxRateB2;
                InformationElementUtil.SupportedRates supportedRates7 = supportedRates2;
                InformationElementUtil.SupportedRates supportedRates8 = extendedSupportedRates;
                ArrayList<Integer> iesFound4 = iesFound;
                int i4 = maxRateA;
                this.mWifiMode = InformationElementUtil.WifiMode.determineMode(this.mPrimaryFreq, this.mMaxRate, vhtOperation.isValid(), iesFound4.contains(61), iesFound4.contains(42));
                return;
            }
            boolean z2 = isHiddenSsid2;
            InformationElementUtil.SupportedRates supportedRates9 = extendedSupportedRates;
            ArrayList<Integer> arrayList2 = iesFound;
            this.mWifiMode = 0;
            this.mMaxRate = 0;
            return;
        }
        throw new IllegalArgumentException("Null information elements");
    }

    private static ByteBuffer getAndAdvancePayload(ByteBuffer data, int plLength) {
        ByteBuffer payload = data.duplicate().order(data.order());
        payload.limit(payload.position() + plLength);
        data.position(data.position() + plLength);
        return payload;
    }

    private NetworkDetail(NetworkDetail base, Map<Constants.ANQPElementType, ANQPElement> anqpElements) {
        this.mDtimInterval = -1;
        this.mStream1 = 0;
        this.mStream2 = 0;
        this.mStream3 = 0;
        this.mStream4 = 0;
        this.mTxMcsSet = 0;
        this.mSSID = base.mSSID;
        this.mIsHiddenSsid = base.mIsHiddenSsid;
        this.mBSSID = base.mBSSID;
        this.mCapabilities = base.mCapabilities;
        this.mHESSID = base.mHESSID;
        this.mStationCount = base.mStationCount;
        this.mChannelUtilization = base.mChannelUtilization;
        this.mCapacity = base.mCapacity;
        this.mAnt = base.mAnt;
        this.mInternet = base.mInternet;
        this.mHSRelease = base.mHSRelease;
        this.mAnqpDomainID = base.mAnqpDomainID;
        this.mAnqpOICount = base.mAnqpOICount;
        this.mRoamingConsortiums = base.mRoamingConsortiums;
        this.mExtendedCapabilities = new InformationElementUtil.ExtendedCapabilities(base.mExtendedCapabilities);
        this.mANQPElements = anqpElements;
        this.mChannelWidth = base.mChannelWidth;
        this.mPrimaryFreq = base.mPrimaryFreq;
        this.mCenterfreq0 = base.mCenterfreq0;
        this.mCenterfreq1 = base.mCenterfreq1;
        this.mDtimInterval = base.mDtimInterval;
        this.mWifiMode = base.mWifiMode;
        this.mMaxRate = base.mMaxRate;
    }

    public NetworkDetail complete(Map<Constants.ANQPElementType, ANQPElement> anqpElements) {
        return new NetworkDetail(this, anqpElements);
    }

    public boolean queriable(List<Constants.ANQPElementType> queryElements) {
        return this.mAnt != null && (Constants.hasBaseANQPElements(queryElements) || (Constants.hasR2Elements(queryElements) && this.mHSRelease == HSRelease.R2));
    }

    public boolean has80211uInfo() {
        return (this.mAnt == null && this.mRoamingConsortiums == null && this.mHSRelease == null) ? false : true;
    }

    public boolean hasInterworking() {
        return this.mAnt != null;
    }

    public String getSSID() {
        return this.mSSID;
    }

    public String getCap() {
        return this.mCapabilities;
    }

    private static String getCapString(String cap) {
        String capStr;
        if (cap == null) {
            return "NULL";
        }
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
        if (this.mSSID != null) {
            for (int n = 0; n < this.mSSID.length(); n++) {
                if (this.mSSID.charAt(n) != 0) {
                    return this.mSSID;
                }
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
        return this.mAnt != null;
    }

    public Ant getAnt() {
        return this.mAnt;
    }

    public boolean isInternet() {
        return this.mInternet;
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
        ANQPElement osuProviders = this.mANQPElements.get(Constants.ANQPElementType.HSOSUProviders);
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

    public Map<Constants.ANQPElementType, ANQPElement> getANQPElements() {
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
        return this.mExtendedCapabilities.is80211McRTTResponder();
    }

    public boolean isSSID_UTF8() {
        return this.mExtendedCapabilities.isStrictUtf8();
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        NetworkDetail that = (NetworkDetail) thatObject;
        boolean ret = false;
        if (getSSID().equals(that.getSSID()) && getBSSID() == that.getBSSID()) {
            if (getCapString(getCap()).equals(getCapString(that.getCap()))) {
                ret = true;
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
        return String.format("NetworkInfo{SSID='%s', HESSID=%x, BSSID=%x, StationCount=%d, ChannelUtilization=%d, Capacity=%d, Ant=%s, Internet=%s, HSRelease=%s, AnqpDomainID=%d, AnqpOICount=%d, RoamingConsortiums=%s}", new Object[]{this.mSSID, Long.valueOf(this.mHESSID), Long.valueOf(this.mBSSID), Integer.valueOf(this.mStationCount), Integer.valueOf(this.mChannelUtilization), Integer.valueOf(this.mCapacity), this.mAnt, Boolean.valueOf(this.mInternet), this.mHSRelease, Integer.valueOf(this.mAnqpDomainID), Integer.valueOf(this.mAnqpOICount), Utils.roamingConsortiumsToString(this.mRoamingConsortiums)});
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

    public boolean isBeaconFrame() {
        return this.mDtimInterval > 0;
    }

    public boolean isHiddenBeaconFrame() {
        return isBeaconFrame() && this.mIsHiddenSsid;
    }

    public static String toMACString(long mac) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int n = 5; n >= 0; n--) {
            if (first) {
                first = false;
            } else {
                sb.append(':');
            }
            sb.append(String.format("%02x", new Object[]{Long.valueOf((mac >>> (n * 8)) & 255)}));
        }
        return sb.toString();
    }
}
