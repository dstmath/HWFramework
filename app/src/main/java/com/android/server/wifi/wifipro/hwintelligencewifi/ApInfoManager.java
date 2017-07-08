package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class ApInfoManager {
    public static final int AUTO_OPEN_RSSI_VALUE = -75;
    public static final int MSG_SCAN_AGAIN = 3;
    public static final int MSG_START_SCAN = 1;
    public static final int MSG_UPDATE_SCAN_RESULT = 2;
    public static final int SCAN_INTERVAL_NORMAL_1 = 60000;
    public static final int SCAN_INTERVAL_NORMAL_3 = 180000;
    public static final int SCAN_INTERVAL_NORMAL_5 = 300000;
    public static final int SCAN_INTERVAL_SHORT = 20000;
    public static final int SCAN_TYPE_FINE_MIN = 3;
    public static final int SCAN_TYPE_ONE_MIN = 1;
    public static final int SCAN_TYPE_SHORT = 0;
    public static final int SCAN_TYPE_THREE_MIN = 2;
    private Context mContext;
    private DataBaseManager mDbManager;
    private Handler mHandler;
    private HwintelligenceWiFiCHR mHwintelligenceWiFiCHR;
    private List<APInfoData> mInfos;
    private boolean mIsRunning;
    private boolean mIsScanAlwaysAvailable;
    private boolean mIsScanInShort;
    private boolean mIsScaning;
    private Object mLock;
    private int mScanTimes;
    private int mScanTotleTimes;
    private int mScanType;
    private HwIntelligenceStateMachine mStateMachine;
    private Handler mWifiHandler;
    private WifiManager mWifiManager;

    public ApInfoManager(Context context, HwIntelligenceStateMachine stateMachine, Handler handler) {
        this.mIsScaning = false;
        this.mIsScanAlwaysAvailable = false;
        this.mInfos = new ArrayList();
        this.mScanTotleTimes = 20;
        this.mScanTimes = SCAN_TYPE_SHORT;
        this.mScanType = SCAN_TYPE_ONE_MIN;
        this.mIsScanInShort = false;
        this.mIsRunning = false;
        this.mLock = new Object();
        this.mWifiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ApInfoManager.SCAN_TYPE_THREE_MIN /*2*/:
                        Log.e(MessageUtil.TAG, "MSG_UPDATE_SCAN_RESULT");
                        List<ScanResult> mLists = ApInfoManager.this.mWifiManager.getScanResults();
                        if (mLists != null) {
                            Log.e(MessageUtil.TAG, "mLists.size() = " + mLists.size());
                        }
                        if (mLists == null || mLists.size() == 0 || !ApInfoManager.this.handleScanResult(mLists)) {
                            ApInfoManager apInfoManager = ApInfoManager.this;
                            apInfoManager.mScanTimes = apInfoManager.mScanTimes + ApInfoManager.SCAN_TYPE_ONE_MIN;
                            if (ApInfoManager.this.mScanTimes <= ApInfoManager.this.mScanTotleTimes) {
                                if (!(mLists == null || !ApInfoManager.this.isInMonitorNearbyAp(mLists) || ApInfoManager.this.mIsScanInShort)) {
                                    ApInfoManager.this.mScanType = ApInfoManager.SCAN_TYPE_SHORT;
                                    ApInfoManager.this.mIsScanInShort = true;
                                }
                                ApInfoManager.this.setScanInterval(ApInfoManager.this.mScanType);
                            } else if (ApInfoManager.this.mScanType == ApInfoManager.SCAN_TYPE_FINE_MIN) {
                                ApInfoManager.this.stopScanAp();
                            } else {
                                apInfoManager = ApInfoManager.this;
                                apInfoManager.mScanType = apInfoManager.mScanType + ApInfoManager.SCAN_TYPE_ONE_MIN;
                                ApInfoManager.this.mScanTimes = ApInfoManager.SCAN_TYPE_SHORT;
                                Log.e(MessageUtil.TAG, "MSG_UPDATE_SCAN_RESULT set scan Interval mScanType = " + ApInfoManager.this.mScanType);
                                ApInfoManager.this.setScanInterval(ApInfoManager.this.mScanType);
                            }
                        }
                    case ApInfoManager.SCAN_TYPE_FINE_MIN /*3*/:
                        Log.e(MessageUtil.TAG, "MSG_SCAN_AGAIN");
                        removeMessages(ApInfoManager.SCAN_TYPE_FINE_MIN);
                        ApInfoManager.this.setScanAlwaysEnable();
                    default:
                }
            }
        };
        this.mContext = context;
        this.mStateMachine = stateMachine;
        this.mHandler = handler;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwintelligenceWiFiCHR = HwintelligenceWiFiCHR.getInstance(stateMachine);
        resetScanAlwaysEnable();
    }

    public void start() {
        synchronized (this.mLock) {
            this.mDbManager = new DataBaseManager(this.mContext);
            this.mInfos = this.mDbManager.getAllApInfos();
            this.mIsRunning = true;
        }
    }

    public void stop() {
        synchronized (this.mLock) {
            if (!this.mIsRunning || this.mDbManager == null) {
                return;
            }
            stopScanAp();
            this.mDbManager.closeDB();
            this.mIsRunning = false;
        }
    }

    public void addCurrentApInfo(String cellid) {
        synchronized (this.mLock) {
            if (cellid == null) {
                return;
            }
            inlineAddCurrentApInfo(cellid);
        }
    }

    private void inlineAddCurrentApInfo(String cellid) {
        if (this.mIsRunning) {
            Log.e(MessageUtil.TAG, "addCurrentApInfo cellid =" + cellid);
            WifiInfo Info = this.mWifiManager.getConnectionInfo();
            if (Info == null || Info.getBSSID() == null || cellid == null || Info.getBSSID().equals(MessageUtil.ILLEGAL_BSSID_01) || Info.getBSSID().equals(MessageUtil.ILLEGAL_BSSID_02)) {
                Log.e(MessageUtil.TAG, "inlineAddCurrentApInfo invalid AP info");
                return;
            }
            APInfoData data = getApInfo(Info.getBSSID());
            List<CellInfoData> cellInfos;
            List<String> nearbyApInfosList;
            if (data == null) {
                Log.e(MessageUtil.TAG, "addCurrentApInfo addApInfos");
                long mTime = System.currentTimeMillis();
                int mAuthType = getAuthType(Info.getNetworkId());
                if (this.mInfos.size() == 499) {
                    this.mHwintelligenceWiFiCHR.uploadWhiteNum((short) 500);
                }
                if (this.mInfos.size() >= MessageUtil.DB_BSSID_MAX_QUANTA) {
                    APInfoData oldestData = getOldestApInfoData();
                    if (oldestData != null) {
                        this.mDbManager.delAPInfos(oldestData.getBssid());
                        this.mInfos.remove(oldestData);
                    }
                }
                this.mDbManager.addApInfos(Info.getBSSID(), Info.getSSID(), cellid, getAuthType(Info.getNetworkId()));
                APInfoData mNewData = new APInfoData(Info.getBSSID(), Info.getSSID(), SCAN_TYPE_SHORT, mAuthType, mTime);
                cellInfos = this.mDbManager.queryCellInfoByBssid(mNewData.getBssid());
                if (cellInfos.size() != 0) {
                    mNewData.setCellInfo(cellInfos);
                }
                nearbyApInfosList = this.mDbManager.getNearbyApInfo(mNewData.getBssid());
                if (nearbyApInfosList.size() != 0) {
                    mNewData.setNearbyAPInfos(nearbyApInfosList);
                }
                this.mInfos.add(mNewData);
            } else if (data.getCellInfos().size() >= 50) {
                Log.e(MessageUtil.TAG, "addCurrentApInfo MessageUtil.DB_NEARBY_CELLID_MAX_QUANTA");
                this.mDbManager.delAPInfos(data.getBssid());
                this.mInfos.remove(data);
            } else {
                int authtype = getAuthType(Info.getNetworkId());
                if (isCellIdExit(data, cellid)) {
                    Log.e(MessageUtil.TAG, "addCurrentApInfo info is already there");
                } else {
                    Log.e(MessageUtil.TAG, "addCurrentApInfo addCellIdInfo");
                    this.mDbManager.addCellInfo(Info.getBSSID(), cellid);
                    this.mDbManager.addNearbyApInfo(Info.getBSSID());
                    cellInfos = this.mDbManager.queryCellInfoByBssid(data.getBssid());
                    if (cellInfos.size() != 0) {
                        data.setCellInfo(cellInfos);
                    }
                    nearbyApInfosList = this.mDbManager.getNearbyApInfo(data.getBssid());
                    if (nearbyApInfosList.size() != 0) {
                        data.setNearbyAPInfos(nearbyApInfosList);
                    }
                }
                this.mDbManager.updateBssidTimer(Info.getBSSID());
                data.setLastTime(System.currentTimeMillis());
                if (!data.getSsid().equals(Info.getSSID())) {
                    Log.d(MessageUtil.TAG, "inlineAddCurrentApInfo updateSsid  data.getSsid() = " + data.getSsid() + "  Info.getSSID() = " + Info.getSSID());
                    this.mDbManager.updateSsid(Info.getBSSID(), Info.getSSID());
                    data.setSsid(Info.getSSID());
                }
                if (data.getAuthType() != authtype) {
                    Log.d(MessageUtil.TAG, "inlineAddCurrentApInfo updateSsid  data.getAuthType() = " + data.getAuthType() + "  authtype = " + authtype);
                    this.mDbManager.updateAuthType(Info.getBSSID(), authtype);
                    data.setAuthType(authtype);
                }
            }
            Log.e(MessageUtil.TAG, "inlineAddCurrentApInfo mInfos.size()=" + this.mInfos.size());
        }
    }

    public void updataApInfo(String cellid) {
        synchronized (this.mLock) {
            Log.e(MessageUtil.TAG, "updataApInfo cellid =" + cellid);
            if (!this.mIsRunning || cellid == null) {
                return;
            }
            inlineAddCurrentApInfo(cellid);
        }
    }

    public void startScanAp() {
        Log.e(MessageUtil.TAG, "startScanAp mIsScaning = " + this.mIsScaning);
        if (!(this.mIsScaning || this.mWifiManager == null)) {
            if (this.mWifiManager.isWifiEnabled()) {
                Log.e(MessageUtil.TAG, "startScanAp wifi is opened");
                stopScanAp();
            } else {
                this.mIsScaning = true;
                this.mScanTimes = SCAN_TYPE_SHORT;
                this.mScanType = SCAN_TYPE_ONE_MIN;
                this.mIsScanAlwaysAvailable = this.mWifiManager.isScanAlwaysAvailable();
                Log.w(MessageUtil.TAG, "startScanAp isScanAlwaysAvailable  = " + this.mIsScanAlwaysAvailable);
                if (this.mIsScanAlwaysAvailable) {
                    this.mWifiManager.startScan();
                } else {
                    Global.putInt(this.mContext.getContentResolver(), "wifi_pro_save_scan_always_enabled", SCAN_TYPE_ONE_MIN);
                    Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", SCAN_TYPE_ONE_MIN);
                    this.mHandler.sendEmptyMessageDelayed(MessageUtil.CMD_START_SCAN, 2000);
                }
            }
        }
    }

    public void stopScanAp() {
        Log.e(MessageUtil.TAG, "stopScanAp mIsScaning = " + this.mIsScaning);
        if (this.mIsScaning) {
            resetScanAlwaysEnable();
        }
        this.mIsScaning = false;
        this.mIsScanInShort = false;
        this.mScanTimes = SCAN_TYPE_SHORT;
        this.mScanType = SCAN_TYPE_ONE_MIN;
        this.mWifiHandler.removeMessages(SCAN_TYPE_FINE_MIN);
    }

    public boolean isScaning() {
        return this.mIsScaning;
    }

    private void setScanAlwaysEnable() {
        if (this.mScanType == SCAN_TYPE_FINE_MIN || this.mScanType == SCAN_TYPE_THREE_MIN) {
            this.mIsScanAlwaysAvailable = this.mWifiManager.isScanAlwaysAvailable();
            Log.e(MessageUtil.TAG, "isScanAlwaysAvailable  = " + this.mIsScanAlwaysAvailable);
            if (this.mIsScanAlwaysAvailable) {
                this.mWifiManager.startScan();
                return;
            }
            Global.putInt(this.mContext.getContentResolver(), "wifi_pro_save_scan_always_enabled", SCAN_TYPE_ONE_MIN);
            Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", SCAN_TYPE_ONE_MIN);
            this.mHandler.sendEmptyMessageDelayed(MessageUtil.CMD_START_SCAN, 2000);
            return;
        }
        this.mWifiManager.startScan();
    }

    private void resetScanAlwaysEnable() {
        boolean isWiFiProEnable = Global.getInt(this.mContext.getContentResolver(), "wifi_pro_save_scan_always_enabled", SCAN_TYPE_SHORT) == SCAN_TYPE_ONE_MIN;
        Log.e(MessageUtil.TAG, "resetScanAlwaysEnable isWiFiProEnable = " + isWiFiProEnable);
        if (isWiFiProEnable) {
            Global.putInt(this.mContext.getContentResolver(), "wifi_pro_save_scan_always_enabled", SCAN_TYPE_SHORT);
            Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", SCAN_TYPE_SHORT);
        }
    }

    public void updateScanResult() {
        if (this.mIsScaning) {
            this.mWifiHandler.sendEmptyMessage(SCAN_TYPE_THREE_MIN);
        }
    }

    public boolean processScanResult(String cellid) {
        Log.e(MessageUtil.TAG, "processScanResult cellid = " + cellid);
        List<ScanResult> mLists = this.mWifiManager.getScanResults();
        if (mLists != null) {
            Log.e(MessageUtil.TAG, "mLists.size() = " + mLists.size());
        }
        boolean checkResult = false;
        synchronized (this.mLock) {
            if (mLists != null) {
                if (mLists.size() > 0) {
                    for (ScanResult result : mLists) {
                        APInfoData data = getApInfo(result.BSSID);
                        if (data != null) {
                            if (isCellIdExit(data, cellid)) {
                                Log.e(MessageUtil.TAG, "addCurrentApInfo info is already there");
                            } else {
                                Log.e(MessageUtil.TAG, "addCurrentApInfo addCellIdInfo");
                                this.mDbManager.addCellInfo(data.getBssid(), cellid);
                                checkResult = true;
                                List<CellInfoData> cellInfos = this.mDbManager.queryCellInfoByBssid(data.getBssid());
                                if (cellInfos.size() != 0) {
                                    data.setCellInfo(cellInfos);
                                }
                            }
                        }
                    }
                }
            }
        }
        return checkResult;
    }

    public APInfoData getApInfoByBssid(String bssid) {
        synchronized (this.mLock) {
            if (bssid == null) {
                return null;
            }
            APInfoData apInfo = getApInfo(bssid);
            return apInfo;
        }
    }

    private APInfoData getApInfo(String bssid) {
        for (APInfoData info : this.mInfos) {
            if (info.getBssid().equals(bssid)) {
                return info;
            }
        }
        return null;
    }

    private boolean isCellIdExit(APInfoData info, String cellid) {
        for (CellInfoData data : info.getCellInfos()) {
            if (data.getCellid().equals(cellid)) {
                return true;
            }
        }
        return false;
    }

    private void setScanInterval(int scanType) {
        int scanInterval;
        switch (scanType) {
            case SCAN_TYPE_SHORT /*0*/:
                scanInterval = SCAN_INTERVAL_SHORT;
                break;
            case SCAN_TYPE_ONE_MIN /*1*/:
                scanInterval = SCAN_INTERVAL_NORMAL_1;
                break;
            case SCAN_TYPE_THREE_MIN /*2*/:
                scanInterval = SCAN_INTERVAL_NORMAL_3;
                resetScanAlwaysEnable();
                break;
            case SCAN_TYPE_FINE_MIN /*3*/:
                scanInterval = SCAN_INTERVAL_NORMAL_5;
                resetScanAlwaysEnable();
                break;
            default:
                scanInterval = SCAN_INTERVAL_NORMAL_1;
                break;
        }
        Log.e(MessageUtil.TAG, "setScanInterval scanInterval = " + scanInterval);
        this.mWifiHandler.sendEmptyMessageDelayed(SCAN_TYPE_FINE_MIN, (long) scanInterval);
    }

    private boolean handleScanResult(List<ScanResult> lists) {
        for (ScanResult result : lists) {
            if (isInTargetAp(result.BSSID) && !isInBlackList(result.BSSID) && result.level >= AUTO_OPEN_RSSI_VALUE) {
                Log.e(MessageUtil.TAG, "handleScanResult find target AP  result.BSSID = " + result.BSSID);
                this.mHandler.sendEmptyMessage(5);
                return true;
            }
        }
        return false;
    }

    private boolean isInTargetAp(String bssid) {
        List<APInfoData> targetDataList = this.mStateMachine.getTargetApInfoDatas();
        if (targetDataList == null || targetDataList.size() == 0) {
            return false;
        }
        for (APInfoData data : targetDataList) {
            if (data.getBssid().equals(bssid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMonitorCellId(String cellid) {
        Log.e(MessageUtil.TAG, "isMonitorCellId mInfos.size() = " + this.mInfos.size());
        synchronized (this.mLock) {
            if (!this.mIsRunning || cellid == null) {
                return false;
            }
            for (APInfoData info : this.mInfos) {
                if (isCellIdExit(info, cellid)) {
                    return true;
                }
            }
            return false;
        }
    }

    public List<APInfoData> getMonitorDatas(String cellid) {
        synchronized (this.mLock) {
            List<APInfoData> datas = new ArrayList();
            if (this.mIsRunning) {
                for (APInfoData info : this.mInfos) {
                    if (isCellIdExit(info, cellid)) {
                        datas.add(info);
                    }
                }
                return datas;
            }
            return datas;
        }
    }

    public boolean isInBlackList(String bssid) {
        synchronized (this.mLock) {
            if (bssid == null) {
                return false;
            }
            APInfoData data = getApInfo(bssid);
            if (data != null) {
                boolean isInBlackList = data.isInBlackList();
                return isInBlackList;
            }
            return false;
        }
    }

    public void delectApInfoByBssid(String bssid) {
        synchronized (this.mLock) {
            if (this.mIsRunning) {
                Log.e(MessageUtil.TAG, "delectApInfoByBssid mInfos.size = " + this.mInfos.size());
                APInfoData data = getApInfo(bssid);
                if (data != null) {
                    this.mDbManager.delAPInfos(bssid);
                    this.mInfos.remove(data);
                    Log.e(MessageUtil.TAG, "delectApInfoByBssid mInfos.size()=" + this.mInfos.size());
                }
                return;
            }
        }
    }

    public void delectApInfoBySsid(String ssid) {
        synchronized (this.mLock) {
            Log.e(MessageUtil.TAG, "getApInfoBySsid mInfos.size = " + this.mInfos.size());
            if (this.mIsRunning) {
                List<APInfoData> delList = new ArrayList();
                for (APInfoData info : this.mInfos) {
                    if (info.getSsid().equals(ssid) && !isInConfigNetworks(ssid, info.getAuthType())) {
                        Log.e(MessageUtil.TAG, "delectApInfoBySsid  ssid = " + ssid + "  info.getAuthType() = " + info.getAuthType());
                        this.mDbManager.delAPInfos(info.getBssid());
                        delList.add(info);
                    }
                }
                if (delList.size() > 0) {
                    for (APInfoData delInfo : delList) {
                        this.mInfos.remove(delInfo);
                    }
                }
                Log.e(MessageUtil.TAG, "delectApInfoBySsid mInfos.size()=" + this.mInfos.size());
                return;
            }
        }
    }

    public void delectApInfoBySsidForPortal(WifiInfo mConnectionInfo) {
        synchronized (this.mLock) {
            Log.e(MessageUtil.TAG, "delectApInfoBySsidForPortal mInfos.size = " + this.mInfos.size());
            if (this.mIsRunning) {
                if (mConnectionInfo != null) {
                    if (mConnectionInfo.getBSSID() != null) {
                        List<APInfoData> delList = new ArrayList();
                        for (APInfoData info : this.mInfos) {
                            if (info.getSsid().equals(mConnectionInfo.getSSID()) && info.getAuthType() == getAuthType(mConnectionInfo.getNetworkId())) {
                                Log.e(MessageUtil.TAG, "delectApInfoBySsid\tssid = " + info.getSsid() + "  info.getAuthType() = " + info.getAuthType());
                                this.mDbManager.delAPInfos(info.getBssid());
                                delList.add(info);
                            }
                        }
                        if (delList.size() > 0) {
                            for (APInfoData delInfo : delList) {
                                this.mInfos.remove(delInfo);
                            }
                        }
                        Log.e(MessageUtil.TAG, "delectApInfoBySsidForPortal mInfos.size()=" + this.mInfos.size());
                        return;
                    }
                }
                return;
            }
        }
    }

    private boolean isInMonitorNearbyAp(List<ScanResult> lists) {
        List<APInfoData> targetDataList = this.mStateMachine.getTargetApInfoDatas();
        if (targetDataList == null || targetDataList.size() == 0) {
            return false;
        }
        for (ScanResult data : lists) {
            for (APInfoData targetData : targetDataList) {
                if (targetData.getNearbyAPInfos().contains(data.BSSID)) {
                    return true;
                }
            }
        }
        return false;
    }

    private APInfoData getOldestApInfoData() {
        if (this.mInfos.size() <= 0) {
            return null;
        }
        APInfoData oldestData = (APInfoData) this.mInfos.get(SCAN_TYPE_SHORT);
        for (APInfoData data : this.mInfos) {
            if (data.getLastTime() < oldestData.getLastTime()) {
                oldestData = data;
            }
        }
        return oldestData;
    }

    public boolean handleAutoScanResult(List<ScanResult> lists) {
        Log.e(MessageUtil.TAG, "handleAutoScanResult enter");
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            Log.e(MessageUtil.TAG, "handleAutoScanResult configNetworks == null || configNetworks.size()");
            return false;
        }
        for (ScanResult scanResult : lists) {
            if (scanResult.capabilities != null) {
                String capStr = getCapString(scanResult.capabilities);
                for (WifiConfiguration config : configNetworks) {
                    boolean found = false;
                    String SSID = "\"" + scanResult.SSID + "\"";
                    if (config.SSID != null && config.SSID.equals(SSID) && !config.isTempCreated && isValid(config)) {
                        if (capStr.equals("NONE") && config.getAuthType() == 0) {
                            found = true;
                        } else if (config.configKey().contains(capStr)) {
                            found = true;
                        }
                        if (found) {
                            Log.e(MessageUtil.TAG, "handleAutoScanResult scanResult.SSID = " + scanResult.SSID);
                            return true;
                        }
                    }
                }
                continue;
            }
        }
        Log.e(MessageUtil.TAG, "handleAutoScanResult return false");
        return false;
    }

    public List<APInfoData> getDatasByCellId(String preCellID, String cellid) {
        List<APInfoData> datas;
        synchronized (this.mLock) {
            datas = new ArrayList();
            for (APInfoData info : this.mInfos) {
                if (isCellIdExit(info, preCellID) && isCellIdExit(info, cellid)) {
                    datas.add(info);
                }
            }
        }
        return datas;
    }

    private String getCapString(String cap) {
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

    public void resetBlackList(List<ScanResult> mLists, boolean isAddtoBlack) {
        synchronized (this.mLock) {
            if (mLists != null) {
                if (mLists.size() > 0) {
                    Log.e(MessageUtil.TAG, "processManualClose mLists.size() = " + mLists.size());
                    for (ScanResult result : mLists) {
                        APInfoData data = getApInfo(result.BSSID);
                        if (!(data == null || data.isInBlackList() == isAddtoBlack)) {
                            setBlackListBySsid(data.getSsid(), data.getAuthType(), isAddtoBlack);
                        }
                    }
                }
            }
            Log.e(MessageUtil.TAG, "processManualClose mLists = null");
        }
    }

    private void setBlackListBySsid(String ssid, int authtype, boolean isAddtoBlack) {
        int value = isAddtoBlack ? SCAN_TYPE_ONE_MIN : SCAN_TYPE_SHORT;
        for (APInfoData info : this.mInfos) {
            if (info.getSsid().equals(ssid) && info.getAuthType() == authtype && info.isInBlackList() != isAddtoBlack) {
                info.setBlackListFlag(isAddtoBlack);
                Log.e(MessageUtil.TAG, "setBlackListBySsid info.getBssid() = " + info.getBssid() + " info.getSsid() = " + info.getSsid() + "isAddtoBlack = " + isAddtoBlack);
                this.mDbManager.updateBssidIsInBlackList(info.getBssid(), value);
            }
        }
    }

    public void resetBlackListByBssid(String bssid, boolean isAddtoBlack) {
        synchronized (this.mLock) {
            if (bssid == null) {
                return;
            }
            int value = isAddtoBlack ? SCAN_TYPE_ONE_MIN : SCAN_TYPE_SHORT;
            APInfoData data = getApInfo(bssid);
            if (data != null) {
                Log.e(MessageUtil.TAG, "resetBlackListByBssid data.getBssid() = " + data.getBssid() + " data.getSsid() = " + data.getSsid() + "isAddtoBlack = " + isAddtoBlack);
                this.mDbManager.updateBssidIsInBlackList(data.getBssid(), value);
                data.setBlackListFlag(isAddtoBlack);
            }
        }
    }

    private boolean isValid(WifiConfiguration config) {
        boolean z = true;
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() > SCAN_TYPE_ONE_MIN) {
            z = false;
        }
        return z;
    }

    private boolean isInConfigNetworks(String ssid, int AuthType) {
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            return false;
        }
        for (WifiConfiguration mConfiguration : configNetworks) {
            if (mConfiguration != null && isValid(mConfiguration) && mConfiguration.status != SCAN_TYPE_ONE_MIN && mConfiguration.SSID != null && mConfiguration.SSID.equals(ssid) && mConfiguration.getAuthType() == AuthType) {
                return true;
            }
        }
        return false;
    }

    private int getAuthType(int networkId) {
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            return -1;
        }
        for (WifiConfiguration config : configs) {
            if (config != null && isValid(config) && networkId == config.networkId) {
                Log.d(MessageUtil.TAG, "getAuthType  networkId= " + networkId + " config.getAuthType() = " + config.getAuthType());
                return config.getAuthType();
            }
        }
        return -1;
    }

    public void resetAllBlackList() {
        synchronized (this.mLock) {
            for (APInfoData info : this.mInfos) {
                if (info.isInBlackList()) {
                    info.setBlackListFlag(false);
                    Log.e(MessageUtil.TAG, "resetAllBlackList info.ssid() = " + info.getSsid());
                    this.mDbManager.updateBssidIsInBlackList(info.getBssid(), SCAN_TYPE_SHORT);
                }
            }
        }
    }

    public boolean isHasTargetAp(List<ScanResult> lists) {
        for (ScanResult result : lists) {
            if (isInTargetAp(result.BSSID) && !isInBlackList(result.BSSID)) {
                Log.e(MessageUtil.TAG, "isHasTargetAp find target AP result.BSSID = " + result.BSSID);
                return true;
            }
        }
        return false;
    }
}
