package com.huawei.hwwifiproservice;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.hwUtil.StringUtilEx;
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
    private static final int CHR_TYPE_BG_AV_CN_NAV = 1;
    private static final int CHR_TYPE_BG_AV_CN_POT = 2;
    private static final int CHR_TYPE_BG_CGT_CN_AV = 7;
    private static final int CHR_TYPE_BG_CGT_CN_NAV = 8;
    private static final int CHR_TYPE_BG_CGT_CN_POT = 9;
    private static final int CHR_TYPE_BG_CN_UNKNOWN = 0;
    private static final int CHR_TYPE_BG_NAV_CN_AV = 3;
    private static final int CHR_TYPE_BG_NAV_CN_POT = 4;
    private static final int CHR_TYPE_BG_POT_CN_AV = 5;
    private static final int CHR_TYPE_BG_POT_CN_NAV = 6;
    public static final int FREQUENCY_FOR_2G = 2412;
    public static final int FREQUENCY_FOR_5G = 5180;
    private static final String INVAILD_SSID = "<unknown ssid>";
    private static final Object LOCK = new Object();
    public static final int MAX_FAIL_COUNTER = 2;
    public static final int MIN_RSSI_LEVEL_EVALUATE_BACK = 3;
    public static final int MIN_RSSI_LEVEL_EVALUATE_SETTINGS = 2;
    public static final int POWER_MAX_VALID_TIME = 12;
    public static final int SCAN_RESULT_PROTECTION_TIME = 2000;
    private static final int SCORE_PROTECTION_DURATION = 60000;
    private static final int SETTINGS_EVALUATE_RSSI_CHANGE_VALIDITY = 20;
    private static final int SETTINGS_EVALUATE_TIME_VALIDITY = 1800000;
    public static final int SIGNAL_LEVEL_ONE = 1;
    public static final int SIGNAL_LEVEL_THREE = 3;
    private static final String TAG = "WiFi_PRO_EvaluateController";
    private static WiFiProEvaluateController sWiFiProEvaluateController = null;
    private static Map<String, WiFiProScoreInfo> scoreEvaluateAPHashMap = new HashMap();
    private Context mContext;
    private boolean mIsWiFiProEvaluateEnabled;
    private List<WiFiProScoreInfo> mOpenApList;
    private List<WiFiProScoreInfo> mSavedApList;
    private Map<String, ScanResult> mScanResultHashMap;
    private Queue<String> mUnEvaluateAPQueue;
    private List<ScanResult> mUnRepetitionScanResultList;
    private List<String> mUntrustedOpenApList;
    private WifiManager mWifiManager;

    private WiFiProEvaluateController(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    public static WiFiProEvaluateController getInstance(Context context) {
        if (sWiFiProEvaluateController == null) {
            synchronized (LOCK) {
                if (sWiFiProEvaluateController == null) {
                    sWiFiProEvaluateController = new WiFiProEvaluateController(context);
                }
            }
        }
        return sWiFiProEvaluateController;
    }

    public static boolean isEvaluateRecordsEmpty() {
        Map<String, WiFiProScoreInfo> map = scoreEvaluateAPHashMap;
        if (map == null || map.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isUnEvaluateAPRecordsEmpty() {
        Queue<String> queue = this.mUnEvaluateAPQueue;
        if (queue == null || queue.isEmpty()) {
            return true;
        }
        return false;
    }

    public void wiFiProEvaluateEnable(boolean enable) {
        this.mIsWiFiProEvaluateEnabled = enable;
        Log.i(TAG, "mIsWiFiProEvaluateEnabled = " + enable);
    }

    public static void evaluateAPHashMapDump() {
        Map<String, WiFiProScoreInfo> map = scoreEvaluateAPHashMap;
        if (map != null && !map.isEmpty()) {
            Log.d(TAG, "scoreEvaluateAPHashMap size =" + scoreEvaluateAPHashMap.size());
        }
    }

    public void unEvaluateAPQueueDump() {
        Queue<String> queue = this.mUnEvaluateAPQueue;
        if (queue == null || queue.isEmpty()) {
            Log.d(TAG, "null == mUnEvaluateAPQueue || mUnEvaluateAPQueue.isEmpty()");
            return;
        }
        Log.d(TAG, "mUnEvaluateAPQueue size =" + this.mUnEvaluateAPQueue.size());
        Iterator<String> it = this.mUnEvaluateAPQueue.iterator();
        while (it.hasNext()) {
            Log.i(TAG, "mUnEvaluateAPQueue ssid =" + StringUtilEx.safeDisplaySsid(it.next()));
        }
    }

    public boolean isWiFiProEvaluateEnable() {
        return this.mIsWiFiProEvaluateEnabled;
    }

    public boolean isLastEvaluateValid(WifiInfo wifiInfo, int evalateType) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getSSID())) {
            return false;
        }
        String ssid = wifiInfo.getSSID();
        if (!scoreEvaluateAPHashMap.containsKey(ssid) || (wiFiProScoreInfo = scoreEvaluateAPHashMap.get(ssid)) == null || wiFiProScoreInfo.internetAccessType == 0 || wiFiProScoreInfo.networkQosLevel == 0) {
            return false;
        }
        if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime >= 1800000) {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",crurrent rssi =  " + wifiInfo.getRssi() + ", last  rssi = " + wiFiProScoreInfo.rssi + ",interval  time = " + ((System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime) / 1000) + "s, last evaluate is NOT Valid");
        } else if (calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) == calculateSignalLevelHW(wifiInfo.is5GHz(), wifiInfo.getRssi())) {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",crurrent Signal =  " + calculateSignalLevelHW(wifiInfo.is5GHz(), wifiInfo.getRssi()) + ", last  Signal = " + calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) + ", isSemiAuto, last evaluate is Valid");
            return true;
        } else if (Math.abs(wifiInfo.getRssi() - wiFiProScoreInfo.rssi) < 20) {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",crurrent rssi =  " + wifiInfo.getRssi() + ", last  rssi = " + wiFiProScoreInfo.rssi + ", isSemiAuto, last evaluate is Valid");
            return true;
        } else {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",Signal level change,and rssi >20");
        }
        return false;
    }

    public boolean isLastEvaluateValid(ScanResult scanResult, int evalateType) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (scanResult == null || TextUtils.isEmpty(scanResult.SSID)) {
            return true;
        }
        if (isEvaluateRecordsEmpty()) {
            return false;
        }
        String ssid = "\"" + scanResult.SSID + "\"";
        if (!scoreEvaluateAPHashMap.containsKey(ssid) || (wiFiProScoreInfo = scoreEvaluateAPHashMap.get(ssid)) == null || wiFiProScoreInfo.internetAccessType == 0) {
            return false;
        }
        if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime < 60000) {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ", evaluate  protection duration, is valid");
            return true;
        }
        if (evalateType == 1) {
            if (wiFiProScoreInfo.internetAccessType == 4 && wiFiProScoreInfo.networkQosLevel == 0) {
                return false;
            }
            if (wiFiProScoreInfo.internetAccessType == 1) {
                if (wiFiProScoreInfo.failCounter < 2) {
                    Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ", is network congestion, activity is Settings, failCounter = " + wiFiProScoreInfo.failCounter);
                    return false;
                } else if (isEvaluateConditionChange(scanResult, evalateType)) {
                    reSetEvaluateRecord(ssid);
                    return true;
                }
            }
        } else if (wiFiProScoreInfo.internetAccessType == 3 || wiFiProScoreInfo.internetAccessType == 2) {
            return true;
        } else {
            if (wiFiProScoreInfo.internetAccessType == 4) {
                if (isEvaluateConditionChange(scanResult, evalateType)) {
                    updateScoreInfoLevel(ssid, 0);
                }
                return true;
            } else if (wiFiProScoreInfo.internetAccessType == 1) {
                if (wiFiProScoreInfo.failCounter >= 2) {
                    return true;
                }
                Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ", is network congestion, activity is Not settings, failCounter = " + wiFiProScoreInfo.failCounter);
                return false;
            }
        }
        if (isEvaluateConditionChange(scanResult, evalateType)) {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",last evaluate is NOT valid ");
            return false;
        }
        Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",last evaluate is valid ");
        return true;
    }

    public boolean isEvaluateConditionChange(ScanResult scanResult, int evalateType) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (scanResult == null || TextUtils.isEmpty(scanResult.SSID) || isEvaluateRecordsEmpty()) {
            return false;
        }
        String ssid = "\"" + scanResult.SSID + "\"";
        if (!scoreEvaluateAPHashMap.containsKey(ssid) || (wiFiProScoreInfo = scoreEvaluateAPHashMap.get(ssid)) == null) {
            return false;
        }
        long validTime = 0;
        int validRssiChange = 0;
        if (evalateType == 0) {
            validTime = 14400000;
            validRssiChange = 20;
        } else if (evalateType == 1) {
            validTime = 1800000;
            validRssiChange = 20;
        } else if (evalateType == 2) {
            validTime = 1800000;
            validRssiChange = 20;
        }
        if (wiFiProScoreInfo.internetAccessType == 2 || wiFiProScoreInfo.internetAccessType == 3) {
            if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime < 12 * validTime) {
                return false;
            }
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",last evaluate is PORTA or no intenet ,  is timeout, last evaluate date: " + WifiproUtils.formatTime(wiFiProScoreInfo.lastScoreTime));
            return true;
        } else if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime > validTime) {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",last evaluate is NOT Valid,last evaluate date: " + WifiproUtils.formatTime(wiFiProScoreInfo.lastScoreTime));
            return true;
        } else if (calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) != calculateSignalLevelHW(scanResult.is5GHz(), scanResult.level) && Math.abs(scanResult.level - wiFiProScoreInfo.rssi) > validRssiChange) {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + ",last evaluate is NOT Valid,SignalLevel change, and rssi change >20, last rssi=" + wiFiProScoreInfo.rssi + ", now rssi = " + scanResult.level);
            return true;
        } else if (Math.abs(calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) - calculateSignalLevelHW(scanResult.is5GHz(), scanResult.level)) > 2) {
            return true;
        } else {
            return false;
        }
    }

    public WiFiProScoreInfo getCurrentWiFiProScoreInfo(String ssid) {
        if (!isEvaluateRecordsEmpty() && !TextUtils.isEmpty(ssid)) {
            return scoreEvaluateAPHashMap.get(ssid);
        }
        Log.w(TAG, "getCurrentWifiProperties is null!");
        return null;
    }

    public static WiFiProScoreInfo getCurrentWiFiProScore(String ssid) {
        if (!isEvaluateRecordsEmpty() && !TextUtils.isEmpty(ssid)) {
            return scoreEvaluateAPHashMap.get(ssid);
        }
        Log.w(TAG, "getCurrentWifiProperties is null!");
        return null;
    }

    public boolean isAbandonEvaluate(String ssid) {
        WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
        if (wiFiProScoreInfo == null) {
            return true;
        }
        if (calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) >= 2) {
            return false;
        }
        Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + " rssi = " + wiFiProScoreInfo.rssi);
        return true;
    }

    public void cleanEvaluateRecords() {
        Queue<String> queue = this.mUnEvaluateAPQueue;
        if (queue != null && !queue.isEmpty()) {
            Log.i(TAG, "clean mUnEvaluateAPQueue");
            this.mUnEvaluateAPQueue.clear();
        }
        cleanEvaluateCacheRecords();
    }

    private void initEvaluateRecords() {
        if (scoreEvaluateAPHashMap == null) {
            scoreEvaluateAPHashMap = new HashMap();
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
        List<WiFiProScoreInfo> list = this.mSavedApList;
        if (list != null) {
            list.clear();
        }
        List<WiFiProScoreInfo> list2 = this.mOpenApList;
        if (list2 != null) {
            list2.clear();
        }
    }

    public boolean isSaveAP(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        if (getWifiConfiguration("\"" + scanResult.SSID + "\"") != null) {
            return true;
        }
        return false;
    }

    public boolean isAllowEvaluate(ScanResult scanResult, int evalateType) {
        if (scanResult == null) {
            return false;
        }
        int level = 3;
        if (evalateType == 1) {
            level = 2;
        }
        if (calculateSignalLevelHW(scanResult.is5GHz(), scanResult.level) < level) {
            return false;
        }
        if (getWifiConfiguration("\"" + scanResult.SSID + "\"") == null && isOpenAP(scanResult)) {
            return true;
        }
        return false;
    }

    private boolean isOpenAP(ScanResult result) {
        if (result == null) {
            return false;
        }
        return WifiProCommonUtils.isOpenNetwork(result.capabilities);
    }

    public void initWifiProEvaluateRecords() {
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (configNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configNetworks) {
                if (wifiConfiguration.SSID != null && !scoreEvaluateAPHashMap.containsKey(wifiConfiguration.SSID)) {
                    WiFiProScoreInfo info = new WiFiProScoreInfo();
                    info.ssid = wifiConfiguration.SSID;
                    if (wifiConfiguration.noInternetAccess) {
                        info.internetAccessType = 2;
                        scoreEvaluateAPHashMap.put(wifiConfiguration.SSID, info);
                    } else if (wifiConfiguration.portalNetwork) {
                        info.internetAccessType = 3;
                        scoreEvaluateAPHashMap.put(wifiConfiguration.SSID, info);
                    } else if (!wifiConfiguration.wifiProNoInternetAccess) {
                        info.internetAccessType = 4;
                        scoreEvaluateAPHashMap.put(wifiConfiguration.SSID, info);
                    }
                }
            }
            return;
        }
        Log.i(TAG, "configNetworks  == null ");
    }

    public boolean isAllowAutoEvaluate(List<ScanResult> scanResultList) {
        if (scanResultList == null || scanResultList.isEmpty()) {
            return false;
        }
        for (ScanResult scanResult : scanResultList) {
            String ssid = "\"" + scanResult.SSID + "\"";
            WifiConfiguration cfg = getWifiConfiguration(ssid);
            if (WifiProCommonUtils.isWifiSelfCuring() || (cfg != null && ((!cfg.noInternetAccess || WifiProCommonUtils.allowWifiConfigRecovery(cfg.internetHistory)) && !cfg.isTempCreated))) {
                Log.i(TAG, StringUtilEx.safeDisplaySsid(scanResult.SSID) + " , has save, not allow Evaluate************ ");
                return false;
            } else if (WifiProCommonUtils.allowRecheckForNoInternet(cfg, scanResult, this.mContext)) {
                Log.i(TAG, "don't allow to auto evaluating because of background recheck for no internet, candidate = " + StringUtilEx.safeDisplaySsid(ssid));
                return false;
            }
        }
        return true;
    }

    public void reSetNotAllowEvaluateRecord(ScanResult scanResult) {
        if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID) && !isEvaluateRecordsEmpty()) {
            String ssid = "\"" + scanResult.SSID + "\"";
            if (updateScoreInfoLevel(ssid, 0)) {
                Log.i(TAG, "reset ssid :" + StringUtilEx.safeDisplaySsid(ssid));
            }
        }
    }

    public synchronized void addEvaluateRecords(WifiInfo wifiInfo, int evalateType) {
        WiFiProScoreInfo wiFiProScoreInfo;
        initEvaluateRecords();
        if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID())) {
            String ssid = wifiInfo.getSSID();
            if (!INVAILD_SSID.equals(ssid)) {
                if (!scoreEvaluateAPHashMap.containsKey(ssid)) {
                    Log.i(TAG, "new wiFiProScoreInfo wifiInfo ssid :" + StringUtilEx.safeDisplaySsid(ssid));
                    wiFiProScoreInfo = new WiFiProScoreInfo(wifiInfo);
                } else if (!isLastEvaluateValid(wifiInfo, evalateType)) {
                    Log.i(TAG, "update  wiFiProScoreInfo wifiInfo ssid :" + StringUtilEx.safeDisplaySsid(ssid));
                    wiFiProScoreInfo = scoreEvaluateAPHashMap.get(ssid);
                    wiFiProScoreInfo.trusted = true;
                    wiFiProScoreInfo.evaluated = false;
                    wiFiProScoreInfo.invalid = true;
                    wiFiProScoreInfo.is5GHz = wifiInfo.is5GHz();
                    wiFiProScoreInfo.rssi = wifiInfo.getRssi();
                    wiFiProScoreInfo.lastUpdateTime = System.currentTimeMillis();
                } else {
                    return;
                }
                scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
            }
        }
    }

    public synchronized void addEvaluateRecords(ScanResult scanResult, int evalateType) {
        WiFiProScoreInfo wiFiProScoreInfo;
        initEvaluateRecords();
        initEvaluateCacheRecords();
        if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID)) {
            String ssid = "\"" + scanResult.SSID + "\"";
            if (!INVAILD_SSID.equals(scanResult.SSID)) {
                if (scoreEvaluateAPHashMap.containsKey(ssid)) {
                    wiFiProScoreInfo = scoreEvaluateAPHashMap.get(ssid);
                    if (System.currentTimeMillis() - wiFiProScoreInfo.lastUpdateTime < 2000 && scanResult.level < wiFiProScoreInfo.rssi) {
                        return;
                    }
                    if (!isLastEvaluateValid(scanResult, evalateType)) {
                        wiFiProScoreInfo.trusted = !scanResult.untrusted;
                        wiFiProScoreInfo.evaluated = false;
                        wiFiProScoreInfo.invalid = true;
                        wiFiProScoreInfo.rssi = scanResult.level;
                        wiFiProScoreInfo.is5GHz = scanResult.is5GHz();
                        wiFiProScoreInfo.lastUpdateTime = System.currentTimeMillis();
                        Log.i(TAG, "update wiFiProScoreInfo ScanResult ssid :" + StringUtilEx.safeDisplaySsid(ssid) + ", rssi = " + scanResult.level);
                    } else {
                        return;
                    }
                } else {
                    Log.i(TAG, "add  wiFiProScoreInfo ScanResult ssid :" + StringUtilEx.safeDisplaySsid(ssid));
                    wiFiProScoreInfo = new WiFiProScoreInfo(scanResult);
                }
                scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                if (isSaveAP(scanResult)) {
                    Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + " is saved ap");
                    this.mSavedApList.add(wiFiProScoreInfo);
                } else {
                    Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + " is open ap");
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
                    ScanResult tmpScanResult = this.mScanResultHashMap.get(scanResult.SSID);
                    if (tmpScanResult != null && tmpScanResult.level < scanResult.level) {
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
                this.mUnRepetitionScanResultList.add(it.next());
            }
        }
        return this.mUnRepetitionScanResultList;
    }

    public void orderByRssi() {
        List<WiFiProScoreInfo> list = this.mSavedApList;
        if (list != null && !list.isEmpty()) {
            Collections.sort(this.mSavedApList);
            Collections.reverse(this.mSavedApList);
            for (WiFiProScoreInfo saveAPInfo : this.mSavedApList) {
                if (!this.mUnEvaluateAPQueue.contains(saveAPInfo.ssid) && this.mUnEvaluateAPQueue.offer(saveAPInfo.ssid)) {
                    Log.i(TAG, StringUtilEx.safeDisplaySsid(saveAPInfo.ssid) + " offer to Queue:  rssi : " + saveAPInfo.rssi);
                }
            }
            this.mSavedApList.clear();
        }
        List<WiFiProScoreInfo> list2 = this.mOpenApList;
        if (!(list2 == null || list2.isEmpty())) {
            Collections.sort(this.mOpenApList);
            Collections.reverse(this.mOpenApList);
            for (WiFiProScoreInfo openAPInfo : this.mOpenApList) {
                if (!this.mUnEvaluateAPQueue.contains(openAPInfo.ssid) && this.mUnEvaluateAPQueue.offer(openAPInfo.ssid)) {
                    Log.i(TAG, StringUtilEx.safeDisplaySsid(openAPInfo.ssid) + " offer to Queue:  rssi : " + openAPInfo.rssi);
                }
            }
            this.mOpenApList.clear();
        }
    }

    public synchronized void updateEvaluateRecords(List<ScanResult> scanResultList, int evaluateType, String currentSsid) {
        if (scanResultList != null) {
            if (!scanResultList.isEmpty()) {
                initEvaluateRecords();
                for (ScanResult scanResult : scanResultList) {
                    if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID)) {
                        String ssid = "\"" + scanResult.SSID + "\"";
                        if (TextUtils.isEmpty(currentSsid) || !currentSsid.equals(ssid)) {
                            if (scoreEvaluateAPHashMap.containsKey(ssid) && !isLastEvaluateValid(scanResult, evaluateType)) {
                                updateScoreInfoLevel(ssid, 0);
                            }
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
        scoreEvaluateAPHashMap.put(ssid, wifiScoreProperties);
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
        WiFiProScoreInfo wiFiProScoreInfo;
        if (!TextUtils.isEmpty(ssid) && (wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid)) != null) {
            wiFiProScoreInfo.failCounter++;
            wiFiProScoreInfo.lastScoreTime = System.currentTimeMillis();
            scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
        }
    }

    public synchronized boolean updateScoreInfoType(String ssid, int internetAccessType) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (!TextUtils.isEmpty(ssid) && (wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid)) != null) {
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
                scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
                Log.i(TAG, "updateEvaluateRecords internetAccessType succeed! new type =  " + internetAccessType);
                return true;
            }
            Log.i(TAG, "internetAccessType not change, can not update");
        }
        return false;
    }

    public synchronized boolean updateScoreInfoLevel(String ssid, int networkQosLevel) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (TextUtils.isEmpty(ssid) || (wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid)) == null) {
            return false;
        }
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
        scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
        Log.i(TAG, "updateEvaluate Level  succeed!.new Level = " + wiFiProScoreInfo.networkQosLevel);
        return true;
    }

    public void updateWifiSecurityInfo(String ssid, int secStatus) {
        WiFiProScoreInfo scoreInfo;
        if (!TextUtils.isEmpty(ssid) && (scoreInfo = getCurrentWiFiProScoreInfo(ssid)) != null) {
            scoreInfo.networkSecurity = secStatus;
        }
    }

    public int getWifiSecurityInfo(String ssid) {
        WiFiProScoreInfo scoreInfo;
        if (TextUtils.isEmpty(ssid) || (scoreInfo = getCurrentWiFiProScoreInfo(ssid)) == null) {
            return -1;
        }
        return scoreInfo.networkSecurity;
    }

    public synchronized boolean updateScoreInfoAbandon(String ssid, boolean abandon) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (TextUtils.isEmpty(ssid) || (wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid)) == null || wiFiProScoreInfo.abandon == abandon) {
            return false;
        }
        wiFiProScoreInfo.abandon = abandon;
        if (abandon) {
            wiFiProScoreInfo.networkQosLevel = 0;
            wiFiProScoreInfo.internetAccessType = 0;
        }
        wiFiProScoreInfo.invalid = false;
        scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
        Log.i(TAG, "updateEvaluateRecords abandon succeed!");
        return true;
    }

    public synchronized boolean updateWifiProbeMode(String ssid, int mode) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (TextUtils.isEmpty(ssid) || (wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid)) == null) {
            return false;
        }
        wiFiProScoreInfo.probeMode = mode;
        Log.i(TAG, "*****updateWifiProbeMode, new probeMode  " + mode);
        scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
        return true;
    }

    public synchronized boolean updateScoreEvaluateStatus(String ssid, boolean evaluate) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (TextUtils.isEmpty(ssid) || (wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid)) == null) {
            return false;
        }
        wiFiProScoreInfo.evaluated = evaluate;
        wiFiProScoreInfo.invalid = false;
        wiFiProScoreInfo.lastScoreTime = System.currentTimeMillis();
        wiFiProScoreInfo.abandon = false;
        scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
        return true;
    }

    public synchronized boolean updateScoreEvaluateInvalid(String ssid, boolean invalid) {
        WiFiProScoreInfo wiFiProScoreInfo;
        if (TextUtils.isEmpty(ssid) || (wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid)) == null || !wiFiProScoreInfo.evaluated) {
            return false;
        }
        wiFiProScoreInfo.invalid = invalid;
        if (invalid) {
            wiFiProScoreInfo.evaluated = false;
        }
        scoreEvaluateAPHashMap.put(ssid, wiFiProScoreInfo);
        Log.i(TAG, "updateEvaluate Invalid succeed!, wiFiProScoreInfo " + wiFiProScoreInfo.dump());
        return true;
    }

    public synchronized void reSetEvaluateRecord(String ssid) {
        if (!TextUtils.isEmpty(ssid)) {
            if (scoreEvaluateAPHashMap != null && scoreEvaluateAPHashMap.containsKey(ssid)) {
                try {
                    Log.i(TAG, " remove " + StringUtilEx.safeDisplaySsid(ssid) + " form EvaluateRecord");
                    scoreEvaluateAPHashMap.remove(ssid);
                } catch (UnsupportedOperationException e) {
                    Log.w(TAG, " unsupportedOperationException ");
                }
            }
        }
    }

    public synchronized void reSetEvaluateRecord(Intent intent) {
        if (intent != null) {
            WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
            int changereson = intent.getIntExtra("changeReason", -1);
            if (config != null && !TextUtils.isEmpty(config.SSID) && changereson == 1 && !config.isTempCreated) {
                reSetEvaluateRecord(config.SSID);
            }
        }
    }

    public String getNextEvaluateWiFiSSID() {
        Queue<String> queue = this.mUnEvaluateAPQueue;
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        return this.mUnEvaluateAPQueue.poll();
    }

    public boolean connectWifi(String ssid) {
        WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
        if (wiFiProScoreInfo == null) {
            Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + "conenct  connect fail");
            evaluateAPHashMapDump();
            return false;
        }
        WifiConfiguration wifiConfiguration = getWifiConfiguration(ssid);
        if (wifiConfiguration == null) {
            if (wiFiProScoreInfo.trusted) {
                Log.i(TAG, StringUtilEx.safeDisplaySsid(ssid) + " trusted : " + wiFiProScoreInfo.trusted);
            }
            Log.i(TAG, "conenct open ap, create a new confg");
            wifiConfiguration = createOpenWifiConfiguration(ssid);
        }
        Bundle data = new Bundle();
        data.putParcelable("WifiConfiguration", wifiConfiguration);
        data.putBoolean("isToast", false);
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 11, data);
        if (result == null || !result.getBoolean("isWifiRestricted")) {
            this.mWifiManager.connect(wifiConfiguration, null);
            return true;
        }
        Log.w(TAG, "MDM deny connect!");
        return false;
    }

    public void addUntrustedOpenApList(String ssid) {
        List<String> list = this.mUntrustedOpenApList;
        if (list != null && ssid != null && !list.contains(ssid)) {
            Log.i(TAG, "add: " + StringUtilEx.safeDisplaySsid(ssid) + " to  UntrustedOpenApList ");
            this.mUntrustedOpenApList.add(ssid);
        }
    }

    public void clearUntrustedOpenApList() {
        List<String> list = this.mUntrustedOpenApList;
        if (list != null && !list.isEmpty()) {
            this.mUntrustedOpenApList.clear();
        }
    }

    public void forgetUntrustedOpenAp() {
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (configNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configNetworks) {
                if (wifiConfiguration.isTempCreated) {
                    Log.i(TAG, "isTempCreated, forget: " + StringUtilEx.safeDisplaySsid(wifiConfiguration.SSID) + " config ");
                    this.mWifiManager.forget(wifiConfiguration.networkId, null);
                }
            }
            List<String> list = this.mUntrustedOpenApList;
            if (list != null && list.isEmpty()) {
                this.mUntrustedOpenApList.clear();
            }
        }
    }

    private WifiConfiguration createOpenWifiConfiguration(String ssid) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        config.isTempCreated = true;
        String currentSsid = ssid.replace("\"", "");
        for (ScanResult scanResult : this.mWifiManager.getScanResults()) {
            if (TextUtils.isEmpty(scanResult.SSID) || !currentSsid.equals(scanResult.SSID)) {
                Log.e(TAG, "createOpenWifiConfiguration : ssid is empty or not current");
            } else if (scanResult.capabilities.contains("OWE")) {
                config.allowedKeyManagement.set(9);
                config.requirePMF = true;
            } else {
                config.allowedKeyManagement.set(0);
            }
        }
        String oriSsid = getOriSsid(ssid);
        if (!TextUtils.isEmpty(oriSsid)) {
            Log.i(TAG, "ssid:" + StringUtilEx.safeDisplaySsid(ssid) + ", oriSsid:" + oriSsid);
            config.oriSsid = oriSsid;
        } else {
            Log.i(TAG, "oriSsid is null");
        }
        return config;
    }

    private String getOriSsid(String ssid) {
        WifiManager wifiManager;
        List<ScanResult> resultLists;
        if (TextUtils.isEmpty(ssid) || (wifiManager = this.mWifiManager) == null || (resultLists = wifiManager.getScanResults()) == null) {
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
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (configNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configNetworks) {
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals(ssid)) {
                    return wifiConfiguration;
                }
            }
            return null;
        }
        Log.d(TAG, "configNetworks  == null ");
        return null;
    }

    public int calculateSignalLevelHW(boolean is5G, int rssi) {
        return HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(is5G ? 5180 : 2412, rssi);
    }

    public int calculateTestWiFiLevel(String ssid) {
        WiFiProScoreInfo properties;
        if (TextUtils.isEmpty(ssid)) {
            Log.i(TAG, "calculateTestWiFiLevel ssid == null ");
            return 0;
        } else if (!scoreEvaluateAPHashMap.containsKey(ssid) || (properties = scoreEvaluateAPHashMap.get(ssid)) == null || properties.abandon || 2 == properties.internetAccessType || 3 == properties.internetAccessType) {
            return 0;
        } else {
            int newSignalLevel = calculateSignalLevelHW(properties.is5GHz, properties.rssi);
            int boost5G = 0;
            if (properties.is5GHz) {
                boost5G = 1;
            }
            int level = newSignalLevel + boost5G;
            if (level > 3) {
                return 3;
            }
            if (level > 1) {
                return 2;
            }
            return 1;
        }
    }

    public synchronized int getOldNetworkType(String ssid) {
        int oldNetworkType;
        oldNetworkType = 0;
        if (!TextUtils.isEmpty(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = getCurrentWiFiProScoreInfo(ssid);
            if (wiFiProScoreInfo != null) {
                Log.i(TAG, "probeMode: " + wiFiProScoreInfo.probeMode);
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
            newNetworkType = 3;
        } else if (-1 == checkResult) {
            newNetworkType = 2;
        }
        return newNetworkType;
    }

    public synchronized int getChrDiffType(int oldType, int newType) {
        int diffType = 0;
        if (oldType == 0 || oldType == newType) {
            return 0;
        }
        if (oldType == 4 && newType == 2) {
            diffType = 1;
        } else if (oldType == 4 && newType == 3) {
            diffType = 2;
        } else if (oldType == 2 && newType == 4) {
            diffType = 3;
        } else if (oldType == 2 && newType == 3) {
            diffType = 4;
        } else if (oldType == 3 && newType == 4) {
            diffType = 5;
        } else if (oldType == 3 && newType == 2) {
            diffType = 6;
        } else if (oldType == 1 && newType == 4) {
            diffType = 7;
        } else if (oldType == 1 && newType == 2) {
            diffType = 8;
        } else if (oldType == 1 && newType == 3) {
            diffType = 9;
        }
        return diffType;
    }
}
