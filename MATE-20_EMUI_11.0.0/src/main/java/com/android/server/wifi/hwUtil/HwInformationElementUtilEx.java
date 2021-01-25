package com.android.server.wifi.hwUtil;

import android.hardware.wifi.supplicant.V1_0.WpsConfigMethods;
import android.net.wifi.ScanResult;
import android.util.Log;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class HwInformationElementUtilEx implements IHwInformationElementUtilEx {
    private static final int[] BCC_IE_HEADER = {0, 224, 252, 64, 0, 0, 0, 1, 0};
    private static final int CAP_WAPI_BIT_OFFSET = 7;
    private static final int CHANNEL_CENTER_SEGMENT_OFFSET_160M = 8;
    private static final int[] CHANNEL_INDEX_OFFSET_160M = {-14, -10, -6, -2, 2, 6, 10, 14};
    private static final int CHIPSET_160M = 32;
    private static final int CHIPSET_NARROWBAND = 16;
    private static final String CHIPSET_WIFI_CATEGORY = "chipset_wifi_category";
    private static final String CHIPSET_WIFI_FEATURE_CAPABILITY = "chipset_wifi_feature_capability";
    private static final int EID_80211AX = 255;
    private static final int EID_HT_INFO = 61;
    private static final int EID_VENDOR = 221;
    private static final int EID_VHT_OPERATION = 192;
    private static final int FIRST_SUBIE_INEDEX = 9;
    private static final int HE_CAPABILITY_160M = 8;
    private static final int HE_CAPABILITY_160M_INDEX = 7;
    private static final int HE_CAPABILITY_80211AX = 35;
    private static final int HT_PRIMARY_CHANNEL_INDEX = 0;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final byte MASK_11V = 8;
    private static final int MASK_BYTE = 255;
    private static final int NARROWBAND_CAPABILITY = 32;
    private static final int NARROWBAND_OFFSET = 1;
    private static final int OFFSET_11V = 2;
    private static final int PMF_C = 1;
    private static final int PMF_INVALID = 3;
    private static final int PMF_NONE = 0;
    private static final int PMF_R = 2;
    private static final int RSN_MFPC_BIT_OFFSET = 7;
    private static final int RSN_MFPR_BIT_OFFSET = 6;
    private static final String TAG = "HwInformationElementUtilEx";
    private static final int TYPE_LEN = 254;
    private static final int TYPE_NARROWBAND_SUBIE = 253;
    private static final int[] VENDOR_HUAWEI_OUI = {0, 224, 252};
    private static final int VHT_CHANNEL_CENTER_SEGMENT0_INDEX = 1;
    private static final int VHT_CHANNEL_CENTER_SEGMENT1_INDEX = 2;
    private static final int VHT_CHANNEL_SEGMENT_DEFAULT = 0;
    private static final int VHT_CHANNEL_WIDTH_160M = 2;
    private static final int VHT_CHANNEL_WIDTH_80MOR160M = 1;
    private static final int VHT_CHANNEL_WIDTH_INDEX = 0;
    private static final byte WAPI_AKM_CERT = 1;
    private static final byte WAPI_AKM_PSK = 2;
    private static final int WIFI_CAPABILITY_DEFAULT = 0;
    public static final int WIFI_CATEGORY_DEFAULT = 1;
    private static final int WIFI_CATEGORY_WIFI6 = 2;
    private static final int WIFI_CATEGORY_WIFI6_PLUS = 3;
    private static boolean is80211K = false;
    private static boolean is80211R = false;
    private static boolean is80211V = false;
    private static int pmfType = 0;
    private static boolean sIsNeedUpdate = true;

    @Override // com.android.server.wifi.hwUtil.IHwInformationElementUtilEx
    public int getWifiCategoryFromIes(ScanResult.InformationElement[] ies, int chipsetWifiFeatureCapability) {
        if (ies == null) {
            return 1;
        }
        boolean isChipset160mSupported = (chipsetWifiFeatureCapability & 32) == 32;
        boolean isChipsetNarrowBandSupported = (chipsetWifiFeatureCapability & 16) == 16;
        boolean isNarrowBandSupported = false;
        boolean isHuaweiRouter = false;
        boolean isWifi6Supported = false;
        boolean is160mSupported = false;
        for (ScanResult.InformationElement ie : ies) {
            if (!(ie == null || ie.id != 255 || ie.bytes == null)) {
                try {
                    if (ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).get() == 35) {
                        isWifi6Supported = true;
                        is160mSupported = isChipset160mSupported && is160mSupported(ie) && is160mConfigurated(ies);
                    }
                    if (isWifi6Supported && isHuaweiRouter && (is160mSupported || isNarrowBandSupported)) {
                        return 3;
                    }
                    if (isWifi6Supported && !isChipset160mSupported && !isChipsetNarrowBandSupported) {
                        return 2;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(TAG, "getWifiCategory: BufferUnderflowException happen");
                }
            }
            if (ie != null && ie.id == 221) {
                boolean isHuaweiRouter2 = isHuaweiRouter ? true : isHuaweiRouter(ie);
                boolean isNarrowBandSupported2 = isNarrowBandSupported(ie) && isChipsetNarrowBandSupported;
                if (isWifi6Supported && isHuaweiRouter2 && (is160mSupported || isNarrowBandSupported2)) {
                    return 3;
                }
                isHuaweiRouter = isHuaweiRouter2;
                isNarrowBandSupported = isNarrowBandSupported2;
            }
        }
        if (isWifi6Supported) {
            return 2;
        }
        return 1;
    }

    public static String parsePmfCapabilities(short pmf, ArrayList<Integer> pmfKeyManagement) {
        String pmfCapabilities;
        String pmfCapabilities2;
        pmfType = 0;
        if ((pmf & WpsConfigMethods.PUSHBUTTON) != 0) {
            pmfCapabilities = "C:1 ";
            pmfType = 1;
        } else {
            pmfCapabilities = "C:0 ";
            pmfType = 0;
        }
        if ((pmf & 64) != 0) {
            pmfCapabilities2 = pmfCapabilities + "R:1 ";
            if (pmfType == 0) {
                pmfType = 3;
            } else {
                pmfType = 2;
            }
        } else {
            pmfCapabilities2 = pmfCapabilities + "R:0 ";
        }
        String pmfCapabilities3 = pmfCapabilities2 + "K:";
        if (pmfKeyManagement.isEmpty()) {
            return pmfCapabilities3 + "NONE";
        }
        int rsnKeyCount = pmfKeyManagement.size();
        int i = 0;
        while (i < rsnKeyCount) {
            StringBuilder sb = new StringBuilder();
            sb.append(pmfCapabilities3);
            sb.append(i == 0 ? "" : "+");
            sb.append(String.valueOf(pmfKeyManagement.get(i)));
            pmfCapabilities3 = sb.toString();
            i++;
        }
        return pmfCapabilities3;
    }

    private static boolean isHuaweiRouter(ScanResult.InformationElement ie) {
        if (ie.bytes.length <= VENDOR_HUAWEI_OUI.length) {
            return false;
        }
        for (int index = 0; index < VENDOR_HUAWEI_OUI.length; index++) {
            if ((ie.bytes[index] & 255) != VENDOR_HUAWEI_OUI[index]) {
                return false;
            }
        }
        return true;
    }

    private static boolean is160mSupported(ScanResult.InformationElement ie) {
        if (ie.bytes.length > 7 && (ie.bytes[7] & 8) == 8) {
            return true;
        }
        return false;
    }

    private boolean is160mConfigurated(ScanResult.InformationElement[] ies) {
        boolean isHtInfoFound = false;
        boolean isVhtOperationFound = false;
        int channelCenterSegment1 = 0;
        int channelIndex = 0;
        for (ScanResult.InformationElement ie : ies) {
            if (ie != null && ie.id == 61) {
                if (ie.bytes.length <= 0) {
                    return false;
                }
                channelIndex = ie.bytes[0] & 255;
                isHtInfoFound = true;
            }
            if (ie != null && ie.id == 192) {
                if (ie.bytes.length <= 2) {
                    return false;
                }
                int channelWidth = ie.bytes[0] & 255;
                int channelCenterSegment0 = ie.bytes[1] & 255;
                channelCenterSegment1 = ie.bytes[2] & 255;
                if (channelWidth == 2) {
                    return true;
                }
                if (!(channelWidth == 1 && channelCenterSegment1 != 0 && (channelCenterSegment0 - channelCenterSegment1 == 8 || channelCenterSegment1 - channelCenterSegment0 == 8))) {
                    return false;
                }
                isVhtOperationFound = true;
            }
            if (isHtInfoFound && isVhtOperationFound) {
                break;
            }
        }
        int channelIndexOffset = channelCenterSegment1 - channelIndex;
        int i = 0;
        while (true) {
            int[] iArr = CHANNEL_INDEX_OFFSET_160M;
            if (i >= iArr.length) {
                return false;
            }
            if (channelIndexOffset == iArr[i]) {
                return true;
            }
            i++;
        }
    }

    private static boolean isNarrowBandSupported(ScanResult.InformationElement ie) {
        if (ie.bytes.length <= BCC_IE_HEADER.length) {
            return false;
        }
        for (int index = 0; index < BCC_IE_HEADER.length; index++) {
            if ((ie.bytes[index] & 255) != BCC_IE_HEADER[index]) {
                return false;
            }
        }
        int index2 = 9;
        while (index2 + 1 < ie.bytes.length && index2 > 0) {
            if ((ie.bytes[index2] & 255) == TYPE_NARROWBAND_SUBIE) {
                int index3 = index2 + ((ie.bytes[index2 + 1] & 255) - 1);
                if (index3 < ie.bytes.length && (ie.bytes[index3] & 32) == 32) {
                    return true;
                }
                return false;
            }
            index2 += (ie.bytes[index2 + 1] & 255) + 2;
        }
        return false;
    }

    public static class HiLinkNetwork {
        public static final int ENTERPRISE_HI_WIFI = 3;
        private static final int[] FORMAT_HILINK = {0, 224, 252, 128, 0, 0, 0, 1, 0};
        private static final int[] FORMAT_HILINK_OUI = {0, 224, 252, 64, 0, 0, 0};
        private static final int HILINK_OUI_HEAD_LEN = 9;
        private static final int LOGO_ID = 249;
        private static final int MASK_HILINK = 255;
        private static final int NORMAL_WIFI = 0;
        public boolean isHiLinkNetwork = false;

        public int parseHiLogoTag(ScanResult.InformationElement[] ies) {
            if (ies == null) {
                return 0;
            }
            for (ScanResult.InformationElement ie : ies) {
                if (221 == ie.id && checkHiLinkOUISection(ie.bytes)) {
                    int index = 9;
                    while (index < ie.bytes.length && index >= 0) {
                        int element = ie.bytes[index] & 255;
                        Log.d(HwInformationElementUtilEx.TAG, "element:" + element);
                        if (element == LOGO_ID) {
                            return ie.bytes[index + 2];
                        }
                        try {
                            index += (ie.bytes[index + 1] & 255) + 2;
                        } catch (IndexOutOfBoundsException e) {
                            Log.w(HwInformationElementUtilEx.TAG, "the information elements is invalid");
                            return 0;
                        }
                    }
                    return 0;
                }
            }
            return 0;
        }

        private boolean checkHiLinkOUISection(byte[] bytes) {
            if (bytes == null || bytes.length < FORMAT_HILINK_OUI.length) {
                Log.w(HwInformationElementUtilEx.TAG, "the information elements's length is invalid");
                return false;
            }
            int index = 0;
            while (true) {
                int[] iArr = FORMAT_HILINK_OUI;
                if (index >= iArr.length) {
                    return true;
                }
                if ((bytes[index] & 255) != iArr[index]) {
                    return false;
                }
                index++;
            }
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
            int index = 0;
            while (true) {
                int[] iArr = FORMAT_HILINK;
                if (index >= iArr.length) {
                    return true;
                }
                if ((bytes[index] & 255) != iArr[index]) {
                    return false;
                }
                index++;
            }
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
                    int i = 0;
                    this.mStream1 = this.mStream1 == 0 ? 0 : 1;
                    this.mStream2 = this.mStream2 == 0 ? 0 : 1;
                    this.mStream3 = this.mStream3 == 0 ? 0 : 1;
                    if (this.mStream4 != 0) {
                        i = 1;
                    }
                    this.mStream4 = i;
                    data.position(data.position() + 8);
                    this.mTxMcsSet = (data.get() >> 4) & 15;
                } else {
                    throw new IllegalArgumentException("APCapInfo length smaller than 16: " + ie.bytes.length);
                }
            } catch (IllegalArgumentException e) {
                if (HwInformationElementUtilEx.HWFLOW) {
                    Log.d(HwInformationElementUtilEx.TAG, "APCapInfo" + e);
                }
            }
        }
    }

    public static void parseWapiElement(ScanResult.InformationElement ie, ArrayList<Integer> protocol, ArrayList<ArrayList<Integer>> keyManagement, ArrayList<ArrayList<Integer>> pairwiseCipher) {
        if (ie.bytes != null && ie.bytes.length > 7) {
            protocol.add(4);
            ArrayList<Integer> wapiKeyManagement = new ArrayList<>();
            ArrayList<Integer> wapiPairwiseCipher = new ArrayList<>();
            byte akm = ie.bytes[7];
            if (akm == 1) {
                Log.d(TAG, "parseWapiElement: This is a WAPI CERT network");
                wapiKeyManagement.add(13);
            } else if (akm != 2) {
                Log.e(TAG, "parseWapiElement: akm=" + ((int) akm) + " Unknown WAPI network type");
            } else {
                Log.d(TAG, "parseWapiElement: This is a WAPI PSK network");
                wapiKeyManagement.add(1);
            }
            keyManagement.add(wapiKeyManagement);
            pairwiseCipher.add(wapiPairwiseCipher);
        }
    }

    public static void parse80211KVRCap(ScanResult.InformationElement ie) {
        boolean z = false;
        if (sIsNeedUpdate) {
            sIsNeedUpdate = false;
            is80211K = false;
            is80211V = false;
            is80211R = false;
            pmfType = 0;
        }
        if (ie.id == 70) {
            is80211K = true;
        }
        if (ie.id == 127 && ie.bytes.length > 2) {
            if ((ie.bytes[2] & 8) == 8) {
                z = true;
            }
            is80211V = z;
        }
        if (ie.id == 54) {
            is80211R = true;
        }
    }

    public static String generateKVRCapabilitiesString(String capabilities) {
        String KVRCapabilities = capabilities;
        if (is80211K) {
            KVRCapabilities = KVRCapabilities + "[K]";
        }
        if (is80211V) {
            KVRCapabilities = KVRCapabilities + "[V]";
        }
        if (is80211R) {
            KVRCapabilities = KVRCapabilities + "[R]";
        }
        sIsNeedUpdate = true;
        return KVRCapabilities;
    }

    public static String generatePmfCapabilitiesString(String capabilities) {
        String PmfCapabilities = capabilities;
        if (pmfType == 1) {
            PmfCapabilities = PmfCapabilities + "[PMFC]";
        }
        if (pmfType == 2) {
            PmfCapabilities = PmfCapabilities + "[PMFR]";
        }
        if (pmfType == 3) {
            PmfCapabilities = PmfCapabilities + "[PMFE]";
        }
        sIsNeedUpdate = true;
        return PmfCapabilities;
    }
}
