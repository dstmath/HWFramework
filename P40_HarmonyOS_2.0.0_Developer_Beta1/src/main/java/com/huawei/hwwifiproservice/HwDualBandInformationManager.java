package com.huawei.hwwifiproservice;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwDualBandInformationManager {
    private static HwDualBandInformationManager sHwDualBandInformationManager = null;
    private Context mContext;
    private WifiManager mWifiManager = null;
    private WifiProHistoryRecordManager mWifiProHistoryRecordManager;

    public static HwDualBandInformationManager createInstance(Context context) {
        if (sHwDualBandInformationManager == null) {
            sHwDualBandInformationManager = new HwDualBandInformationManager(context);
        }
        return sHwDualBandInformationManager;
    }

    public static HwDualBandInformationManager getInstance() {
        return sHwDualBandInformationManager;
    }

    private HwDualBandInformationManager(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(this.mContext, this.mWifiManager);
    }

    public void updateAPInfo(WifiProDualBandApInfoRcd tempApInfo) {
        Log.e(HwDualBandMessageUtil.TAG, "updateAPInfo update AP info ssid = " + StringUtilEx.safeDisplaySsid(tempApInfo.mApSSID));
        this.mWifiProHistoryRecordManager.saveDualBandApInfo(tempApInfo);
    }

    public boolean saveApInfo() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null) {
            return false;
        }
        String mCurrentBSSID = mWifiInfo.getBSSID();
        if (mCurrentBSSID == null) {
            Log.e(HwDualBandMessageUtil.TAG, "mCurrentBSSID == null");
            return false;
        }
        WifiProDualBandApInfoRcd apInfo = new WifiProDualBandApInfoRcd(mCurrentBSSID);
        apInfo.mApBSSID = mCurrentBSSID;
        apInfo.mApSSID = mWifiInfo.getSSID();
        apInfo.mApAuthType = Short.valueOf((short) getAuthType(mWifiInfo.getNetworkId()));
        apInfo.mChannelFrequency = mWifiInfo.getFrequency();
        apInfo.mInetCapability = 1;
        apInfo.mInBlackList = 0;
        if (ScanResult.is5GHz(mWifiInfo.getFrequency())) {
            apInfo.mServingBand = 2;
        } else {
            apInfo.mServingBand = 1;
        }
        Log.i(HwDualBandMessageUtil.TAG, "ConnectedState apInfo.mApBSSID = " + WifiProCommonUtils.safeDisplayBssid(apInfo.mApBSSID) + "  apInfo.mApSSID = " + StringUtilEx.safeDisplaySsid(apInfo.mApSSID) + " apInfo.mChannelFrequency = " + apInfo.mChannelFrequency + " apInfo.mServingBand = " + apInfo.mServingBand);
        this.mWifiProHistoryRecordManager.saveDualBandApInfo(apInfo);
        return true;
    }

    public WifiProDualBandApInfoRcd getDualBandAPInfo(String bssid) {
        List<WifiProRelateApRcd> relateApList = new ArrayList<>();
        WifiProDualBandApInfoRcd result = this.mWifiProHistoryRecordManager.getDualBandApRecord(bssid);
        if (result != null) {
            this.mWifiProHistoryRecordManager.getRelateApList(bssid, relateApList);
            result.setRelateApRcds(relateApList);
        } else {
            Log.e(HwDualBandMessageUtil.TAG, "getDualBandAPInfo return null");
        }
        return result;
    }

    public void deleteDualbandApInfoBySsid(String ssid, int authType) {
        Log.e(HwDualBandMessageUtil.TAG, "deleteDualbandApInfoBySsid ssid= " + StringUtilEx.safeDisplaySsid(ssid));
        List<WifiProDualBandApInfoRcd> rcdList = this.mWifiProHistoryRecordManager.getDualBandApInfoBySsid(ssid);
        if (!(rcdList == null || rcdList.size() == 0)) {
            Log.e(HwDualBandMessageUtil.TAG, "deleteDualbandApInfoBySsid rcdList.size() = " + rcdList.size());
            for (WifiProDualBandApInfoRcd rcd : rcdList) {
                if (rcd.mApAuthType.shortValue() == authType) {
                    Log.e(HwDualBandMessageUtil.TAG, "deleteDualbandApInfoBySsid  rcd.mApSSID = " + WifiProCommonUtils.safeDisplayBssid(rcd.mApBSSID) + " rcd.mServingBand = " + rcd.mServingBand);
                    this.mWifiProHistoryRecordManager.deleteDualBandApInfo(rcd.mApBSSID);
                    if (rcd.mServingBand.shortValue() == 1) {
                        this.mWifiProHistoryRecordManager.deleteRelateApInfo(rcd.mApBSSID);
                    } else {
                        this.mWifiProHistoryRecordManager.deleteRelate5GApInfo(rcd.mApBSSID);
                    }
                }
            }
        }
    }

    private boolean isValid(WifiConfiguration config) {
        if (config == null || config.allowedKeyManagement.cardinality() > 1) {
            return false;
        }
        return true;
    }

    public boolean isEnterpriseSecurity(int networkId) {
        List<WifiConfiguration> configs = WifiproUtils.getAllConfiguredNetworks();
        if (configs == null || configs.size() <= 0) {
            return false;
        }
        for (WifiConfiguration config : configs) {
            if (config != null && networkId == config.networkId) {
                return config.isEnterprise();
            }
        }
        return false;
    }

    public int getAuthType(int networkId) {
        List<WifiConfiguration> configs = WifiproUtils.getAllConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            return -1;
        }
        for (WifiConfiguration config : configs) {
            if (config != null && isValid(config) && networkId == config.networkId) {
                Log.i(HwDualBandMessageUtil.TAG, "getAuthType  networkId= " + networkId + " config.getAuthType() = " + config.getAuthType());
                return config.getAuthType();
            }
        }
        return -1;
    }

    public boolean saveApInfo(ScanResult result, int authType) {
        Log.e(HwDualBandMessageUtil.TAG, "saveAPInfo for scan result");
        String mCurrentBSSID = result.BSSID;
        if (mCurrentBSSID == null) {
            Log.e(HwDualBandMessageUtil.TAG, "mCurrentBSSID == null");
            return false;
        }
        WifiProDualBandApInfoRcd apInfo = new WifiProDualBandApInfoRcd(mCurrentBSSID);
        apInfo.mApBSSID = mCurrentBSSID;
        apInfo.mApSSID = "\"" + result.SSID + "\"";
        apInfo.mApAuthType = Short.valueOf((short) authType);
        apInfo.mChannelFrequency = result.frequency;
        apInfo.mInetCapability = 1;
        if (ScanResult.is5GHz(result.frequency)) {
            apInfo.mServingBand = 2;
        } else {
            apInfo.mServingBand = 1;
        }
        Log.i(HwDualBandMessageUtil.TAG, "ConnectedState apInfo.mApBSSID = " + WifiProCommonUtils.safeDisplayBssid(apInfo.mApBSSID) + "  apInfo.mApSSID = " + StringUtilEx.safeDisplaySsid(apInfo.mApSSID) + " apInfo.mChannelFrequency = " + apInfo.mChannelFrequency + " apInfo.mServingBand = " + apInfo.mServingBand);
        this.mWifiProHistoryRecordManager.saveDualBandApInfo(apInfo);
        return true;
    }

    public boolean isHaveMultipleAP(String bssid, String ssid, int type) {
        return this.mWifiProHistoryRecordManager.isHaveMultipleAP(bssid, ssid, type);
    }
}
