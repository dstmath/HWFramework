package com.android.server.wifi.wifipro;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class WiFiProEvaluateController {
    private static final int BACK_EVALUATE_RSSI_CHANGE_VALIDITY = 20;
    private static final int BACK_EVALUATE_TIME_VALIDITY = 14400000;
    private static final String INVAILD_SSID = "<unknown ssid>";
    public static final int MAX_FAIL_COUNTER = 2;
    public static final int MIN_RSSI_LEVEL_EVALUATE_BACK = 3;
    public static final int MIN_RSSI_LEVEL_EVALUATE_SETTINGS = 2;
    private static final int SCORE_PROTECTION_DURATION = 60000;
    private static final int SETTINGS_EVALUATE_RSSI_CHANGE_VALIDITY = 20;
    private static final int SETTINGS_EVALUATE_TIME_VALIDITY = 1800000;
    private static final String TAG = "WiFi_PRO_EvaluateController";
    private static Map<String, WiFiProScoreInfo> mEvaluateAPHashMap;
    private Context mContext;
    private boolean mIsWiFiProEvaluateEnabled;
    private List<WiFiProScoreInfo> mOpenApList;
    private List<WiFiProScoreInfo> mSavedApList;
    private Map<String, ScanResult> mScanResultHashMap;
    private Queue<String> mUnEvaluateAPQueue;
    private List<ScanResult> mUnRepetitionScanResultList;
    private List<String> mUntrustedOpenApList;
    private WifiManager mWifiManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.WiFiProEvaluateController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.WiFiProEvaluateController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WiFiProEvaluateController.<clinit>():void");
    }

    public WiFiProEvaluateController(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    public static boolean isEvaluateRecordsEmpty() {
        if (mEvaluateAPHashMap == null || mEvaluateAPHashMap.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isUnEvaluateAPRecordsEmpty() {
        if (this.mUnEvaluateAPQueue == null || this.mUnEvaluateAPQueue.isEmpty()) {
            return true;
        }
        return false;
    }

    public void wiFiProEvaluateEnable(boolean enable) {
        this.mIsWiFiProEvaluateEnabled = enable;
        Log.d(TAG, "mIsWiFiProEvaluateEnabled = " + enable);
    }

    public static void evaluateAPHashMapDump() {
        if (mEvaluateAPHashMap != null && !mEvaluateAPHashMap.isEmpty()) {
            Log.d(TAG, "mEvaluateAPHashMap size =" + mEvaluateAPHashMap.size());
        }
    }

    public void unEvaluateAPQueueDump() {
        if (this.mUnEvaluateAPQueue == null || this.mUnEvaluateAPQueue.isEmpty()) {
            Log.d(TAG, "null == mUnEvaluateAPQueue || mUnEvaluateAPQueue.isEmpty()");
            return;
        }
        Log.d(TAG, "mUnEvaluateAPQueue size =" + this.mUnEvaluateAPQueue.size());
        for (String ssid : this.mUnEvaluateAPQueue) {
            Log.d(TAG, "mUnEvaluateAPQueue ssid =" + ssid);
        }
    }

    public boolean isWiFiProEvaluateEnable() {
        return this.mIsWiFiProEvaluateEnabled;
    }

    public boolean isLastEvaluateValid(WifiInfo wifiinfo, int evalate_type) {
        if (wifiinfo == null || TextUtils.isEmpty(wifiinfo.getSSID())) {
            return false;
        }
        String ssid = wifiinfo.getSSID();
        if (mEvaluateAPHashMap.containsKey(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = (WiFiProScoreInfo) mEvaluateAPHashMap.get(ssid);
            if (wiFiProScoreInfo == null || wiFiProScoreInfo.internetAccessType == 0 || wiFiProScoreInfo.networkQosLevel == 0) {
                return false;
            }
            if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime >= 1800000) {
                Log.d(TAG, ssid + ",crurrent rssi =  " + wifiinfo.getRssi() + ", last  rssi = " + wiFiProScoreInfo.rssi + ",interval  time = " + ((System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime) / 1000) + "s, last evaluate is NOT Valid");
            } else if (calculateSignalLevelHW(wiFiProScoreInfo.rssi) == calculateSignalLevelHW(wifiinfo.getRssi())) {
                Log.d(TAG, ssid + ",crurrent Signal =  " + calculateSignalLevelHW(wifiinfo.getRssi()) + ", last  Signal = " + calculateSignalLevelHW(wiFiProScoreInfo.rssi) + ", isSemiAuto, last evaluate is Valid");
                return true;
            } else if (Math.abs(wifiinfo.getRssi() - wiFiProScoreInfo.rssi) < SETTINGS_EVALUATE_RSSI_CHANGE_VALIDITY) {
                Log.d(TAG, ssid + ",crurrent rssi =  " + wifiinfo.getRssi() + ", last  rssi = " + wiFiProScoreInfo.rssi + ", isSemiAuto, last evaluate is Valid");
                return true;
            } else {
                Log.d(TAG, ssid + ",Signal level change,and rssi >20");
            }
        }
        return false;
    }

    public boolean isLastEvaluateValid(ScanResult scanResult, int evalate_type) {
        if (scanResult == null || TextUtils.isEmpty(scanResult.SSID)) {
            return true;
        }
        if (isEvaluateRecordsEmpty()) {
            return false;
        }
        String ssid = "\"" + scanResult.SSID + "\"";
        if (!mEvaluateAPHashMap.containsKey(ssid)) {
            return false;
        }
        WiFiProScoreInfo wiFiProScoreInfo = (WiFiProScoreInfo) mEvaluateAPHashMap.get(ssid);
        if (wiFiProScoreInfo == null || wiFiProScoreInfo.internetAccessType == 0) {
            return false;
        }
        if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime < 60000) {
            Log.d(TAG, ssid + ", evaluate  protection duration, is valid");
            return true;
        }
        if (evalate_type == 1) {
            if (wiFiProScoreInfo.internetAccessType == 4 && wiFiProScoreInfo.networkQosLevel == 0) {
                return false;
            }
            if (wiFiProScoreInfo.internetAccessType == 1) {
                if (wiFiProScoreInfo.failCounter < MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
                    Log.d(TAG, ssid + ", is network congestion, activity is Settings, failCounter = " + wiFiProScoreInfo.failCounter);
                    return false;
                } else if (isEvaluateConditionChange(scanResult, evalate_type)) {
                    reSetEvaluateRecord(ssid);
                    return true;
                }
            }
        } else if (wiFiProScoreInfo.internetAccessType == MIN_RSSI_LEVEL_EVALUATE_BACK || wiFiProScoreInfo.internetAccessType == MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
            return true;
        } else {
            if (wiFiProScoreInfo.internetAccessType == 4) {
                if (isEvaluateConditionChange(scanResult, evalate_type)) {
                    updateScoreInfoLevel(ssid, 0);
                }
                return true;
            } else if (wiFiProScoreInfo.internetAccessType == 1) {
                if (wiFiProScoreInfo.failCounter >= MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
                    return true;
                }
                Log.d(TAG, ssid + ", is network congestion, activity is Not settings, failCounter = " + wiFiProScoreInfo.failCounter);
                return false;
            }
        }
        if (isEvaluateConditionChange(scanResult, evalate_type)) {
            Log.d(TAG, ssid + ",last evaluate is NOT valid ");
            return false;
        }
        Log.d(TAG, ssid + ",last evaluate is valid ");
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isEvaluateConditionChange(ScanResult scanResult, int evalate_type) {
        if (scanResult == null || TextUtils.isEmpty(scanResult.SSID) || isEvaluateRecordsEmpty()) {
            return false;
        }
        String ssid = "\"" + scanResult.SSID + "\"";
        if (mEvaluateAPHashMap.containsKey(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = (WiFiProScoreInfo) mEvaluateAPHashMap.get(ssid);
            if (wiFiProScoreInfo == null) {
                return false;
            }
            long valid_time = 0;
            int valid_rssi_change = 0;
            if (evalate_type == 0) {
                valid_time = 14400000;
                valid_rssi_change = SETTINGS_EVALUATE_RSSI_CHANGE_VALIDITY;
            } else if (evalate_type == 1) {
                valid_time = 1800000;
                valid_rssi_change = SETTINGS_EVALUATE_RSSI_CHANGE_VALIDITY;
            } else if (evalate_type == MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
                valid_time = 1800000;
                valid_rssi_change = SETTINGS_EVALUATE_RSSI_CHANGE_VALIDITY;
            }
            if (wiFiProScoreInfo.internetAccessType == MIN_RSSI_LEVEL_EVALUATE_SETTINGS || wiFiProScoreInfo.internetAccessType == MIN_RSSI_LEVEL_EVALUATE_BACK) {
                if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime < 12 * valid_time) {
                    return false;
                }
                Log.d(TAG, ssid + ",last evaluate is  PORTA or no intenet ,  is timeout, last evaluate date: " + WifiproUtils.formatTime(wiFiProScoreInfo.lastScoreTime));
                return true;
            } else if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime > valid_time) {
                Log.d(TAG, ssid + ",last evaluate is NOT Valid,last evaluate date: " + WifiproUtils.formatTime(wiFiProScoreInfo.lastScoreTime));
                return true;
            } else if (calculateSignalLevelHW(wiFiProScoreInfo.rssi) != calculateSignalLevelHW(scanResult.level) && Math.abs(scanResult.level - wiFiProScoreInfo.rssi) > valid_rssi_change) {
                Log.d(TAG, ssid + ",last evaluate is NOT Valid,SignalLevel change, and rssi change >20, last rssi=" + wiFiProScoreInfo.rssi + ", now rssi = " + scanResult.level);
                return true;
            } else if (Math.abs(calculateSignalLevelHW(wiFiProScoreInfo.rssi) - calculateSignalLevelHW(scanResult.level)) > MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
                return true;
            }
        }
        return false;
    }

    public WiFiProScoreInfo getCurrentWiFiProScoreInfo(String ssid) {
        if (!isEvaluateRecordsEmpty() && !TextUtils.isEmpty(ssid)) {
            return (WiFiProScoreInfo) mEvaluateAPHashMap.get(ssid);
        }
        Log.w(TAG, "getCurrentWifiProperties is null!");
        return null;
    }

    public static WiFiProScoreInfo getCurrentWiFiProScore(String ssid) {
        if (!isEvaluateRecordsEmpty() && !TextUtils.isEmpty(ssid)) {
            return (WiFiProScoreInfo) mEvaluateAPHashMap.get(ssid);
        }
        Log.w(TAG, "getCurrentWifiProperties is null!");
        return null;
    }

    public boolean isAbandonEvaluate(String ssid) {
        WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
        if (wiFiProScoreInfo == null) {
            return true;
        }
        if (calculateSignalLevelHW(wiFiProScoreInfo.rssi) >= MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
            return false;
        }
        Log.d(TAG, ssid + " rssi = " + wiFiProScoreInfo.rssi);
        return true;
    }

    public void cleanEvaluateRecords() {
        if (!(this.mUnEvaluateAPQueue == null || this.mUnEvaluateAPQueue.isEmpty())) {
            Log.d(TAG, "clean mUnEvaluateAPQueue");
            this.mUnEvaluateAPQueue.clear();
        }
        cleanEvaluateCacheRecords();
    }

    private void initEvaluateRecords() {
        if (mEvaluateAPHashMap == null) {
            mEvaluateAPHashMap = new HashMap();
        }
        if (this.mUnEvaluateAPQueue == null) {
            this.mUnEvaluateAPQueue = new LinkedList();
        }
        if (this.mUntrustedOpenApList == null) {
            this.mUntrustedOpenApList = new ArrayList();
        }
    }

    private void initEvaluateCacheRecords() {
        if (this.mSavedApList == null) {
            this.mSavedApList = new ArrayList();
        }
        if (this.mOpenApList == null) {
            this.mOpenApList = new ArrayList();
        }
    }

    public void cleanEvaluateCacheRecords() {
        if (this.mSavedApList != null) {
            this.mSavedApList.clear();
        }
        if (this.mOpenApList != null) {
            this.mOpenApList.clear();
        }
    }

    public boolean isSaveAP(ScanResult scanResult) {
        if (scanResult == null || getWifiConfiguration("\"" + scanResult.SSID + "\"") == null) {
            return false;
        }
        return true;
    }

    public boolean isAllowEvaluate(ScanResult scanResult, int evalate_type) {
        if (scanResult == null) {
            return false;
        }
        int level = MIN_RSSI_LEVEL_EVALUATE_BACK;
        if (evalate_type == 1) {
            level = MIN_RSSI_LEVEL_EVALUATE_SETTINGS;
        }
        return calculateSignalLevelHW(scanResult.level) >= level && getWifiConfiguration("\"" + scanResult.SSID + "\"") == null && isOpenAP(scanResult);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isOpenAP(ScanResult result) {
        if (result.capabilities.contains("WEP") || result.capabilities.contains("WAPI-PSK") || result.capabilities.contains("QUALCOMM-WAPI-PSK") || result.capabilities.contains("WAPI-CERT") || result.capabilities.contains("QUALCOMM-WAPI-CERT") || result.capabilities.contains("PSK") || result.capabilities.contains("EAP")) {
            return false;
        }
        return true;
    }

    public void initWifiProEvaluateRecords() {
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configNetworks) {
                if (!(wifiConfiguration.SSID == null || mEvaluateAPHashMap.containsKey(wifiConfiguration.SSID))) {
                    WiFiProScoreInfo info = new WiFiProScoreInfo();
                    info.ssid = wifiConfiguration.SSID;
                    if (wifiConfiguration.noInternetAccess) {
                        info.internetAccessType = MIN_RSSI_LEVEL_EVALUATE_SETTINGS;
                        mEvaluateAPHashMap.put(wifiConfiguration.SSID, info);
                    } else if (wifiConfiguration.portalNetwork) {
                        info.internetAccessType = MIN_RSSI_LEVEL_EVALUATE_BACK;
                        mEvaluateAPHashMap.put(wifiConfiguration.SSID, info);
                    } else if (!wifiConfiguration.wifiProNoInternetAccess) {
                        info.internetAccessType = 4;
                        mEvaluateAPHashMap.put(wifiConfiguration.SSID, info);
                    }
                }
            }
            return;
        }
        Log.d(TAG, "configNetworks  == null ");
    }

    public boolean isAllowAutoEvaluate(List<ScanResult> ScanResultList) {
        if (ScanResultList == null || ScanResultList.isEmpty()) {
            return false;
        }
        for (ScanResult scanResult : ScanResultList) {
            String ssid = "\"" + scanResult.SSID + "\"";
            WifiConfiguration cfg = getWifiConfiguration(ssid);
            if (WifiProCommonUtils.isWifiSelfCuring() || (cfg != null && ((!cfg.noInternetAccess || NetworkHistoryUtils.allowWifiConfigRecovery(cfg.internetHistory)) && !cfg.isTempCreated))) {
                Log.d(TAG, scanResult.SSID + " , has save, not allow Evaluate************ ");
                return false;
            } else if (WifiProCommonUtils.allowRecheckForNoInternet(cfg, scanResult, this.mContext)) {
                Log.d(TAG, "don't allow to auto evaluating because of background recheck for no internet, candidate = " + ssid);
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reSetNotAllowEvaluateRecord(ScanResult scanResult) {
        if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID) && !isEvaluateRecordsEmpty()) {
            String ssid = "\"" + scanResult.SSID + "\"";
            if (updateScoreInfoLevel(ssid, 0)) {
                Log.d(TAG, "reset ssid :" + ssid);
            }
        }
    }

    public synchronized void addEvaluateRecords(WifiInfo wifiInfo, int evalate_type) {
        initEvaluateRecords();
        if (!(wifiInfo == null || TextUtils.isEmpty(wifiInfo.getSSID()))) {
            String ssid = wifiInfo.getSSID();
            if (!INVAILD_SSID.equals(ssid)) {
                WiFiProScoreInfo wiFiProScoreInfo;
                if (!mEvaluateAPHashMap.containsKey(ssid)) {
                    Log.d(TAG, "new wiFiProScoreInfo wifiinfo ssid :" + ssid);
                    wiFiProScoreInfo = new WiFiProScoreInfo(wifiInfo);
                } else if (!isLastEvaluateValid(wifiInfo, evalate_type)) {
                    Log.d(TAG, "update  wiFiProScoreInfo wifiinfo ssid :" + ssid);
                    wiFiProScoreInfo = (WiFiProScoreInfo) mEvaluateAPHashMap.get(ssid);
                    wiFiProScoreInfo.trusted = true;
                    wiFiProScoreInfo.evaluated = false;
                    wiFiProScoreInfo.invalid = true;
                    wiFiProScoreInfo.is5GHz = wifiInfo.is5GHz();
                    wiFiProScoreInfo.rssi = wifiInfo.getRssi();
                    wiFiProScoreInfo.lastUpdateTime = System.currentTimeMillis();
                } else {
                    return;
                }
                mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
            }
        }
    }

    public synchronized void addEvaluateRecords(ScanResult scanResult, int evalate_type) {
        boolean z = false;
        synchronized (this) {
            initEvaluateRecords();
            initEvaluateCacheRecords();
            if (!(scanResult == null || TextUtils.isEmpty(scanResult.SSID))) {
                String ssid = "\"" + scanResult.SSID + "\"";
                if (INVAILD_SSID.equals(scanResult.SSID)) {
                    return;
                }
                WiFiProScoreInfo wiFiProScoreInfo;
                if (mEvaluateAPHashMap.containsKey(ssid)) {
                    wiFiProScoreInfo = (WiFiProScoreInfo) mEvaluateAPHashMap.get(ssid);
                    if (System.currentTimeMillis() - wiFiProScoreInfo.lastUpdateTime < 2000 && scanResult.level < wiFiProScoreInfo.rssi) {
                        return;
                    } else if (isLastEvaluateValid(scanResult, evalate_type)) {
                        return;
                    } else {
                        if (!scanResult.untrusted) {
                            z = true;
                        }
                        wiFiProScoreInfo.trusted = z;
                        wiFiProScoreInfo.evaluated = false;
                        wiFiProScoreInfo.invalid = true;
                        wiFiProScoreInfo.rssi = scanResult.level;
                        wiFiProScoreInfo.is5GHz = scanResult.is5GHz();
                        wiFiProScoreInfo.lastUpdateTime = System.currentTimeMillis();
                        Log.i(TAG, "update  wiFiProScoreInfo ScanResult ssid :" + ssid + ", rssi = " + scanResult.level);
                    }
                } else {
                    Log.d(TAG, "add  wiFiProScoreInfo ScanResult ssid :" + ssid);
                    wiFiProScoreInfo = new WiFiProScoreInfo(scanResult);
                }
                mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                if (isSaveAP(scanResult)) {
                    Log.d(TAG, ssid + " is saved ap");
                    this.mSavedApList.add(wiFiProScoreInfo);
                } else {
                    Log.d(TAG, ssid + " is open ap");
                    this.mOpenApList.add(wiFiProScoreInfo);
                }
            }
        }
    }

    public List<ScanResult> scanResultListFilter(List<ScanResult> scanResultList) {
        if (scanResultList == null) {
            return null;
        }
        this.mUnRepetitionScanResultList = new ArrayList();
        this.mScanResultHashMap = new HashMap();
        for (ScanResult scanResult : scanResultList) {
            if (!TextUtils.isEmpty(scanResult.SSID)) {
                if (this.mScanResultHashMap.containsKey(scanResult.SSID)) {
                    ScanResult scan_result = (ScanResult) this.mScanResultHashMap.get(scanResult.SSID);
                    if (scan_result != null && scan_result.level < scanResult.level) {
                        this.mScanResultHashMap.put(scanResult.SSID, scanResult);
                    }
                } else {
                    this.mScanResultHashMap.put(scanResult.SSID, scanResult);
                }
            }
        }
        Iterator it = this.mScanResultHashMap.values().iterator();
        if (it != null) {
            while (it.hasNext()) {
                this.mUnRepetitionScanResultList.add((ScanResult) it.next());
            }
        }
        return this.mUnRepetitionScanResultList;
    }

    public void orderByRssi() {
        if (!(this.mSavedApList == null || this.mSavedApList.isEmpty())) {
            Collections.sort(this.mSavedApList);
            Collections.reverse(this.mSavedApList);
            try {
                for (WiFiProScoreInfo saveAPInfo : this.mSavedApList) {
                    if (!this.mUnEvaluateAPQueue.contains(saveAPInfo.ssid) && this.mUnEvaluateAPQueue.offer(saveAPInfo.ssid)) {
                        Log.d(TAG, saveAPInfo.ssid + " offer to Queue: " + " rssi : " + saveAPInfo.rssi);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
            } finally {
                this.mSavedApList.clear();
            }
        }
        if (this.mOpenApList != null && !this.mOpenApList.isEmpty()) {
            Collections.sort(this.mOpenApList);
            Collections.reverse(this.mOpenApList);
            try {
                for (WiFiProScoreInfo openAPInfo : this.mOpenApList) {
                    if (!this.mUnEvaluateAPQueue.contains(openAPInfo.ssid) && this.mUnEvaluateAPQueue.offer(openAPInfo.ssid)) {
                        Log.d(TAG, openAPInfo.ssid + " offer to Queue: " + " rssi : " + openAPInfo.rssi);
                    }
                }
            } catch (Exception e2) {
                Log.w(TAG, e2.getMessage());
            } finally {
                this.mOpenApList.clear();
            }
        }
    }

    public synchronized void updateEvaluateRecords(List<ScanResult> scanResultList, int evaluate_type, String currentSsid) {
        if (scanResultList != null) {
            if (!scanResultList.isEmpty()) {
                initEvaluateRecords();
                for (ScanResult scanResult : scanResultList) {
                    if (!(scanResult == null || TextUtils.isEmpty(scanResult.SSID))) {
                        String ssid = "\"" + scanResult.SSID + "\"";
                        if ((TextUtils.isEmpty(currentSsid) || !currentSsid.equals(ssid)) && mEvaluateAPHashMap.containsKey(ssid) && !isLastEvaluateValid(scanResult, evaluate_type)) {
                            updateScoreInfoLevel(ssid, 0);
                        }
                    }
                }
            }
        }
    }

    public boolean updateEvaluateRecords(String ssid, WiFiProScoreInfo wifiScoreProperties) {
        if (TextUtils.isEmpty(ssid) || isEvaluateRecordsEmpty()) {
            Log.w(TAG, "updateEvaluateRecords fail!");
            return false;
        }
        mEvaluateAPHashMap.put(ssid, wifiScoreProperties);
        return true;
    }

    public synchronized boolean isAccessAPOutOfRange(List<ScanResult> scanResultList) {
        if (scanResultList == null) {
            Log.w(TAG, "scanResultList is null, Access AP uut of range");
            return true;
        }
        for (ScanResult scanResult : scanResultList) {
            if (scanResult != null && isOpenAP(scanResult) && scanResult.internetAccessType == 4) {
                return false;
            }
        }
        Log.w(TAG, "scanResultList internetAccessType is not normal, Access AP out of range");
        return true;
    }

    public synchronized void increaseFailCounter(String ssid) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (wiFiProScoreInfo != null) {
                wiFiProScoreInfo.failCounter++;
                wiFiProScoreInfo.lastScoreTime = System.currentTimeMillis();
                mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
            }
        }
    }

    public synchronized boolean updateScoreInfoType(String ssid, int internetAccessType) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (wiFiProScoreInfo != null) {
                if (wiFiProScoreInfo.internetAccessType != internetAccessType) {
                    wiFiProScoreInfo.internetAccessType = internetAccessType;
                    if (internetAccessType != 1) {
                        wiFiProScoreInfo.failCounter = 0;
                    }
                    wiFiProScoreInfo.networkQosLevel = 0;
                    wiFiProScoreInfo.invalid = false;
                    wiFiProScoreInfo.evaluated = true;
                    wiFiProScoreInfo.abandon = false;
                    wiFiProScoreInfo.lastScoreTime = System.currentTimeMillis();
                    mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                    Log.d(TAG, "updateEvaluateRecords internetAccessType succeed! new type =  " + internetAccessType);
                    return true;
                }
                Log.d(TAG, "internetAccessType not change, can not update");
            }
        }
        return false;
    }

    public synchronized boolean updateScoreInfoLevel(String ssid, int networkQosLevel) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (wiFiProScoreInfo != null) {
                if (wiFiProScoreInfo.internetAccessType != 4) {
                    wiFiProScoreInfo.networkQosLevel = 0;
                } else {
                    wiFiProScoreInfo.networkQosLevel = networkQosLevel;
                }
                wiFiProScoreInfo.networkQosScore = WiFiProScoreInfo.calculateWiFiScore(wiFiProScoreInfo);
                wiFiProScoreInfo.invalid = false;
                wiFiProScoreInfo.evaluated = true;
                wiFiProScoreInfo.abandon = false;
                wiFiProScoreInfo.lastScoreTime = System.currentTimeMillis();
                mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                Log.d(TAG, "updateEvaluate Level  succeed!.new Level = " + wiFiProScoreInfo.networkQosLevel);
                return true;
            }
        }
        return false;
    }

    public void updateWifiSecurityInfo(String ssid, int secStatus) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo scoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (scoreInfo != null) {
                scoreInfo.networkSecurity = secStatus;
            }
        }
    }

    public int getWifiSecurityInfo(String ssid) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo scoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (scoreInfo != null) {
                return scoreInfo.networkSecurity;
            }
        }
        return -1;
    }

    public synchronized boolean updateScoreInfoAbandon(String ssid, boolean abandon) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (!(wiFiProScoreInfo == null || wiFiProScoreInfo.abandon == abandon)) {
                wiFiProScoreInfo.abandon = abandon;
                if (abandon) {
                    wiFiProScoreInfo.networkQosLevel = 0;
                    wiFiProScoreInfo.internetAccessType = 0;
                }
                wiFiProScoreInfo.invalid = false;
                mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                Log.d(TAG, "updateEvaluateRecords abandon succeed!");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean updateWifiProbeMode(String ssid, int mode) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (wiFiProScoreInfo != null) {
                wiFiProScoreInfo.probeMode = mode;
                Log.d(TAG, "*****updateWifiProbeMode, new probeMode  " + mode);
                mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean updateScoreEvaluateStatus(String ssid, boolean evaluate) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (wiFiProScoreInfo != null) {
                wiFiProScoreInfo.evaluated = evaluate;
                wiFiProScoreInfo.invalid = false;
                wiFiProScoreInfo.lastScoreTime = System.currentTimeMillis();
                wiFiProScoreInfo.abandon = false;
                mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean updateScoreEvaluateInvalid(String ssid, boolean invalid) {
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (wiFiProScoreInfo != null && wiFiProScoreInfo.evaluated) {
                wiFiProScoreInfo.invalid = invalid;
                if (invalid) {
                    wiFiProScoreInfo.evaluated = false;
                }
                mEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                Log.d(TAG, "updateEvaluate Invalid succeed!, wiFiProScoreInfo " + wiFiProScoreInfo.dump());
                return true;
            }
        }
        return false;
    }

    public synchronized void reSetEvaluateRecord(String ssid) {
        if (!TextUtils.isEmpty(ssid)) {
            if (mEvaluateAPHashMap != null && mEvaluateAPHashMap.containsKey(ssid)) {
                try {
                    Log.d(TAG, " remove " + ssid + " form EvaluateRecord");
                    mEvaluateAPHashMap.remove(ssid);
                } catch (UnsupportedOperationException e) {
                    Log.w(TAG, " unsupportedOperationException ");
                }
            }
        }
    }

    public synchronized void reSetEvaluateRecord(Intent intent) {
        if (intent != null) {
            WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra(MessageUtil.EXTRA_WIFI_CONFIGURATION);
            int changereson = intent.getIntExtra(MessageUtil.EXTRA_CHANGE_REASON, -1);
            if (!(config == null || TextUtils.isEmpty(config.SSID) || changereson != 1)) {
                if (!config.isTempCreated) {
                    reSetEvaluateRecord(config.SSID);
                }
            }
        }
    }

    public synchronized void restoreEvaluateRecord(NetworkInfo info) {
        if (info != null) {
            if (info.getDetailedState() == DetailedState.DISCONNECTED) {
                String ssid = info.getExtraInfo();
                if (!TextUtils.isEmpty(ssid) && !INVAILD_SSID.equals(ssid)) {
                    WifiConfiguration config = getWifiConfiguration(ssid);
                    if (config != null && config.portalNetwork) {
                        WiFiProScoreInfo scoreInfo = getCurrentWiFiProScoreInfo(ssid);
                        if (!(scoreInfo == null || scoreInfo.internetAccessType == 0 || scoreInfo.internetAccessType == MIN_RSSI_LEVEL_EVALUATE_BACK)) {
                            Log.d(TAG, "restore to portal, ssid = " + ssid);
                            updateScoreInfoType(ssid, MIN_RSSI_LEVEL_EVALUATE_BACK);
                        }
                    }
                }
            }
        }
    }

    public String getNextEvaluateWiFiSSID() {
        if (this.mUnEvaluateAPQueue == null || this.mUnEvaluateAPQueue.isEmpty()) {
            return null;
        }
        return (String) this.mUnEvaluateAPQueue.poll();
    }

    public boolean connectWifi(String ssid) {
        WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
        if (wiFiProScoreInfo == null) {
            Log.d(TAG, ssid + "conenct  connect fail");
            evaluateAPHashMapDump();
            return false;
        }
        WifiConfiguration wifiConfiguration = getWifiConfiguration(ssid);
        if (wifiConfiguration == null) {
            if (wiFiProScoreInfo.trusted) {
                Log.d(TAG, ssid + " trusted : " + wiFiProScoreInfo.trusted);
            }
            Log.d(TAG, "conenct open ap, create a new confg");
            wifiConfiguration = createOpenWifiConfiguration(ssid);
        }
        this.mWifiManager.connect(wifiConfiguration, null);
        return true;
    }

    public void addUntrustedOpenApList(String ssid) {
        if (this.mUntrustedOpenApList != null && ssid != null && !this.mUntrustedOpenApList.contains(ssid)) {
            Log.d(TAG, "add: " + ssid + " to  UntrustedOpenApList ");
            this.mUntrustedOpenApList.add(ssid);
        }
    }

    public void clearUntrustedOpenApList() {
        if (this.mUntrustedOpenApList != null && !this.mUntrustedOpenApList.isEmpty()) {
            this.mUntrustedOpenApList.clear();
        }
    }

    public void forgetUntrustedOpenAp() {
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configNetworks) {
                if (wifiConfiguration.isTempCreated) {
                    Log.d(TAG, "isTempCreated, forget: " + wifiConfiguration.SSID + " config ");
                    this.mWifiManager.forget(wifiConfiguration.networkId, null);
                }
            }
            if (this.mUntrustedOpenApList != null && this.mUntrustedOpenApList.isEmpty()) {
                this.mUntrustedOpenApList.clear();
            }
        }
    }

    private WifiConfiguration createOpenWifiConfiguration(String ssid) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        config.allowedKeyManagement.set(0);
        config.isTempCreated = true;
        String oriSsid = getOriSsid(ssid);
        if (TextUtils.isEmpty(oriSsid)) {
            Log.d(TAG, "oriSsid is null");
        } else {
            Log.d(TAG, "ssid:" + ssid + ", oriSsid:" + oriSsid);
            config.oriSsid = oriSsid;
        }
        return config;
    }

    private String getOriSsid(String ssid) {
        if (TextUtils.isEmpty(ssid) || this.mWifiManager == null) {
            return null;
        }
        List<ScanResult> resultLists = this.mWifiManager.getScanResults();
        if (resultLists == null) {
            return null;
        }
        for (ScanResult scanResult : resultLists) {
            if (("\"" + scanResult.SSID + "\"").equals(ssid) && scanResult.wifiSsid != null) {
                return scanResult.wifiSsid.oriSsid;
            }
        }
        return null;
    }

    public WifiConfiguration getWifiConfiguration(String ssid) {
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configNetworks) {
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals(ssid)) {
                    return wifiConfiguration;
                }
            }
        } else {
            Log.d(TAG, "configNetworks  == null ");
        }
        return null;
    }

    public int calculateSignalLevelHW(int rssi) {
        return HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(rssi);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int calculateTestWiFiLevel(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "calculateTestWiFiLevel ssid == null ");
            return 0;
        } else if (!mEvaluateAPHashMap.containsKey(ssid)) {
            return 0;
        } else {
            WiFiProScoreInfo properties = (WiFiProScoreInfo) mEvaluateAPHashMap.get(ssid);
            if (properties == null || properties.abandon || MIN_RSSI_LEVEL_EVALUATE_SETTINGS == properties.internetAccessType || MIN_RSSI_LEVEL_EVALUATE_BACK == properties.internetAccessType) {
                return 0;
            }
            int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(properties.rssi);
            int boost_5G = 0;
            if (properties.is5GHz) {
                boost_5G = 1;
            }
            int level = newSignalLevel + boost_5G;
            if (level > MIN_RSSI_LEVEL_EVALUATE_BACK) {
                return MIN_RSSI_LEVEL_EVALUATE_BACK;
            }
            return level > 1 ? MIN_RSSI_LEVEL_EVALUATE_SETTINGS : 1;
        }
    }

    public synchronized int getOldNetworkType(String ssid) {
        int oldNetworkType;
        oldNetworkType = 0;
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (wiFiProScoreInfo != null) {
                Log.d(TAG, "probeMode: " + wiFiProScoreInfo.probeMode);
            }
            if (wiFiProScoreInfo != null && wiFiProScoreInfo.probeMode == 1) {
                oldNetworkType = wiFiProScoreInfo.internetAccessType;
            }
        }
        return oldNetworkType;
    }

    public synchronized int getNewNetworkType(int checkResult) {
        int newNetworkType;
        newNetworkType = 0;
        if (5 == checkResult) {
            newNetworkType = 4;
        } else if (6 == checkResult) {
            newNetworkType = MIN_RSSI_LEVEL_EVALUATE_BACK;
        } else if (-1 == checkResult) {
            newNetworkType = MIN_RSSI_LEVEL_EVALUATE_SETTINGS;
        }
        return newNetworkType;
    }

    public synchronized int getChrDiffType(int oldType, int newType) {
        int diffType = 0;
        if (oldType == 0 || oldType == newType) {
            return 0;
        }
        if (oldType == 4 && newType == MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
            diffType = 1;
        } else if (oldType == 4 && newType == MIN_RSSI_LEVEL_EVALUATE_BACK) {
            diffType = MIN_RSSI_LEVEL_EVALUATE_SETTINGS;
        } else if (oldType == MIN_RSSI_LEVEL_EVALUATE_SETTINGS && newType == 4) {
            diffType = MIN_RSSI_LEVEL_EVALUATE_BACK;
        } else if (oldType == MIN_RSSI_LEVEL_EVALUATE_SETTINGS && newType == MIN_RSSI_LEVEL_EVALUATE_BACK) {
            diffType = 4;
        } else if (oldType == MIN_RSSI_LEVEL_EVALUATE_BACK && newType == 4) {
            diffType = 5;
        } else if (oldType == MIN_RSSI_LEVEL_EVALUATE_BACK && newType == MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
            diffType = 6;
        } else if (oldType == 1 && newType == 4) {
            diffType = 7;
        } else if (oldType == 1 && newType == MIN_RSSI_LEVEL_EVALUATE_SETTINGS) {
            diffType = 8;
        } else if (oldType == 1 && newType == MIN_RSSI_LEVEL_EVALUATE_BACK) {
            diffType = 9;
        }
        return diffType;
    }
}
