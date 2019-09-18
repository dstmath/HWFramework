package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
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
    /* access modifiers changed from: private */
    public boolean mIsScanInShort = false;
    private boolean mIsScaning = false;
    private Object mLock = new Object();
    /* access modifiers changed from: private */
    public int mScanTimes = 0;
    /* access modifiers changed from: private */
    public int mScanTotleTimes = 20;
    /* access modifiers changed from: private */
    public int mScanType = 1;
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
                        int unused = ApInfoManager.this.mScanTimes = ApInfoManager.this.mScanTimes + 1;
                        if (ApInfoManager.this.mScanTimes <= ApInfoManager.this.mScanTotleTimes) {
                            if (mLists != null && ApInfoManager.this.isInMonitorNearbyAp(mLists) && !ApInfoManager.this.mIsScanInShort) {
                                int unused2 = ApInfoManager.this.mScanType = 0;
                                boolean unused3 = ApInfoManager.this.mIsScanInShort = true;
                            }
                            ApInfoManager.this.setScanInterval(ApInfoManager.this.mScanType);
                            return;
                        } else if (ApInfoManager.this.mScanType == 3) {
                            ApInfoManager.this.stopScanAp();
                            return;
                        } else {
                            int unused4 = ApInfoManager.this.mScanType = ApInfoManager.this.mScanType + 1;
                            int unused5 = ApInfoManager.this.mScanTimes = 0;
                            Log.e(MessageUtil.TAG, "MSG_UPDATE_SCAN_RESULT set scan Interval mScanType = " + ApInfoManager.this.mScanType);
                            ApInfoManager.this.setScanInterval(ApInfoManager.this.mScanType);
                            return;
                        }
                    } else {
                        return;
                    }
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
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;

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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001a, code lost:
        return;
     */
    public void stop() {
        synchronized (this.mLock) {
            if (this.mIsRunning) {
                if (this.mDbManager != null) {
                    stopScanAp();
                    this.mDbManager.closeDB();
                    this.mIsRunning = false;
                }
            }
        }
    }

    public void addCurrentApInfo(String cellid) {
        synchronized (this.mLock) {
            if (cellid == null) {
                try {
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                inlineAddCurrentApInfo(cellid);
            }
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
            if (data == null) {
                Log.e(MessageUtil.TAG, "addCurrentApInfo addApInfos");
                long mTime = System.currentTimeMillis();
                int mAuthType = getAuthType(Info.getNetworkId());
                if (this.mInfos.size() == 499) {
                    this.mHwintelligenceWiFiCHR.uploadWhiteNum(500);
                }
                if (this.mInfos.size() >= 500) {
                    APInfoData oldestData = getOldestApInfoData();
                    if (oldestData != null) {
                        this.mDbManager.delAPInfos(oldestData.getBssid());
                        this.mInfos.remove(oldestData);
                    }
                }
                this.mDbManager.addApInfos(Info.getBSSID(), Info.getSSID(), cellid, getAuthType(Info.getNetworkId()));
                APInfoData mNewData = new APInfoData(Info.getBSSID(), Info.getSSID(), 0, mAuthType, mTime, 0);
                List<CellInfoData> cellInfos = this.mDbManager.queryCellInfoByBssid(mNewData.getBssid());
                if (cellInfos.size() != 0) {
                    mNewData.setCellInfo(cellInfos);
                }
                List<String> nearbyApInfosList = this.mDbManager.getNearbyApInfo(mNewData.getBssid());
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
                if (!isCellIdExit(data, cellid)) {
                    Log.e(MessageUtil.TAG, "addCurrentApInfo addCellIdInfo");
                    this.mDbManager.addCellInfo(Info.getBSSID(), cellid);
                    this.mDbManager.addNearbyApInfo(Info.getBSSID());
                    List<CellInfoData> cellInfos2 = this.mDbManager.queryCellInfoByBssid(data.getBssid());
                    if (cellInfos2.size() != 0) {
                        data.setCellInfo(cellInfos2);
                    }
                    List<String> nearbyApInfosList2 = this.mDbManager.getNearbyApInfo(data.getBssid());
                    if (nearbyApInfosList2.size() != 0) {
                        data.setNearbyAPInfos(nearbyApInfosList2);
                    }
                } else {
                    Log.e(MessageUtil.TAG, "addCurrentApInfo info is already there");
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0026, code lost:
        return;
     */
    public void updataApInfo(String cellid) {
        synchronized (this.mLock) {
            Log.e(MessageUtil.TAG, "updataApInfo cellid =" + cellid);
            if (this.mIsRunning) {
                if (cellid != null) {
                    inlineAddCurrentApInfo(cellid);
                }
            }
        }
    }

    public void updateCurrentApHomebySsid(String ssid, int authtype, boolean isHome) {
        int value = isHome;
        Log.d(MessageUtil.TAG, "updateCurrentApHomebySsid ssid = " + ssid + ", isHome = " + isHome);
        for (APInfoData info : this.mInfos) {
            if (info.getSsid().equals(ssid) && info.getAuthType() == authtype && info.isHomeAp() != isHome) {
                info.setHomeAp(isHome);
                this.mDbManager.updateBssidIsHome(info.getBssid(), (int) value);
            }
        }
    }

    public void startScanAp() {
        Log.e(MessageUtil.TAG, "startScanAp mIsScaning = " + this.mIsScaning);
        if (!this.mIsScaning && this.mWifiManager != null) {
            if (!this.mWifiManager.isWifiEnabled()) {
                this.mIsScaning = true;
                this.mScanTimes = 0;
                this.mScanType = 1;
                this.mIsScanAlwaysAvailable = this.mWifiManager.isScanAlwaysAvailable();
                Log.w(MessageUtil.TAG, "startScanAp isScanAlwaysAvailable  = " + this.mIsScanAlwaysAvailable);
                if (this.mIsScanAlwaysAvailable) {
                    this.mWifiManager.startScan();
                }
            } else {
                Log.e(MessageUtil.TAG, "startScanAp wifi is opened");
                stopScanAp();
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

    /* access modifiers changed from: private */
    public void setScanAlwaysEnable() {
        if (this.mScanType == 3 || this.mScanType == 2) {
            this.mIsScanAlwaysAvailable = this.mWifiManager.isScanAlwaysAvailable();
            Log.e(MessageUtil.TAG, "isScanAlwaysAvailable  = " + this.mIsScanAlwaysAvailable);
            if (this.mIsScanAlwaysAvailable) {
                this.mWifiManager.startScan();
                return;
            }
            return;
        }
        this.mWifiManager.startScan();
    }

    private void resetScanAlwaysEnable() {
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
                try {
                    if (mLists.size() > 0) {
                        for (ScanResult result : mLists) {
                            APInfoData data = getApInfo(result.BSSID);
                            if (data != null) {
                                if (!isCellIdExit(data, cellid)) {
                                    Log.e(MessageUtil.TAG, "addCurrentApInfo addCellIdInfo");
                                    this.mDbManager.addCellInfo(data.getBssid(), cellid);
                                    checkResult = true;
                                    List<CellInfoData> cellInfos = this.mDbManager.queryCellInfoByBssid(data.getBssid());
                                    if (cellInfos.size() != 0) {
                                        data.setCellInfo(cellInfos);
                                    }
                                } else {
                                    Log.e(MessageUtil.TAG, "addCurrentApInfo info is already there");
                                }
                            }
                        }
                    }
                } finally {
                }
            }
        }
        return checkResult;
    }

    public APInfoData getApInfoByBssid(String bssid) {
        synchronized (this.mLock) {
            if (bssid == null) {
                try {
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                APInfoData apInfo = getApInfo(bssid);
                return apInfo;
            }
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

    /* access modifiers changed from: private */
    public void setScanInterval(int scanType) {
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

    /* access modifiers changed from: private */
    public boolean handleScanResult(List<ScanResult> lists) {
        boolean hasApInBlackList = false;
        boolean hasTargetAp = false;
        if (lists == null) {
            return false;
        }
        Iterator<ScanResult> it = lists.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ScanResult data = it.next();
            APInfoData apInfo = getApInfo(data.BSSID);
            String str = data.BSSID;
            if (!isInTargetAp(str, "\"" + data.SSID + "\"")) {
                if (!(apInfo == null || apInfo.getSsid() == null)) {
                    String ssid = apInfo.getSsid();
                    if (!ssid.equals("\"" + data.SSID + "\"")) {
                        continue;
                    }
                }
            }
            if (isInBlackList(data.BSSID)) {
                Log.d(MessageUtil.TAG, "handleScanResult AP in balcklist SSID = " + data.SSID);
                hasApInBlackList = true;
                break;
            }
            String str2 = data.BSSID;
            if (!isInTargetAp(str2, "\"" + data.SSID + "\"")) {
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

    private boolean isInTargetAp(String bssid, String ssid) {
        List<APInfoData> targetDataList = this.mStateMachine.getTargetApInfoDatas();
        if (targetDataList == null || targetDataList.size() == 0) {
            return false;
        }
        for (APInfoData data : targetDataList) {
            if (data.getBssid().equals(bssid) && data.getSsid() != null && data.getSsid().equals(ssid)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0046, code lost:
        return false;
     */
    public boolean isMonitorCellId(String cellid) {
        Log.e(MessageUtil.TAG, "isMonitorCellId mInfos.size() = " + this.mInfos.size());
        synchronized (this.mLock) {
            if (this.mIsRunning) {
                if (cellid != null) {
                    for (APInfoData info : this.mInfos) {
                        if (isCellIdExit(info, cellid)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
    }

    public List<APInfoData> getMonitorDatas(String cellid) {
        synchronized (this.mLock) {
            List<APInfoData> datas = new ArrayList<>();
            if (!this.mIsRunning) {
                return datas;
            }
            for (APInfoData info : this.mInfos) {
                if (isCellIdExit(info, cellid)) {
                    datas.add(info);
                }
            }
            return datas;
        }
    }

    public boolean isInBlackList(String bssid) {
        synchronized (this.mLock) {
            if (bssid == null) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                APInfoData data = getApInfo(bssid);
                if (data == null) {
                    return false;
                }
                boolean isInBlackList = data.isInBlackList();
                return isInBlackList;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0052, code lost:
        return;
     */
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
                List<APInfoData> delList = new ArrayList<>();
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
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00d0, code lost:
        return;
     */
    public void delectApInfoBySsidForPortal(WifiInfo mConnectionInfo) {
        synchronized (this.mLock) {
            Log.e(MessageUtil.TAG, "delectApInfoBySsidForPortal mInfos.size = " + this.mInfos.size());
            if (this.mIsRunning) {
                if (mConnectionInfo != null) {
                    if (mConnectionInfo.getBSSID() != null) {
                        List<APInfoData> delList = new ArrayList<>();
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
    }

    /* access modifiers changed from: private */
    public boolean isInMonitorNearbyAp(List<ScanResult> lists) {
        List<APInfoData> targetDataList = this.mStateMachine.getTargetApInfoDatas();
        if (targetDataList == null || targetDataList.size() == 0) {
            return false;
        }
        for (ScanResult data : lists) {
            Iterator<APInfoData> it = targetDataList.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (it.next().getNearbyAPInfos().contains(data.BSSID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private APInfoData getOldestApInfoData() {
        if (this.mInfos.size() <= 0) {
            return null;
        }
        APInfoData oldestData = this.mInfos.get(0);
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
                    if (config.SSID != null && config.SSID.equals(SSID) && !config.isTempCreated) {
                        if (!isValid(config)) {
                            found = true;
                        } else if (capStr.equals("NONE") && config.getAuthType() == 0) {
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
            datas = new ArrayList<>();
            for (APInfoData info : this.mInfos) {
                if (isCellIdExit(info, preCellID) && isCellIdExit(info, cellid)) {
                    datas.add(info);
                }
            }
        }
        return datas;
    }

    private String getCapString(String cap) {
        String capStr;
        if (cap == null) {
            return "NULL";
        }
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
                try {
                    if (mLists.size() > 0) {
                        Log.e(MessageUtil.TAG, "processManualClose mLists.size() = " + mLists.size());
                        for (ScanResult result : mLists) {
                            APInfoData data = getApInfo(result.BSSID);
                            if (!(data == null || data.isInBlackList() == isAddtoBlack)) {
                                setBlackListBySsid(data.getSsid(), data.getAuthType(), isAddtoBlack);
                            }
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            Log.e(MessageUtil.TAG, "processManualClose mLists = null");
        }
    }

    public void setBlackListBySsid(String ssid, int authtype, boolean isAddtoBlack) {
        int value = isAddtoBlack;
        for (APInfoData info : this.mInfos) {
            if (info.getSsid().equals(ssid) && info.getAuthType() == authtype && info.isInBlackList() != isAddtoBlack) {
                info.setBlackListFlag(isAddtoBlack);
                Log.e(MessageUtil.TAG, "setBlackListBySsid info.getBssid() = " + partDisplayBssid(info.getBssid()) + " info.getSsid() = " + info.getSsid() + "isAddtoBlack = " + isAddtoBlack);
                this.mDbManager.updateBssidIsInBlackList(info.getBssid(), (int) value);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004f, code lost:
        return;
     */
    public void resetBlackListByBssid(String bssid, boolean isAddtoBlack) {
        synchronized (this.mLock) {
            if (bssid == null) {
                try {
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                int value = isAddtoBlack;
                APInfoData data = getApInfo(bssid);
                if (data != null) {
                    Log.e(MessageUtil.TAG, "resetBlackListByBssid data.getBssid() = " + partDisplayBssid(data.getBssid()) + " data.getSsid() = " + data.getSsid() + "isAddtoBlack = " + isAddtoBlack);
                    this.mDbManager.updateBssidIsInBlackList(data.getBssid(), (int) value);
                    data.setBlackListFlag(isAddtoBlack);
                }
            }
        }
    }

    private boolean isValid(WifiConfiguration config) {
        boolean z = false;
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() <= 1) {
            z = true;
        }
        return z;
    }

    private boolean isInConfigNetworks(String ssid, int AuthType) {
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            return false;
        }
        for (WifiConfiguration mConfiguration : configNetworks) {
            if (mConfiguration != null && isValid(mConfiguration) && mConfiguration.status != 1 && mConfiguration.SSID != null && mConfiguration.SSID.equals(ssid) && mConfiguration.getAuthType() == AuthType) {
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
            String str = result.BSSID;
            if (isInTargetAp(str, "\"" + result.SSID + "\"") && !isInBlackList(result.BSSID)) {
                Log.e(MessageUtil.TAG, "isHasTargetAp find target AP result.BSSID = " + partDisplayBssid(result.BSSID));
                return true;
            }
        }
        return false;
    }

    private void inlineUpdataApCellInfo(APInfoData data, String cellid) {
        if (this.mIsRunning && data != null) {
            if (!isCellIdExit(data, cellid)) {
                Log.e(MessageUtil.TAG, "addCurrentApInfo addCellIdInfo");
                this.mDbManager.addCellInfo(data.getBssid(), cellid);
                List<CellInfoData> cellInfos = this.mDbManager.queryCellInfoByBssid(data.getBssid());
                if (cellInfos.size() != 0) {
                    data.setCellInfo(cellInfos);
                }
            } else {
                Log.e(MessageUtil.TAG, "inlineUpdataApCellInfo info is already there");
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
