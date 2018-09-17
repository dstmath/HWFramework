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
    private List<APInfoData> mInfos = new ArrayList();
    private boolean mIsRunning = false;
    private boolean mIsScanAlwaysAvailable = false;
    private boolean mIsScanInShort = false;
    private boolean mIsScaning = false;
    private Object mLock = new Object();
    private int mScanTimes = 0;
    private int mScanTotleTimes = 20;
    private int mScanType = 1;
    private HwIntelligenceStateMachine mStateMachine;
    private Handler mWifiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    Log.e(MessageUtil.TAG, "MSG_UPDATE_SCAN_RESULT");
                    List<ScanResult> mLists = ApInfoManager.this.mWifiManager.getScanResults();
                    if (mLists != null) {
                        Log.e(MessageUtil.TAG, "mLists.size() = " + mLists.size());
                    }
                    if (!ApInfoManager.this.handleScanResult(mLists)) {
                        ApInfoManager apInfoManager = ApInfoManager.this;
                        apInfoManager.mScanTimes = apInfoManager.mScanTimes + 1;
                        if (ApInfoManager.this.mScanTimes <= ApInfoManager.this.mScanTotleTimes) {
                            if (!(mLists == null || !ApInfoManager.this.isInMonitorNearbyAp(mLists) || (ApInfoManager.this.mIsScanInShort ^ 1) == 0)) {
                                ApInfoManager.this.mScanType = 0;
                                ApInfoManager.this.mIsScanInShort = true;
                            }
                            ApInfoManager.this.setScanInterval(ApInfoManager.this.mScanType);
                            return;
                        } else if (ApInfoManager.this.mScanType == 3) {
                            ApInfoManager.this.stopScanAp();
                            return;
                        } else {
                            apInfoManager = ApInfoManager.this;
                            apInfoManager.mScanType = apInfoManager.mScanType + 1;
                            ApInfoManager.this.mScanTimes = 0;
                            Log.e(MessageUtil.TAG, "MSG_UPDATE_SCAN_RESULT set scan Interval mScanType = " + ApInfoManager.this.mScanType);
                            ApInfoManager.this.setScanInterval(ApInfoManager.this.mScanType);
                            return;
                        }
                    }
                    return;
                case 3:
                    Log.e(MessageUtil.TAG, "MSG_SCAN_AGAIN");
                    removeMessages(3);
                    ApInfoManager.this.setScanAlwaysEnable();
                    return;
                default:
                    return;
            }
        }
    };
    private WifiManager mWifiManager;

    public ApInfoManager(Context context, HwIntelligenceStateMachine stateMachine, Handler handler) {
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

    /* JADX WARNING: Missing block: B:8:0x000c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stop() {
        synchronized (this.mLock) {
            if (!this.mIsRunning || this.mDbManager == null) {
            } else {
                stopScanAp();
                this.mDbManager.closeDB();
                this.mIsRunning = false;
            }
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
            if (Info == null || Info.getBSSID() == null || cellid == null || Info.getBSSID().equals("any") || Info.getBSSID().equals(MessageUtil.ILLEGAL_BSSID_02)) {
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
                if (this.mInfos.size() >= 500) {
                    APInfoData oldestData = getOldestApInfoData();
                    if (oldestData != null) {
                        this.mDbManager.delAPInfos(oldestData.getBssid());
                        this.mInfos.remove(oldestData);
                    }
                }
                this.mDbManager.addApInfos(Info.getBSSID(), Info.getSSID(), cellid, getAuthType(Info.getNetworkId()));
                APInfoData mNewData = new APInfoData(Info.getBSSID(), Info.getSSID(), 0, mAuthType, mTime);
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

    /* JADX WARNING: Missing block: B:7:0x0024, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updataApInfo(String cellid) {
        synchronized (this.mLock) {
            Log.e(MessageUtil.TAG, "updataApInfo cellid =" + cellid);
            if (!this.mIsRunning || cellid == null) {
            } else {
                inlineAddCurrentApInfo(cellid);
            }
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
                this.mScanTimes = 0;
                this.mScanType = 1;
                this.mIsScanAlwaysAvailable = this.mWifiManager.isScanAlwaysAvailable();
                Log.w(MessageUtil.TAG, "startScanAp isScanAlwaysAvailable  = " + this.mIsScanAlwaysAvailable);
                if (this.mIsScanAlwaysAvailable) {
                    this.mWifiManager.startScan();
                } else {
                    Global.putInt(this.mContext.getContentResolver(), "wifi_pro_save_scan_always_enabled", 1);
                    Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 1);
                    this.mHandler.sendEmptyMessageDelayed(102, 2000);
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
        this.mScanTimes = 0;
        this.mScanType = 1;
        this.mWifiHandler.removeMessages(3);
    }

    public boolean isScaning() {
        return this.mIsScaning;
    }

    private void setScanAlwaysEnable() {
        if (this.mScanType == 3 || this.mScanType == 2) {
            this.mIsScanAlwaysAvailable = this.mWifiManager.isScanAlwaysAvailable();
            Log.e(MessageUtil.TAG, "isScanAlwaysAvailable  = " + this.mIsScanAlwaysAvailable);
            if (this.mIsScanAlwaysAvailable) {
                this.mWifiManager.startScan();
                return;
            }
            Global.putInt(this.mContext.getContentResolver(), "wifi_pro_save_scan_always_enabled", 1);
            Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 1);
            this.mHandler.sendEmptyMessageDelayed(102, 2000);
            return;
        }
        this.mWifiManager.startScan();
    }

    private void resetScanAlwaysEnable() {
        boolean isWiFiProEnable = Global.getInt(this.mContext.getContentResolver(), "wifi_pro_save_scan_always_enabled", 0) == 1;
        Log.e(MessageUtil.TAG, "resetScanAlwaysEnable isWiFiProEnable = " + isWiFiProEnable);
        if (isWiFiProEnable) {
            Global.putInt(this.mContext.getContentResolver(), "wifi_pro_save_scan_always_enabled", 0);
            Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0);
        }
    }

    public void updateScanResult() {
        if (this.mIsScaning) {
            this.mWifiHandler.sendEmptyMessage(2);
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
            case 0:
                scanInterval = 20000;
                break;
            case 1:
                scanInterval = 60000;
                break;
            case 2:
                scanInterval = SCAN_INTERVAL_NORMAL_3;
                resetScanAlwaysEnable();
                break;
            case 3:
                scanInterval = 300000;
                resetScanAlwaysEnable();
                break;
            default:
                scanInterval = 60000;
                break;
        }
        Log.e(MessageUtil.TAG, "setScanInterval scanInterval = " + scanInterval);
        this.mWifiHandler.sendEmptyMessageDelayed(3, (long) scanInterval);
    }

    private boolean handleScanResult(List<ScanResult> lists) {
        boolean hasApInBlackList = false;
        boolean hasTargetAp = false;
        if (lists == null) {
            return false;
        }
        for (ScanResult data : lists) {
            if (isInTargetAp(data.BSSID) || getApInfo(data.BSSID) != null) {
                if (isInBlackList(data.BSSID)) {
                    Log.d(MessageUtil.TAG, "handleScanResult AP in balcklist SSID = " + data.SSID);
                    hasApInBlackList = true;
                    break;
                }
                if (!isInTargetAp(data.BSSID)) {
                    APInfoData apInfo = getApInfo(data.BSSID);
                    String cellId = this.mStateMachine.getCellStateMonitor().getCurrentCellid();
                    if (!(apInfo == null || cellId == null)) {
                        inlineUpdataApCellInfo(apInfo, cellId);
                        List<APInfoData> targetDataList = this.mStateMachine.getTargetApInfoDatas();
                        if (targetDataList != null) {
                            targetDataList.add(apInfo);
                        }
                    }
                }
                if (data.level >= -75) {
                    hasTargetAp = true;
                } else {
                    Log.e(MessageUtil.TAG, "handleScanResult AP RSSI is weak SSID = " + data.SSID + " BSSID = " + partDisplayBssid(data.BSSID) + " data.level = " + data.level);
                }
            }
        }
        if (hasApInBlackList) {
            Log.d(MessageUtil.TAG, "Has tartget in blacklist, update record.");
            resetBlackList(lists, true);
            return true;
        } else if (!hasTargetAp) {
            return false;
        } else {
            Log.d(MessageUtil.TAG, "handleScanResult find target AP!");
            this.mHandler.sendEmptyMessage(5);
            return true;
        }
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

    /* JADX WARNING: Missing block: B:7:0x002b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isMonitorCellId(String cellid) {
        Log.e(MessageUtil.TAG, "isMonitorCellId mInfos.size() = " + this.mInfos.size());
        synchronized (this.mLock) {
            if (!this.mIsRunning || cellid == null) {
            } else {
                for (APInfoData info : this.mInfos) {
                    if (isCellIdExit(info, cellid)) {
                        return true;
                    }
                }
                return false;
            }
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

    /* JADX WARNING: Missing block: B:12:0x005a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            }
        }
    }

    public void delectApInfoBySsid(String ssid) {
        synchronized (this.mLock) {
            Log.e(MessageUtil.TAG, "getApInfoBySsid mInfos.size = " + this.mInfos.size());
            if (this.mIsRunning) {
                List<APInfoData> delList = new ArrayList();
                for (APInfoData info : this.mInfos) {
                    if (info.getSsid().equals(ssid) && (isInConfigNetworks(ssid, info.getAuthType()) ^ 1) != 0) {
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

    /* JADX WARNING: Missing block: B:12:0x0032, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void delectApInfoBySsidForPortal(WifiInfo mConnectionInfo) {
        synchronized (this.mLock) {
            Log.e(MessageUtil.TAG, "delectApInfoBySsidForPortal mInfos.size = " + this.mInfos.size());
            if (!this.mIsRunning) {
            } else if (mConnectionInfo != null) {
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
                }
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
        APInfoData oldestData = (APInfoData) this.mInfos.get(0);
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
                    if (config.SSID != null && (config.SSID.equals(SSID) ^ 1) == 0 && !config.isTempCreated && (isValid(config) ^ 1) == 0) {
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

    public void setBlackListBySsid(String ssid, int authtype, boolean isAddtoBlack) {
        int value = isAddtoBlack ? 1 : 0;
        for (APInfoData info : this.mInfos) {
            if (info.getSsid().equals(ssid) && info.getAuthType() == authtype && info.isInBlackList() != isAddtoBlack) {
                info.setBlackListFlag(isAddtoBlack);
                Log.e(MessageUtil.TAG, "setBlackListBySsid info.getBssid() = " + partDisplayBssid(info.getBssid()) + " info.getSsid() = " + info.getSsid() + "isAddtoBlack = " + isAddtoBlack);
                this.mDbManager.updateBssidIsInBlackList(info.getBssid(), value);
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0059, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void resetBlackListByBssid(String bssid, boolean isAddtoBlack) {
        synchronized (this.mLock) {
            if (bssid == null) {
                return;
            }
            int value = isAddtoBlack ? 1 : 0;
            APInfoData data = getApInfo(bssid);
            if (data != null) {
                Log.e(MessageUtil.TAG, "resetBlackListByBssid data.getBssid() = " + partDisplayBssid(data.getBssid()) + " data.getSsid() = " + data.getSsid() + "isAddtoBlack = " + isAddtoBlack);
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
        if (config.allowedKeyManagement.cardinality() > 1) {
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
            if (mConfiguration != null && (isValid(mConfiguration) ^ 1) == 0 && mConfiguration.status != 1 && mConfiguration.SSID != null && mConfiguration.SSID.equals(ssid) && mConfiguration.getAuthType() == AuthType) {
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
                    this.mDbManager.updateBssidIsInBlackList(info.getBssid(), 0);
                }
            }
        }
    }

    public boolean isHasTargetAp(List<ScanResult> lists) {
        for (ScanResult result : lists) {
            if (isInTargetAp(result.BSSID) && (isInBlackList(result.BSSID) ^ 1) != 0) {
                Log.e(MessageUtil.TAG, "isHasTargetAp find target AP result.BSSID = " + partDisplayBssid(result.BSSID));
                return true;
            }
        }
        return false;
    }

    private void inlineUpdataApCellInfo(APInfoData data, String cellid) {
        if (this.mIsRunning && data != null) {
            if (isCellIdExit(data, cellid)) {
                Log.e(MessageUtil.TAG, "inlineUpdataApCellInfo info is already there");
            } else {
                Log.e(MessageUtil.TAG, "addCurrentApInfo addCellIdInfo");
                this.mDbManager.addCellInfo(data.getBssid(), cellid);
                List<CellInfoData> cellInfos = this.mDbManager.queryCellInfoByBssid(data.getBssid());
                if (cellInfos.size() != 0) {
                    data.setCellInfo(cellInfos);
                }
            }
        }
    }

    private String partDisplayBssid(String srcBssid) {
        if (srcBssid == null) {
            return "null";
        }
        int len = srcBssid.length();
        if (len < 12) {
            return "Can not display bssid";
        }
        return srcBssid.substring(0, 9) + "**:**" + srcBssid.substring(len - 3, len);
    }
}
