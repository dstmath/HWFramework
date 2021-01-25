package com.android.server.wifi.hotspot2;

import android.net.wifi.ScanResult;
import android.util.Log;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.RawByteElement;
import com.android.server.wifi.hwUtil.HwInformationElementUtilEx;
import com.android.server.wifi.hwUtil.StringUtilEx;
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

    /* JADX INFO: Multiple debug info for r6v13 'supportedRates'  com.android.server.wifi.util.InformationElementUtil$SupportedRates: [D('isHiddenSsid' boolean), D('supportedRates' com.android.server.wifi.util.InformationElementUtil$SupportedRates)] */
    /* JADX INFO: Multiple debug info for r5v16 'apCapInfo'  com.android.server.wifi.hwUtil.HwInformationElementUtilEx$APCapInfo: [D('ssid' java.lang.String), D('apCapInfo' com.android.server.wifi.hwUtil.HwInformationElementUtilEx$APCapInfo)] */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x025d  */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x02be  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x02d1  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x02e8  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x02f5  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0310  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x031a  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0368  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x037a  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0205  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x020b  */
    public NetworkDetail(String bssid, ScanResult.InformationElement[] infoElements, List<String> list, int freq, String cap) {
        boolean isHiddenSsid;
        String ssid;
        ArrayList<Integer> iesFound;
        InformationElementUtil.SupportedRates supportedRates;
        HwInformationElementUtilEx.APCapInfo apCapInfo;
        InformationElementUtil.SupportedRates extendedSupportedRates;
        byte[] ssidOctets;
        boolean isHiddenSsid2;
        String ssid2;
        int maxRateB;
        String ssid3;
        byte[] ssidOctets2;
        ArrayList<Integer> iesFound2;
        RuntimeException e;
        ScanResult.InformationElement ie;
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
            String ssid4 = null;
            boolean isHiddenSsid3 = false;
            InformationElementUtil.BssLoad bssLoad = new InformationElementUtil.BssLoad();
            InformationElementUtil.Interworking interworking = new InformationElementUtil.Interworking();
            InformationElementUtil.RoamingConsortium roamingConsortium = new InformationElementUtil.RoamingConsortium();
            InformationElementUtil.Vsa vsa = new InformationElementUtil.Vsa();
            InformationElementUtil.HtOperation htOperation = new InformationElementUtil.HtOperation();
            InformationElementUtil.VhtOperation vhtOperation = new InformationElementUtil.VhtOperation();
            InformationElementUtil.ExtendedCapabilities extendedCapabilities = new InformationElementUtil.ExtendedCapabilities();
            InformationElementUtil.TrafficIndicationMap trafficIndicationMap = new InformationElementUtil.TrafficIndicationMap();
            InformationElementUtil.SupportedRates supportedRates2 = new InformationElementUtil.SupportedRates();
            InformationElementUtil.SupportedRates extendedSupportedRates2 = new InformationElementUtil.SupportedRates();
            HwInformationElementUtilEx.APCapInfo apCapInfo2 = new HwInformationElementUtilEx.APCapInfo();
            RuntimeException exception = null;
            ArrayList<Integer> iesFound3 = new ArrayList<>();
            try {
                int length = informationElementArr.length;
                ssidOctets2 = null;
                int i = 0;
                while (i < length) {
                    try {
                        ie = informationElementArr[i];
                        iesFound2 = iesFound3;
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e2) {
                        e = e2;
                        extendedSupportedRates = extendedSupportedRates2;
                        iesFound2 = iesFound3;
                        ssid = ssid4;
                        apCapInfo = apCapInfo2;
                        isHiddenSsid = isHiddenSsid3;
                        supportedRates = supportedRates2;
                        String hs2LogTag = Utils.hs2LogTag(getClass());
                        StringBuilder sb = new StringBuilder();
                        iesFound = iesFound2;
                        sb.append("Caught ");
                        sb.append(e);
                        Log.d(hs2LogTag, sb.toString());
                        if (ssidOctets2 != null) {
                        }
                    }
                    try {
                        iesFound2.add(Integer.valueOf(ie.id));
                        int i2 = ie.id;
                        if (i2 == 0) {
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                            ssidOctets2 = ie.bytes;
                        } else if (i2 == 1) {
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                            try {
                                supportedRates.from(ie);
                            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e3) {
                                e = e3;
                                String hs2LogTag2 = Utils.hs2LogTag(getClass());
                                StringBuilder sb2 = new StringBuilder();
                                iesFound = iesFound2;
                                sb2.append("Caught ");
                                sb2.append(e);
                                Log.d(hs2LogTag2, sb2.toString());
                                if (ssidOctets2 != null) {
                                }
                            }
                        } else if (i2 == 5) {
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            trafficIndicationMap.from(ie);
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                        } else if (i2 == 11) {
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            bssLoad.from(ie);
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                        } else if (i2 == 45) {
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            try {
                                apCapInfo.from(ie);
                                isHiddenSsid = isHiddenSsid3;
                                supportedRates = supportedRates2;
                            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e4) {
                                e = e4;
                                isHiddenSsid = isHiddenSsid3;
                                supportedRates = supportedRates2;
                                String hs2LogTag22 = Utils.hs2LogTag(getClass());
                                StringBuilder sb22 = new StringBuilder();
                                iesFound = iesFound2;
                                sb22.append("Caught ");
                                sb22.append(e);
                                Log.d(hs2LogTag22, sb22.toString());
                                if (ssidOctets2 != null) {
                                }
                            }
                        } else if (i2 == 50) {
                            extendedSupportedRates = extendedSupportedRates2;
                            try {
                                extendedSupportedRates.from(ie);
                                ssid = ssid4;
                                apCapInfo = apCapInfo2;
                                isHiddenSsid = isHiddenSsid3;
                                supportedRates = supportedRates2;
                            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e5) {
                                e = e5;
                                ssid = ssid4;
                                apCapInfo = apCapInfo2;
                                isHiddenSsid = isHiddenSsid3;
                                supportedRates = supportedRates2;
                                String hs2LogTag222 = Utils.hs2LogTag(getClass());
                                StringBuilder sb222 = new StringBuilder();
                                iesFound = iesFound2;
                                sb222.append("Caught ");
                                sb222.append(e);
                                Log.d(hs2LogTag222, sb222.toString());
                                if (ssidOctets2 != null) {
                                }
                            }
                        } else if (i2 == 61) {
                            htOperation.from(ie);
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                        } else if (i2 == 107) {
                            interworking.from(ie);
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                        } else if (i2 == 111) {
                            roamingConsortium.from(ie);
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                        } else if (i2 == 127) {
                            extendedCapabilities.from(ie);
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                        } else if (i2 == 192) {
                            vhtOperation.from(ie);
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                        } else if (i2 != 221) {
                            extendedSupportedRates = extendedSupportedRates2;
                            ssid = ssid4;
                            apCapInfo = apCapInfo2;
                            isHiddenSsid = isHiddenSsid3;
                            supportedRates = supportedRates2;
                        } else {
                            try {
                                vsa.from(ie);
                                extendedSupportedRates = extendedSupportedRates2;
                                ssid = ssid4;
                                apCapInfo = apCapInfo2;
                                isHiddenSsid = isHiddenSsid3;
                                supportedRates = supportedRates2;
                            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e6) {
                                e = e6;
                                extendedSupportedRates = extendedSupportedRates2;
                                ssid = ssid4;
                                apCapInfo = apCapInfo2;
                                isHiddenSsid = isHiddenSsid3;
                                supportedRates = supportedRates2;
                                String hs2LogTag2222 = Utils.hs2LogTag(getClass());
                                StringBuilder sb2222 = new StringBuilder();
                                iesFound = iesFound2;
                                sb2222.append("Caught ");
                                sb2222.append(e);
                                Log.d(hs2LogTag2222, sb2222.toString());
                                if (ssidOctets2 != null) {
                                    exception = e;
                                    ssidOctets = ssidOctets2;
                                    if (ssidOctets == null) {
                                    }
                                    this.mStream1 = apCapInfo.getStream1();
                                    this.mStream2 = apCapInfo.getStream2();
                                    this.mStream3 = apCapInfo.getStream3();
                                    this.mStream4 = apCapInfo.getStream4();
                                    this.mTxMcsSet = apCapInfo.getTxMcsSet();
                                    this.mSSID = ssid2;
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
                                    if (!extendedSupportedRates.isValid()) {
                                    }
                                    if (!supportedRates.isValid()) {
                                    }
                                } else {
                                    throw new IllegalArgumentException("Malformed IE string (no SSID)", e);
                                }
                            }
                        }
                        i++;
                        iesFound3 = iesFound2;
                        supportedRates2 = supportedRates;
                        isHiddenSsid3 = isHiddenSsid;
                        length = length;
                        informationElementArr = infoElements;
                        apCapInfo2 = apCapInfo;
                        ssid4 = ssid;
                        extendedSupportedRates2 = extendedSupportedRates;
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e7) {
                        e = e7;
                        extendedSupportedRates = extendedSupportedRates2;
                        ssid = ssid4;
                        apCapInfo = apCapInfo2;
                        isHiddenSsid = isHiddenSsid3;
                        supportedRates = supportedRates2;
                        String hs2LogTag22222 = Utils.hs2LogTag(getClass());
                        StringBuilder sb22222 = new StringBuilder();
                        iesFound = iesFound2;
                        sb22222.append("Caught ");
                        sb22222.append(e);
                        Log.d(hs2LogTag22222, sb22222.toString());
                        if (ssidOctets2 != null) {
                        }
                    }
                }
                extendedSupportedRates = extendedSupportedRates2;
                ssid = ssid4;
                apCapInfo = apCapInfo2;
                isHiddenSsid = isHiddenSsid3;
                supportedRates = supportedRates2;
                iesFound = iesFound3;
                ssidOctets = ssidOctets2;
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e8) {
                e = e8;
                extendedSupportedRates = extendedSupportedRates2;
                iesFound2 = iesFound3;
                ssid = null;
                apCapInfo = apCapInfo2;
                isHiddenSsid = false;
                supportedRates = supportedRates2;
                ssidOctets2 = null;
                String hs2LogTag222222 = Utils.hs2LogTag(getClass());
                StringBuilder sb222222 = new StringBuilder();
                iesFound = iesFound2;
                sb222222.append("Caught ");
                sb222222.append(e);
                Log.d(hs2LogTag222222, sb222222.toString());
                if (ssidOctets2 != null) {
                }
            }
            if (ssidOctets == null) {
                try {
                    ssid3 = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(ssidOctets)).toString();
                } catch (CharacterCodingException e9) {
                    ssid3 = null;
                }
                if (ssid3 == null) {
                    if (extendedCapabilities.isStrictUtf8()) {
                        if (exception != null) {
                            throw new IllegalArgumentException("Failed to decode SSID in dubious IE string");
                        }
                    }
                    ssid3 = new String(ssidOctets, StandardCharsets.ISO_8859_1);
                }
                isHiddenSsid2 = true;
                int length2 = ssidOctets.length;
                int i3 = 0;
                while (true) {
                    if (i3 >= length2) {
                        ssid2 = ssid3;
                        break;
                    } else if (ssidOctets[i3] != 0) {
                        isHiddenSsid2 = false;
                        ssid2 = ssid3;
                        break;
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
            if (!extendedSupportedRates.isValid()) {
                maxRateB = extendedSupportedRates.mRates.get(extendedSupportedRates.mRates.size() - 1).intValue();
            } else {
                maxRateB = 0;
            }
            if (!supportedRates.isValid()) {
                int maxRateA = supportedRates.mRates.get(supportedRates.mRates.size() - 1).intValue();
                this.mMaxRate = maxRateA > maxRateB ? maxRateA : maxRateB;
                this.mWifiMode = InformationElementUtil.WifiMode.determineMode(this.mPrimaryFreq, this.mMaxRate, vhtOperation.isValid(), iesFound.contains(61), iesFound.contains(42));
                return;
            }
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
        if (cap == null) {
            return "NULL";
        }
        if (cap.contains("WEP")) {
            return "WEP";
        }
        if (cap.contains("PSK")) {
            return "PSK";
        }
        if (cap.contains("EAP")) {
            return "EAP";
        }
        return "NONE";
    }

    public String getTrimmedSSID() {
        if (this.mSSID == null) {
            return "";
        }
        for (int n = 0; n < this.mSSID.length(); n++) {
            if (this.mSSID.charAt(n) != 0) {
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
        ANQPElement osuProviders;
        Map<Constants.ANQPElementType, ANQPElement> map = this.mANQPElements;
        if (map == null || (osuProviders = map.get(Constants.ANQPElementType.HSOSUProviders)) == null) {
            return null;
        }
        return ((RawByteElement) osuProviders).getPayload();
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
        if (!getSSID().equals(that.getSSID()) || getBSSID() != that.getBSSID()) {
            return false;
        }
        if (getCapString(getCap()).equals(getCapString(that.getCap()))) {
            return true;
        }
        Log.d(TAG, "CapChanged: " + getCap() + ", that Cap: " + that.getCap());
        return false;
    }

    public int hashCode() {
        long j = this.mBSSID;
        return ((((this.mSSID + getCapString(this.mCapabilities)).hashCode() * 31) + ((int) (j >>> 32))) * 31) + ((int) j);
    }

    public String toString() {
        return String.format("NetworkInfo{SSID='%s', HESSID=%x, BSSID=%x, StationCount=%d, ChannelUtilization=%d, Capacity=%d, Ant=%s, Internet=%s, HSRelease=%s, AnqpDomainID=%d, AnqpOICount=%d, RoamingConsortiums=%s}", StringUtilEx.safeDisplaySsid(this.mSSID), Long.valueOf(this.mHESSID), Long.valueOf(this.mBSSID), Integer.valueOf(this.mStationCount), Integer.valueOf(this.mChannelUtilization), Integer.valueOf(this.mCapacity), this.mAnt, Boolean.valueOf(this.mInternet), this.mHSRelease, Integer.valueOf(this.mAnqpDomainID), Integer.valueOf(this.mAnqpOICount), Utils.roamingConsortiumsToString(this.mRoamingConsortiums));
    }

    public String toKeyString() {
        if (this.mHESSID != 0) {
            return String.format("'%s':%012x (%012x)", this.mSSID, Long.valueOf(this.mBSSID), Long.valueOf(this.mHESSID));
        }
        return String.format("'%s':%012x", this.mSSID, Long.valueOf(this.mBSSID));
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
            sb.append(String.format("%02x", Long.valueOf((mac >>> (n * 8)) & 255)));
        }
        return sb.toString();
    }
}
