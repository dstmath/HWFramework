package com.android.server.wifi.wifipro;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HwQoE.HidataWechatTraffic;
import com.android.server.wifi.HwWifiServiceFactory;
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
    private static Map<String, WiFiProScoreInfo> mEvaluateAPHashMap = new HashMap();
    private Context mContext;
    private boolean mIsWiFiProEvaluateEnabled;
    private List<WiFiProScoreInfo> mOpenApList;
    private List<WiFiProScoreInfo> mSavedApList;
    private Map<String, ScanResult> mScanResultHashMap;
    private Queue<String> mUnEvaluateAPQueue;
    private List<ScanResult> mUnRepetitionScanResultList;
    private List<String> mUntrustedOpenApList;
    private WifiManager mWifiManager;

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
        Iterator it = this.mUnEvaluateAPQueue.iterator();
        while (it.hasNext()) {
            Log.d(TAG, "mUnEvaluateAPQueue ssid =" + ((String) it.next()));
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
            WiFiProScoreInfo wiFiProScoreInfo = mEvaluateAPHashMap.get(ssid);
            if (wiFiProScoreInfo == null || wiFiProScoreInfo.internetAccessType == 0 || wiFiProScoreInfo.networkQosLevel == 0) {
                return false;
            }
            if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime >= 1800000) {
                Log.d(TAG, ssid + ",crurrent rssi =  " + wifiinfo.getRssi() + ", last  rssi = " + wiFiProScoreInfo.rssi + ",interval  time = " + ((System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime) / 1000) + "s, last evaluate is NOT Valid");
            } else if (calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) == calculateSignalLevelHW(wifiinfo.is5GHz(), wifiinfo.getRssi())) {
                Log.d(TAG, ssid + ",crurrent Signal =  " + calculateSignalLevelHW(wifiinfo.is5GHz(), wifiinfo.getRssi()) + ", last  Signal = " + calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) + ", isSemiAuto, last evaluate is Valid");
                return true;
            } else if (Math.abs(wifiinfo.getRssi() - wiFiProScoreInfo.rssi) < 20) {
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
        WiFiProScoreInfo wiFiProScoreInfo = mEvaluateAPHashMap.get(ssid);
        if (wiFiProScoreInfo == null || wiFiProScoreInfo.internetAccessType == 0) {
            return false;
        }
        if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime < HidataWechatTraffic.MIN_VALID_TIME) {
            Log.d(TAG, ssid + ", evaluate  protection duration, is valid");
            return true;
        }
        if (evalate_type == 1) {
            if (wiFiProScoreInfo.internetAccessType == 4 && wiFiProScoreInfo.networkQosLevel == 0) {
                return false;
            }
            if (wiFiProScoreInfo.internetAccessType == 1) {
                if (wiFiProScoreInfo.failCounter < 2) {
                    Log.d(TAG, ssid + ", is network congestion, activity is Settings, failCounter = " + wiFiProScoreInfo.failCounter);
                    return false;
                } else if (isEvaluateConditionChange(scanResult, evalate_type)) {
                    reSetEvaluateRecord(ssid);
                    return true;
                }
            }
        } else if (wiFiProScoreInfo.internetAccessType == 3 || wiFiProScoreInfo.internetAccessType == 2) {
            return true;
        } else {
            if (wiFiProScoreInfo.internetAccessType == 4) {
                if (isEvaluateConditionChange(scanResult, evalate_type)) {
                    updateScoreInfoLevel(ssid, 0);
                }
                return true;
            } else if (wiFiProScoreInfo.internetAccessType == 1) {
                if (wiFiProScoreInfo.failCounter >= 2) {
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

    public boolean isEvaluateConditionChange(ScanResult scanResult, int evalate_type) {
        if (scanResult == null || TextUtils.isEmpty(scanResult.SSID) || isEvaluateRecordsEmpty()) {
            return false;
        }
        String ssid = "\"" + scanResult.SSID + "\"";
        if (mEvaluateAPHashMap.containsKey(ssid)) {
            WiFiProScoreInfo wiFiProScoreInfo = mEvaluateAPHashMap.get(ssid);
            if (wiFiProScoreInfo == null) {
                return false;
            }
            long valid_time = 0;
            int valid_rssi_change = 0;
            if (evalate_type == 0) {
                valid_time = 14400000;
                valid_rssi_change = 20;
            } else if (evalate_type == 1) {
                valid_time = 1800000;
                valid_rssi_change = 20;
            } else if (evalate_type == 2) {
                valid_time = 1800000;
                valid_rssi_change = 20;
            }
            if (wiFiProScoreInfo.internetAccessType == 2 || wiFiProScoreInfo.internetAccessType == 3) {
                if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime < 12 * valid_time) {
                    return false;
                }
                Log.d(TAG, ssid + ",last evaluate is  PORTA or no intenet ,  is timeout, last evaluate date: " + WifiproUtils.formatTime(wiFiProScoreInfo.lastScoreTime));
                return true;
            } else if (System.currentTimeMillis() - wiFiProScoreInfo.lastScoreTime > valid_time) {
                Log.d(TAG, ssid + ",last evaluate is NOT Valid,last evaluate date: " + WifiproUtils.formatTime(wiFiProScoreInfo.lastScoreTime));
                return true;
            } else if (calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) != calculateSignalLevelHW(scanResult.is5GHz(), scanResult.level) && Math.abs(scanResult.level - wiFiProScoreInfo.rssi) > valid_rssi_change) {
                Log.d(TAG, ssid + ",last evaluate is NOT Valid,SignalLevel change, and rssi change >20, last rssi=" + wiFiProScoreInfo.rssi + ", now rssi = " + scanResult.level);
                return true;
            } else if (Math.abs(calculateSignalLevelHW(wiFiProScoreInfo.is5GHz, wiFiProScoreInfo.rssi) - calculateSignalLevelHW(scanResult.is5GHz(), scanResult.level)) > 2) {
                return true;
            }
        }
        return false;
    }

    public WiFiProScoreInfo getCurrentWiFiProScoreInfo(String ssid) {
        if (!isEvaluateRecordsEmpty() && !TextUtils.isEmpty(ssid)) {
            return mEvaluateAPHashMap.get(ssid);
        }
        Log.w(TAG, "getCurrentWifiProperties is null!");
        return null;
    }

    public static WiFiProScoreInfo getCurrentWiFiProScore(String ssid) {
        if (!isEvaluateRecordsEmpty() && !TextUtils.isEmpty(ssid)) {
            return mEvaluateAPHashMap.get(ssid);
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
        Log.d(TAG, ssid + " rssi = " + wiFiProScoreInfo.rssi);
        return true;
    }

    public void cleanEvaluateRecords() {
        if (this.mUnEvaluateAPQueue != null && !this.mUnEvaluateAPQueue.isEmpty()) {
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
        if (scanResult == null) {
            return false;
        }
        if (getWifiConfiguration("\"" + scanResult.SSID + "\"") != null) {
            return true;
        }
        return false;
    }

    public boolean isAllowEvaluate(ScanResult scanResult, int evalate_type) {
        if (scanResult == null) {
            return false;
        }
        int level = 3;
        if (evalate_type == 1) {
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
        if (!result.capabilities.contains("WEP") && !result.capabilities.contains("WAPI-PSK") && !result.capabilities.contains("QUALCOMM-WAPI-PSK") && !result.capabilities.contains("WAPI-CERT") && !result.capabilities.contains("QUALCOMM-WAPI-CERT") && !result.capabilities.contains("PSK") && !result.capabilities.contains("EAP")) {
            return true;
        }
        return false;
    }

    public void initWifiProEvaluateRecords() {
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configNetworks) {
                if (wifiConfiguration.SSID != null && !mEvaluateAPHashMap.containsKey(wifiConfiguration.SSID)) {
                    WiFiProScoreInfo info = new WiFiProScoreInfo();
                    info.ssid = wifiConfiguration.SSID;
                    if (wifiConfiguration.noInternetAccess) {
                        info.internetAccessType = 2;
                        mEvaluateAPHashMap.put(wifiConfiguration.SSID, info);
                    } else if (wifiConfiguration.portalNetwork) {
                        info.internetAccessType = 3;
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
            WifiConfiguration cfg = getWifiConfiguration("\"" + scanResult.SSID + "\"");
            if (WifiProCommonUtils.isWifiSelfCuring() || (cfg != null && ((!cfg.noInternetAccess || WifiProCommonUtils.allowWifiConfigRecovery(cfg.internetHistory)) && !cfg.isTempCreated))) {
                Log.d(TAG, scanResult.SSID + " , has save, not allow Evaluate************ ");
                return false;
            } else if (WifiProCommonUtils.allowRecheckForNoInternet(cfg, scanResult, this.mContext)) {
                Log.d(TAG, "don't allow to auto evaluating because of background recheck for no internet, candidate = " + ssid);
                return false;
            }
        }
        return true;
    }

    public void reSetNotAllowEvaluateRecord(ScanResult scanResult) {
        if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID) && !isEvaluateRecordsEmpty()) {
            if (updateScoreInfoLevel("\"" + scanResult.SSID + "\"", 0)) {
                Log.d(TAG, "reset ssid :" + ssid);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008b, code lost:
        return;
     */
    public synchronized void addEvaluateRecords(WifiInfo wifiInfo, int evalate_type) {
        WiFiProScoreInfo wiFiProScoreInfo;
        initEvaluateRecords();
        if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID())) {
            String ssid = wifiInfo.getSSID();
            if (!INVAILD_SSID.equals(ssid)) {
                if (!mEvaluateAPHashMap.containsKey(ssid)) {
                    Log.d(TAG, "new wiFiProScoreInfo wifiinfo ssid :" + ssid);
                    wiFiProScoreInfo = new WiFiProScoreInfo(wifiInfo);
                } else if (!isLastEvaluateValid(wifiInfo, evalate_type)) {
                    Log.d(TAG, "update  wiFiProScoreInfo wifiinfo ssid :" + ssid);
                    wiFiProScoreInfo = mEvaluateAPHashMap.get(ssid);
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

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ff, code lost:
        return;
     */
    public synchronized void addEvaluateRecords(ScanResult scanResult, int evalate_type) {
        WiFiProScoreInfo wiFiProScoreInfo;
        initEvaluateRecords();
        initEvaluateCacheRecords();
        if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID)) {
            String ssid = "\"" + scanResult.SSID + "\"";
            if (!INVAILD_SSID.equals(scanResult.SSID)) {
                if (mEvaluateAPHashMap.containsKey(ssid)) {
                    wiFiProScoreInfo = mEvaluateAPHashMap.get(ssid);
                    if (System.currentTimeMillis() - wiFiProScoreInfo.lastUpdateTime < 2000 && scanResult.level < wiFiProScoreInfo.rssi) {
                        return;
                    }
                    if (!isLastEvaluateValid(scanResult, evalate_type)) {
                        wiFiProScoreInfo.trusted = !scanResult.untrusted;
                        wiFiProScoreInfo.evaluated = false;
                        wiFiProScoreInfo.invalid = true;
                        wiFiProScoreInfo.rssi = scanResult.level;
                        wiFiProScoreInfo.is5GHz = scanResult.is5GHz();
                        wiFiProScoreInfo.lastUpdateTime = System.currentTimeMillis();
                        Log.i(TAG, "update  wiFiProScoreInfo ScanResult ssid :" + ssid + ", rssi = " + scanResult.level);
                    } else {
                        return;
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
                    ScanResult scan_result = this.mScanResultHashMap.get(scanResult.SSID);
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
                this.mUnRepetitionScanResultList.add(it.next());
            }
        }
        return this.mUnRepetitionScanResultList;
    }

    public void orderByRssi() {
        if (this.mSavedApList != null && !this.mSavedApList.isEmpty()) {
            Collections.sort(this.mSavedApList);
            Collections.reverse(this.mSavedApList);
            try {
                for (WiFiProScoreInfo saveAPInfo : this.mSavedApList) {
                    if (!this.mUnEvaluateAPQueue.contains(saveAPInfo.ssid) && this.mUnEvaluateAPQueue.offer(saveAPInfo.ssid)) {
                        Log.d(TAG, saveAPInfo.ssid + " offer to Queue:  rssi : " + saveAPInfo.rssi);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
            } catch (Throwable th) {
                this.mSavedApList.clear();
                throw th;
            }
            this.mSavedApList.clear();
        }
        if (this.mOpenApList != null && !this.mOpenApList.isEmpty()) {
            Collections.sort(this.mOpenApList);
            Collections.reverse(this.mOpenApList);
            try {
                for (WiFiProScoreInfo openAPInfo : this.mOpenApList) {
                    if (!this.mUnEvaluateAPQueue.contains(openAPInfo.ssid) && this.mUnEvaluateAPQueue.offer(openAPInfo.ssid)) {
                        Log.d(TAG, openAPInfo.ssid + " offer to Queue:  rssi : " + openAPInfo.rssi);
                    }
                }
            } catch (Exception e2) {
                Log.w(TAG, e2.getMessage());
            } catch (Throwable th2) {
                this.mOpenApList.clear();
                throw th2;
            }
            this.mOpenApList.clear();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0065, code lost:
        return;
     */
    public synchronized void updateEvaluateRecords(List<ScanResult> scanResultList, int evaluate_type, String currentSsid) {
        if (scanResultList != null) {
            if (!scanResultList.isEmpty()) {
                initEvaluateRecords();
                for (ScanResult scanResult : scanResultList) {
                    if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID)) {
                        String ssid = "\"" + scanResult.SSID + "\"";
                        if (TextUtils.isEmpty(currentSsid) || !currentSsid.equals(ssid)) {
                            if (mEvaluateAPHashMap.containsKey(ssid) && !isLastEvaluateValid(scanResult, evaluate_type)) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004c, code lost:
        return false;
     */
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
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004b, code lost:
        return false;
     */
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        return false;
     */
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0023, code lost:
        return false;
     */
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
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        return false;
     */
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
        } else {
            return;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002b, code lost:
        return;
     */
    public synchronized void reSetEvaluateRecord(Intent intent) {
        if (intent != null) {
            WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
            int changereson = intent.getIntExtra("changeReason", -1);
            if (config != null && !TextUtils.isEmpty(config.SSID) && changereson == 1 && !config.isTempCreated) {
                reSetEvaluateRecord(config.SSID);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0039, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003b, code lost:
        return;
     */
    public synchronized void restorePortalEvaluateRecord(String currSsid) {
        if (!TextUtils.isEmpty(currSsid)) {
            if (!INVAILD_SSID.equals(currSsid)) {
                WiFiProScoreInfo scoreInfo = getCurrentWiFiProScoreInfo(currSsid);
                if (!(scoreInfo == null || scoreInfo.internetAccessType == 0 || scoreInfo.internetAccessType == 3)) {
                    Log.d(TAG, "restore to portal, ssid = " + currSsid);
                    updateScoreInfoType(currSsid, 3);
                }
            }
        }
    }

    public String getNextEvaluateWiFiSSID() {
        if (this.mUnEvaluateAPQueue == null || this.mUnEvaluateAPQueue.isEmpty()) {
            return null;
        }
        return this.mUnEvaluateAPQueue.poll();
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
        if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(wifiConfiguration, false)) {
            Log.w(TAG, "MDM deny connect!");
            return false;
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
        if (!TextUtils.isEmpty(oriSsid)) {
            Log.d(TAG, "ssid:" + ssid + ", oriSsid:" + oriSsid);
            config.oriSsid = oriSsid;
        } else {
            Log.d(TAG, "oriSsid is null");
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

    public int calculateSignalLevelHW(boolean is5G, int rssi) {
        return HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(is5G ? 5180 : 2412, rssi);
    }

    public int calculateTestWiFiLevel(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "calculateTestWiFiLevel ssid == null ");
            return 0;
        } else if (!mEvaluateAPHashMap.containsKey(ssid)) {
            return 0;
        } else {
            WiFiProScoreInfo properties = mEvaluateAPHashMap.get(ssid);
            if (properties == null || properties.abandon || 2 == properties.internetAccessType || 3 == properties.internetAccessType) {
                return 0;
            }
            int newSignalLevel = calculateSignalLevelHW(properties.is5GHz, properties.rssi);
            int boost_5G = 0;
            if (properties.is5GHz) {
                boost_5G = 1;
            }
            int level = newSignalLevel + boost_5G;
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
            newNetworkType = 3;
        } else if (-1 == checkResult) {
            newNetworkType = 2;
        }
        return newNetworkType;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0043, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0045, code lost:
        return 0;
     */
    public synchronized int getChrDiffType(int oldType, int newType) {
        int diffType = 0;
        if (oldType != 0 && oldType != newType) {
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
        }
    }
}
