package com.android.server.wifi.wifipro;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwDualBandRelationManager {
    public static final int SECURITY_EAP = 3;
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_WAPI_CERT = 5;
    public static final int SECURITY_WAPI_PSK = 4;
    public static final int SECURITY_WEP = 1;
    private static HwDualBandRelationManager mHwDualBandRelationManager;
    private Context mContext;
    private Handler mHandler;
    private HwDualBandInformationManager mHwDualBandInformationManager;
    private WifiManager mWifiManager;
    private WifiProHistoryRecordManager mWifiProHistoryRecordManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.HwDualBandRelationManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.HwDualBandRelationManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.HwDualBandRelationManager.<clinit>():void");
    }

    public static HwDualBandRelationManager createInstance(Context context, Handler handler) {
        if (mHwDualBandRelationManager == null) {
            mHwDualBandRelationManager = new HwDualBandRelationManager(context, handler);
        }
        return mHwDualBandRelationManager;
    }

    public static HwDualBandRelationManager getInstance() {
        return mHwDualBandRelationManager;
    }

    private HwDualBandRelationManager(Context context, Handler handler) {
        this.mWifiManager = null;
        this.mHwDualBandInformationManager = null;
        this.mContext = context;
        this.mHandler = handler;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(this.mContext, this.mWifiManager);
    }

    public void updateAPRelation() {
        Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation");
        this.mHwDualBandInformationManager = HwDualBandInformationManager.getInstance();
        List<ScanResult> lists = this.mWifiManager.getScanResults();
        if (lists == null) {
            Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation lists == null");
            return;
        }
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null || mWifiInfo.getBSSID() == null) {
            Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation mWifiInfo == null");
            return;
        }
        String bssid = mWifiInfo.getBSSID();
        boolean is5GAP = ScanResult.is5GHz(mWifiInfo.getFrequency());
        Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation lists.size = " + lists.size());
        WifiProDualBandApInfoRcd APInfo = this.mHwDualBandInformationManager.getDualBandAPInfo(bssid);
        if (APInfo == null) {
            Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation APInfo == null bssid = " + bssid);
            return;
        }
        String ssid;
        WifiProDualBandApInfoRcd ScanAPInfo;
        WifiProRelateApRcd RelateAPInfo;
        int isDualBandAP;
        if (is5GAP) {
            for (ScanResult result : lists) {
                if (isAPInConfig(result, bssid) && result.BSSID != null && !result.BSSID.equals(bssid) && ScanResult.is24GHz(result.frequency)) {
                    if (isHaveDuplicateRecord(result, APInfo)) {
                        Log.w(HwDualBandMessageUtil.TAG, "isHaveDuplicateRecord 2.4G APInfo.mApSSID == " + APInfo.apBSSID + " mApAuthType == " + APInfo.mApAuthType);
                        this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(APInfo.mApSSID, APInfo.mApAuthType.shortValue());
                        return;
                    } else if (isHaveMultipleRecord(result)) {
                        ssid = "\"" + result.SSID + "\"";
                        Log.w(HwDualBandMessageUtil.TAG, "isHaveMultipleRecord  2.4G ssid == " + ssid);
                        this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(ssid, getSecurity(result));
                    } else {
                        ScanAPInfo = this.mHwDualBandInformationManager.getDualBandAPInfo(result.BSSID);
                        if (ScanAPInfo != null && ScanAPInfo.mServingBand.shortValue() == (short) 1 && ScanAPInfo.mInetCapability.shortValue() == (short) 1) {
                            RelateAPInfo = getRelateAPInfo(result.BSSID, bssid);
                            if (RelateAPInfo == null) {
                                if (isDualBandAP(result.BSSID, bssid)) {
                                    isDualBandAP = SECURITY_WEP;
                                } else {
                                    isDualBandAP = SECURITY_PSK;
                                }
                                RelateAPInfo = new WifiProRelateApRcd(result.BSSID);
                                RelateAPInfo.mRelatedBSSID = bssid;
                                RelateAPInfo.mRelateType = isDualBandAP;
                                RelateAPInfo.mMaxRelatedRSSI = mWifiInfo.getRssi();
                                RelateAPInfo.mMinCurrentRSSI = result.level;
                                this.mWifiProHistoryRecordManager.saveRelateApInfo(RelateAPInfo);
                            } else if (mWifiInfo.getRssi() > RelateAPInfo.mMaxRelatedRSSI || RelateAPInfo.mMaxRelatedRSSI == 0) {
                                Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation max 5G AP relate rssi");
                                RelateAPInfo.mMaxRelatedRSSI = mWifiInfo.getRssi();
                                RelateAPInfo.mMinCurrentRSSI = result.level;
                                this.mWifiProHistoryRecordManager.saveRelateApInfo(RelateAPInfo);
                            }
                        }
                    }
                }
            }
        } else {
            for (ScanResult result2 : lists) {
                if (isAPInConfig(result2, bssid) && result2.BSSID != null && !result2.BSSID.equals(bssid) && ScanResult.is5GHz(result2.frequency)) {
                    if (isHaveDuplicateRecord(result2, APInfo)) {
                        Log.w(HwDualBandMessageUtil.TAG, "isHaveDuplicateRecord 5G APInfo.mApSSID == " + APInfo.mApSSID + " mApAuthType == " + APInfo.mApAuthType);
                        this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(APInfo.mApSSID, APInfo.mApAuthType.shortValue());
                        return;
                    } else if (isHaveMultipleRecord(result2)) {
                        ssid = "\"" + result2.SSID + "\"";
                        Log.w(HwDualBandMessageUtil.TAG, "isHaveMultipleRecord 5G ssid == " + ssid);
                        this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(ssid, getSecurity(result2));
                    } else {
                        ScanAPInfo = this.mHwDualBandInformationManager.getDualBandAPInfo(result2.BSSID);
                        if (ScanAPInfo == null) {
                            Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation result.BSSID = " + result2.BSSID + " ssid = " + result2.SSID);
                            this.mHwDualBandInformationManager.saveAPInfo(result2, getSecurity(result2));
                        } else if (isDualBandAP(bssid, result2.BSSID) && ScanAPInfo.mInetCapability.shortValue() != (short) 1) {
                            Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation update dual band internet capability");
                            ScanAPInfo.mInetCapability = Short.valueOf((short) 1);
                            this.mWifiProHistoryRecordManager.saveDualBandApInfo(ScanAPInfo);
                        }
                        Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation result.BSSID = " + result2.BSSID + " ssid = " + result2.SSID);
                        RelateAPInfo = getRelateAPInfo(bssid, result2.BSSID);
                        if (RelateAPInfo == null) {
                            if (isDualBandAP(bssid, result2.BSSID)) {
                                isDualBandAP = SECURITY_WEP;
                                WifiProStatisticsManager.getInstance().uploadWifiProDualbandApInfoEvent(SECURITY_WEP, bssid, result2.BSSID);
                            } else {
                                isDualBandAP = SECURITY_PSK;
                                WifiProStatisticsManager.getInstance().uploadWifiProDualbandApInfoEvent(SECURITY_PSK, bssid, result2.BSSID);
                            }
                            RelateAPInfo = new WifiProRelateApRcd(bssid);
                            RelateAPInfo.mRelateType = isDualBandAP;
                            RelateAPInfo.mRelatedBSSID = result2.BSSID;
                            RelateAPInfo.mMaxCurrentRSSI = mWifiInfo.getRssi();
                            RelateAPInfo.mMinRelatedRSSI = result2.level;
                            Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation add new relation  isDualBandAP = " + isDualBandAP);
                            this.mWifiProHistoryRecordManager.saveRelateApInfo(RelateAPInfo);
                        } else if (mWifiInfo.getRssi() > RelateAPInfo.mMaxCurrentRSSI || RelateAPInfo.mMaxCurrentRSSI == 0) {
                            Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation 2.4G AP relate rssi");
                            RelateAPInfo.mMaxCurrentRSSI = mWifiInfo.getRssi();
                            RelateAPInfo.mMinRelatedRSSI = result2.level;
                            this.mWifiProHistoryRecordManager.saveRelateApInfo(RelateAPInfo);
                        }
                    }
                }
            }
            APInfo = this.mHwDualBandInformationManager.getDualBandAPInfo(bssid);
            if (APInfo == null) {
                Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation APInfo == null");
                return;
            }
            Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation APInfo.getRelateApRcds().size() = " + APInfo.getRelateApRcds().size());
            if (APInfo.getRelateApRcds().size() > 0) {
                if (APInfo.getRelateApRcds().size() == SECURITY_WEP && ((WifiProRelateApRcd) APInfo.getRelateApRcds().get(SECURITY_NONE)).mRelateType == SECURITY_WEP) {
                    Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation MSG_DUAL_BAND_WIFI_TYPE_SINGLE");
                    this.mHandler.sendEmptyMessage(16);
                } else {
                    Log.w(HwDualBandMessageUtil.TAG, "updateAPRelation MSG_DUAL_BAND_WIFI_TYPE_MIX size() = " + APInfo.getRelateApRcds().size());
                    this.mHandler.sendEmptyMessage(17);
                }
            }
        }
    }

    public List<WifiProRelateApRcd> getRelateAPInfos(String bssid) {
        List<WifiProRelateApRcd> relateApList = new ArrayList();
        this.mWifiProHistoryRecordManager.getRelateApList(bssid, relateApList);
        return relateApList;
    }

    public WifiProRelateApRcd getRelateAPInfo(String bssid, String relateAPbssid) {
        WifiProRelateApRcd result = null;
        List<WifiProRelateApRcd> RelateAPInfos = getRelateAPInfos(bssid);
        if (RelateAPInfos.size() == 0) {
            return null;
        }
        for (WifiProRelateApRcd info : RelateAPInfos) {
            if (info.apBSSID.equals(bssid) && info.mRelatedBSSID.equals(relateAPbssid)) {
                result = info;
            }
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isDualBandAP(String m24GAP, String m5GAP) {
        if (m24GAP == null || m5GAP == null || !m24GAP.substring(SECURITY_NONE, m24GAP.length() - 1).equals(m5GAP.substring(SECURITY_NONE, m5GAP.length() - 1))) {
            return false;
        }
        return true;
    }

    private boolean isAPInConfig(ScanResult result, String bssid) {
        if (result.SSID == null || result.SSID.length() <= 0) {
            Log.d(HwDualBandMessageUtil.TAG, "isAPInConfig, result.SSID error!");
            return false;
        }
        int authType = getSecurity(result);
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            Log.d(HwDualBandMessageUtil.TAG, "isAPInConfig, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return false;
        }
        for (WifiConfiguration nextConfig : configNetworks) {
            if (isValidConfig(nextConfig) && nextConfig.SSID.contains(result.SSID) && authType == nextConfig.getAuthType()) {
                if (isAPHaveInternet(nextConfig) || isDualBandAP(bssid, result.BSSID)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidConfig(WifiConfiguration config) {
        boolean z = true;
        if (config == null || config.SSID == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() > SECURITY_WEP) {
            z = false;
        }
        return z;
    }

    private boolean isAPHaveInternet(WifiConfiguration config) {
        return WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
    }

    private static int getSecurity(ScanResult result) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + result.SSID + "\"";
        if (result.capabilities.contains("WEP")) {
            config.allowedKeyManagement.set(SECURITY_NONE);
            config.allowedAuthAlgorithms.set(SECURITY_NONE);
            config.allowedAuthAlgorithms.set(SECURITY_WEP);
        }
        if (result.capabilities.contains("PSK")) {
            config.allowedKeyManagement.set(SECURITY_WEP);
        }
        if (result.capabilities.contains("EAP")) {
            config.allowedKeyManagement.set(SECURITY_PSK);
            config.allowedKeyManagement.set(SECURITY_EAP);
        }
        if (config.allowedKeyManagement.get(SECURITY_WEP)) {
            return SECURITY_WEP;
        }
        if (config.allowedKeyManagement.get(SECURITY_WAPI_PSK)) {
            return SECURITY_WAPI_PSK;
        }
        if (config.allowedKeyManagement.get(SECURITY_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(SECURITY_EAP)) {
            return SECURITY_EAP;
        }
        if (config.allowedKeyManagement.get(6)) {
            return 6;
        }
        if (config.allowedKeyManagement.get(7)) {
            return 7;
        }
        if (config.allowedKeyManagement.get(8)) {
            return 8;
        }
        if (config.allowedKeyManagement.get(9)) {
            return 9;
        }
        return SECURITY_NONE;
    }

    private boolean isHaveDuplicateRecord(ScanResult result, WifiProDualBandApInfoRcd info) {
        if (("\"" + result.SSID + "\"").equals(info.mApSSID) && getSecurity(result) == info.mApAuthType.shortValue()) {
            return true;
        }
        return false;
    }

    private boolean isHaveMultipleRecord(ScanResult result) {
        int num = SECURITY_NONE;
        String ssid = "\"" + result.SSID + "\"";
        List<WifiProDualBandApInfoRcd> list = this.mWifiProHistoryRecordManager.getDualBandApInfoBySsid(ssid);
        if (list != null && list.size() >= SECURITY_WEP) {
            for (WifiProDualBandApInfoRcd record : list) {
                if (getSecurity(result) == record.mApAuthType.shortValue() && !record.apBSSID.equals(result.BSSID)) {
                    num += SECURITY_WEP;
                }
            }
        }
        return num > 0 || this.mWifiProHistoryRecordManager.isEnterpriseAP(ssid, getSecurity(result)) || this.mHwDualBandInformationManager.isHaveMultipleAP(result.BSSID, ssid, getSecurity(result));
    }
}
