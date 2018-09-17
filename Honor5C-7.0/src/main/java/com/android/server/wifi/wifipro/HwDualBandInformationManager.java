package com.android.server.wifi.wifipro;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class HwDualBandInformationManager {
    private static HwDualBandInformationManager mHwDualBandInformationManager;
    private Context mContext;
    private WifiManager mWifiManager;
    private WifiProHistoryRecordManager mWifiProHistoryRecordManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.HwDualBandInformationManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.HwDualBandInformationManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.HwDualBandInformationManager.<clinit>():void");
    }

    public static HwDualBandInformationManager createInstance(Context context) {
        if (mHwDualBandInformationManager == null) {
            mHwDualBandInformationManager = new HwDualBandInformationManager(context);
        }
        return mHwDualBandInformationManager;
    }

    public static HwDualBandInformationManager getInstance() {
        return mHwDualBandInformationManager;
    }

    private HwDualBandInformationManager(Context context) {
        this.mWifiManager = null;
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(this.mContext, this.mWifiManager);
    }

    public void updateAPInfo(WifiProDualBandApInfoRcd mApInfo) {
        Log.e(HwDualBandMessageUtil.TAG, "updateAPInfo update AP info ssid = " + mApInfo.mApSSID);
        this.mWifiProHistoryRecordManager.saveDualBandApInfo(mApInfo);
    }

    public boolean saveAPInfo() {
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
        apInfo.apBSSID = mCurrentBSSID;
        apInfo.mApSSID = mWifiInfo.getSSID();
        apInfo.mApAuthType = Short.valueOf((short) getAuthType(mWifiInfo.getNetworkId()));
        apInfo.mChannelFrequency = mWifiInfo.getFrequency();
        apInfo.mInetCapability = Short.valueOf((short) 1);
        apInfo.isInBlackList = 0;
        if (ScanResult.is5GHz(mWifiInfo.getFrequency())) {
            apInfo.mServingBand = Short.valueOf((short) 2);
        } else {
            apInfo.mServingBand = Short.valueOf((short) 1);
        }
        Log.e(HwDualBandMessageUtil.TAG, "ConnectedState apInfo.apBSSID = " + apInfo.apBSSID + "  apInfo.mApSSID = " + apInfo.mApSSID + " apInfo.mChannelFrequency = " + apInfo.mChannelFrequency + " apInfo.mServingBand = " + apInfo.mServingBand);
        this.mWifiProHistoryRecordManager.saveDualBandApInfo(apInfo);
        return true;
    }

    public WifiProDualBandApInfoRcd getDualBandAPInfo(String bssid) {
        List<WifiProRelateApRcd> relateApList = new ArrayList();
        WifiProDualBandApInfoRcd result = this.mWifiProHistoryRecordManager.getDualBandApRecord(bssid);
        if (result != null) {
            this.mWifiProHistoryRecordManager.getRelateApList(bssid, relateApList);
            result.setRelateApRcds(relateApList);
        } else {
            Log.e(HwDualBandMessageUtil.TAG, "getDualBandAPInfo return null");
        }
        return result;
    }

    public void delectDualBandAPInfoBySsid(String ssid, int authtype) {
        Log.e(HwDualBandMessageUtil.TAG, "delectDualBandAPInfoBySsid ssid= " + ssid);
        List<WifiProDualBandApInfoRcd> RcdList = this.mWifiProHistoryRecordManager.getDualBandApInfoBySsid(ssid);
        if (RcdList != null && RcdList.size() != 0) {
            Log.e(HwDualBandMessageUtil.TAG, "delectDualBandAPInfoBySsid  RcdList.size() = " + RcdList.size());
            for (WifiProDualBandApInfoRcd rcd : RcdList) {
                if (rcd.mApAuthType.shortValue() == authtype) {
                    Log.e(HwDualBandMessageUtil.TAG, "delectDualBandAPInfoBySsid  rcd.mApSSID = " + rcd.mApSSID + " rcd.mServingBand = " + rcd.mServingBand);
                    this.mWifiProHistoryRecordManager.deleteDualBandApInfo(rcd.apBSSID);
                    if (rcd.mServingBand.shortValue() == (short) 1) {
                        this.mWifiProHistoryRecordManager.deleteRelateApInfo(rcd.apBSSID);
                    } else {
                        this.mWifiProHistoryRecordManager.deleteRelate5GApInfo(rcd.apBSSID);
                    }
                }
            }
        }
    }

    public boolean isEnterpriseAP(String bssid) {
        WifiProApInfoRecord record = this.mWifiProHistoryRecordManager.getApInfoRecord(bssid);
        if (record == null) {
            Log.e(HwDualBandMessageUtil.TAG, "record == null");
            return false;
        }
        Log.e(HwDualBandMessageUtil.TAG, "record.isEnterpriseAP = " + record.isEnterpriseAP);
        if (record.isEnterpriseAP) {
            return true;
        }
        return false;
    }

    private boolean isValid(WifiConfiguration config) {
        boolean z = true;
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            z = false;
        }
        return z;
    }

    public int getAuthType(int networkId) {
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            return -1;
        }
        for (WifiConfiguration config : configs) {
            if (config != null && isValid(config) && networkId == config.networkId) {
                Log.d(HwDualBandMessageUtil.TAG, "getAuthType  networkId= " + networkId + " config.getAuthType() = " + config.getAuthType());
                return config.getAuthType();
            }
        }
        return -1;
    }

    public boolean saveAPInfo(ScanResult result, int authType) {
        Log.e(HwDualBandMessageUtil.TAG, "saveAPInfo for scan result");
        String mCurrentBSSID = result.BSSID;
        if (mCurrentBSSID == null) {
            Log.e(HwDualBandMessageUtil.TAG, "mCurrentBSSID == null");
            return false;
        }
        WifiProDualBandApInfoRcd apInfo = new WifiProDualBandApInfoRcd(mCurrentBSSID);
        apInfo.apBSSID = mCurrentBSSID;
        apInfo.mApSSID = "\"" + result.SSID + "\"";
        apInfo.mApAuthType = Short.valueOf((short) authType);
        apInfo.mChannelFrequency = result.frequency;
        apInfo.mInetCapability = Short.valueOf((short) 1);
        if (ScanResult.is5GHz(result.frequency)) {
            apInfo.mServingBand = Short.valueOf((short) 2);
        } else {
            apInfo.mServingBand = Short.valueOf((short) 1);
        }
        Log.e(HwDualBandMessageUtil.TAG, "ConnectedState apInfo.apBSSID = " + apInfo.apBSSID + "  apInfo.mApSSID = " + apInfo.mApSSID + " apInfo.mChannelFrequency = " + apInfo.mChannelFrequency + " apInfo.mServingBand = " + apInfo.mServingBand);
        this.mWifiProHistoryRecordManager.saveDualBandApInfo(apInfo);
        return true;
    }

    public boolean isHaveMultipleAP(String bssid, String ssid, int type) {
        return this.mWifiProHistoryRecordManager.isHaveMultipleAP(bssid, ssid, type);
    }
}
