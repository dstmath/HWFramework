package com.huawei.hwwifiproservice;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
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
    private boolean mIsScanInShort = false;
    private boolean mIsScaning = false;
    private final Object mLock = new Object();
    private int mScanTimes = 0;
    private int mScanTotleTimes = 20;
    private int mScanType = 1;
    private HwIntelligenceStateMachine mStateMachine;
    private Handler mWifiHandler = new Handler() {
        /* class com.huawei.hwwifiproservice.ApInfoManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 2) {
                HwHiLog.i(MessageUtil.TAG, false, "MSG_UPDATE_SCAN_RESULT", new Object[0]);
                List<ScanResult> mLists = ApInfoManager.this.mWifiManager.getScanResults();
                if (mLists != null) {
                    HwHiLog.i(MessageUtil.TAG, false, "mLists.size() = %{public}d", new Object[]{Integer.valueOf(mLists.size())});
                }
                if (!ApInfoManager.this.handleScanResult(mLists)) {
                    ApInfoManager.access$308(ApInfoManager.this);
                    if (ApInfoManager.this.mScanTimes <= ApInfoManager.this.mScanTotleTimes) {
                        if (mLists != null && ApInfoManager.this.isInMonitorNearbyAp(mLists) && !ApInfoManager.this.mIsScanInShort) {
                            ApInfoManager.this.mScanType = 0;
                            ApInfoManager.this.mIsScanInShort = true;
                        }
                        ApInfoManager apInfoManager = ApInfoManager.this;
                        apInfoManager.setScanInterval(apInfoManager.mScanType);
                    } else if (ApInfoManager.this.mScanType == 3) {
                        ApInfoManager.this.stopScanAp();
                    } else {
                        ApInfoManager.access$708(ApInfoManager.this);
                        ApInfoManager.this.mScanTimes = 0;
                        HwHiLog.i(MessageUtil.TAG, false, "MSG_UPDATE_SCAN_RESULT set scan Interval mScanType = %{public}d", new Object[]{Integer.valueOf(ApInfoManager.this.mScanType)});
                        ApInfoManager apInfoManager2 = ApInfoManager.this;
                        apInfoManager2.setScanInterval(apInfoManager2.mScanType);
                    }
                }
            } else if (i == 3) {
                HwHiLog.i(MessageUtil.TAG, false, "MSG_SCAN_AGAIN", new Object[0]);
                removeMessages(3);
                ApInfoManager.this.setScanAlwaysEnable();
            }
        }
    };
    private WifiManager mWifiManager;

    static /* synthetic */ int access$308(ApInfoManager x0) {
        int i = x0.mScanTimes;
        x0.mScanTimes = i + 1;
        return i;
    }

    static /* synthetic */ int access$708(ApInfoManager x0) {
        int i = x0.mScanType;
        x0.mScanType = i + 1;
        return i;
    }

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

    public List<Integer> queryAvailable5gChannelListByCellId(String cellid) {
        synchronized (this.mLock) {
            if (TextUtils.isEmpty(cellid)) {
                HwHiLog.d(MessageUtil.TAG, false, "ignore queryAvailable5gChannelListByCellId, cellid is null", new Object[0]);
                return new ArrayList();
            }
            if (this.mIsRunning) {
                if (this.mDbManager != null) {
                    return this.mDbManager.queryAvailable5gChannelListByCellId(cellid);
                }
            }
            HwHiLog.d(MessageUtil.TAG, false, "ignore queryAvailable5gChannelListByCellId, parameter invalid", new Object[0]);
            return new ArrayList();
        }
    }

    public void addCurrentApInfo(String cellid) {
        synchronized (this.mLock) {
            if (cellid != null) {
                inlineAddApInfo(cellid, this.mWifiManager.getConnectionInfo());
            }
        }
    }

    public void addTargetApInfo(String cellId, WifiInfo wifiInfo) {
        synchronized (this.mLock) {
            if (cellId != null) {
                inlineAddApInfo(cellId, wifiInfo);
            }
        }
    }

    private void inlineAddApInfo(String cellId, WifiInfo info) {
        APInfoData oldestData;
        if (this.mIsRunning) {
            HwHiLog.d(MessageUtil.TAG, false, "addCurrentApInfo cellid = %{private}s", new Object[]{cellId});
            if (info == null || info.getBSSID() == null || cellId == null || MessageUtil.ILLEGAL_BSSID_01.equals(info.getBSSID()) || MessageUtil.ILLEGAL_BSSID_02.equals(info.getBSSID())) {
                HwHiLog.e(MessageUtil.TAG, false, "inlineAddApInfo invalid AP info", new Object[0]);
                return;
            }
            APInfoData data = getApInfo(info.getBSSID());
            if (data == null) {
                HwHiLog.i(MessageUtil.TAG, false, "addCurrentApInfo addApInfos", new Object[0]);
                long mTime = System.currentTimeMillis();
                int mAuthType = getAuthType(info.getNetworkId());
                if (this.mInfos.size() == 499) {
                    this.mHwintelligenceWiFiCHR.uploadWhiteNum(500);
                }
                if (this.mInfos.size() >= 500 && (oldestData = getOldestApInfoData()) != null) {
                    this.mDbManager.delAPInfos(oldestData.getBssid());
                    this.mInfos.remove(oldestData);
                }
                this.mDbManager.addApInfos(info.getBSSID(), info.getSSID(), cellId, getAuthType(info.getNetworkId()), info.getFrequency());
                APInfoData mNewData = new APInfoData(info.getBSSID(), info.getSSID(), 0, mAuthType, mTime, 0, info.getFrequency());
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
                HwHiLog.i(MessageUtil.TAG, false, "addCurrentApInfo MessageUtil.DB_NEARBY_CELLID_MAX_QUANTA", new Object[0]);
                this.mDbManager.delAPInfos(data.getBssid());
                this.mInfos.remove(data);
            } else {
                int authtype = getAuthType(info.getNetworkId());
                if (!isCellIdExit(data, cellId)) {
                    HwHiLog.i(MessageUtil.TAG, false, "addCurrentApInfo addCellIdInfo", new Object[0]);
                    this.mDbManager.addCellInfo(info.getBSSID(), cellId);
                    this.mDbManager.addNearbyApInfo(info.getBSSID());
                    List<CellInfoData> cellInfos2 = this.mDbManager.queryCellInfoByBssid(data.getBssid());
                    if (cellInfos2.size() != 0) {
                        data.setCellInfo(cellInfos2);
                    }
                    List<String> nearbyApInfosList2 = this.mDbManager.getNearbyApInfo(data.getBssid());
                    if (nearbyApInfosList2.size() != 0) {
                        data.setNearbyAPInfos(nearbyApInfosList2);
                    }
                } else {
                    HwHiLog.i(MessageUtil.TAG, false, "addCurrentApInfo info is already there", new Object[0]);
                }
                this.mDbManager.updateBssidTimer(info.getBSSID());
                data.setLastTime(System.currentTimeMillis());
                if (!data.getSsid().equals(info.getSSID())) {
                    HwHiLog.d(MessageUtil.TAG, false, "inlineAddApInfo updateSsid  data.getSsid() = %{public}s  Info.getSSID() = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(data.getSsid()), StringUtilEx.safeDisplaySsid(info.getSSID())});
                    this.mDbManager.updateSsid(info.getBSSID(), info.getSSID());
                    data.setSsid(info.getSSID());
                }
                if (data.getAuthType() != authtype) {
                    HwHiLog.d(MessageUtil.TAG, false, "inlineAddApInfo updateSsid  data.getAuthType() = %{public}d  authtype = %{public}d", new Object[]{Integer.valueOf(data.getAuthType()), Integer.valueOf(authtype)});
                    this.mDbManager.updateAuthType(info.getBSSID(), authtype);
                    data.setAuthType(authtype);
                }
                int freq = info.getFrequency();
                if (data.getFrequency() != freq) {
                    HwHiLog.d(MessageUtil.TAG, false, "inlineAddApInfo updateChannel  data.getFrequency() = %{public}d  freq = %{public}d", new Object[]{Integer.valueOf(data.getFrequency()), Integer.valueOf(freq)});
                    this.mDbManager.updateChannel(info.getBSSID(), freq);
                    data.setFrequency(info.getFrequency());
                }
            }
            HwHiLog.i(MessageUtil.TAG, false, "inlineAddApInfo mInfos.size()=%{public}d", new Object[]{Integer.valueOf(this.mInfos.size())});
        }
    }

    public void updataApInfo(String cellid) {
        synchronized (this.mLock) {
            HwHiLog.d(MessageUtil.TAG, false, "updataApInfo cellid = %{private}s", new Object[]{cellid});
            if (this.mIsRunning) {
                if (cellid != null) {
                    inlineAddApInfo(cellid, this.mWifiManager.getConnectionInfo());
                }
            }
        }
    }

    public void startScanAp() {
        WifiManager wifiManager;
        HwHiLog.i(MessageUtil.TAG, false, "startScanAp mIsScaning = %{public}s", new Object[]{String.valueOf(this.mIsScaning)});
        if (this.mIsScaning || (wifiManager = this.mWifiManager) == null) {
            return;
        }
        if (!wifiManager.isWifiEnabled()) {
            this.mIsScaning = true;
            this.mScanTimes = 0;
            this.mScanType = 1;
            this.mIsScanAlwaysAvailable = this.mWifiManager.isScanAlwaysAvailable();
            HwHiLog.i(MessageUtil.TAG, false, "startScanAp isScanAlwaysAvailable  = %{public}s", new Object[]{String.valueOf(this.mIsScanAlwaysAvailable)});
            if (this.mIsScanAlwaysAvailable) {
                this.mWifiManager.startScan();
                return;
            }
            return;
        }
        HwHiLog.i(MessageUtil.TAG, false, "startScanAp wifi is opened", new Object[0]);
        stopScanAp();
    }

    public void stopScanAp() {
        HwHiLog.i(MessageUtil.TAG, false, "stopScanAp mIsScaning = %{public}s", new Object[]{String.valueOf(this.mIsScaning)});
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
    /* access modifiers changed from: public */
    private void setScanAlwaysEnable() {
        int i = this.mScanType;
        if (i == 3 || i == 2) {
            this.mIsScanAlwaysAvailable = this.mWifiManager.isScanAlwaysAvailable();
            HwHiLog.i(MessageUtil.TAG, false, "isScanAlwaysAvailable = %{public}s", new Object[]{String.valueOf(this.mIsScanAlwaysAvailable)});
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
        HwHiLog.d(MessageUtil.TAG, false, "processScanResult cellid = %{private}s", new Object[]{cellid});
        List<ScanResult> mLists = this.mWifiManager.getScanResults();
        if (mLists != null) {
            HwHiLog.i(MessageUtil.TAG, false, "mLists.size() = %{public}d", new Object[]{Integer.valueOf(mLists.size())});
        }
        boolean checkResult = false;
        synchronized (this.mLock) {
            if (mLists != null) {
                if (mLists.size() > 0) {
                    for (ScanResult result : mLists) {
                        APInfoData data = getApInfo(result.BSSID);
                        if (data != null) {
                            if (!isCellIdExit(data, cellid)) {
                                HwHiLog.i(MessageUtil.TAG, false, "addCurrentApInfo addCellIdInfo", new Object[0]);
                                this.mDbManager.addCellInfo(data.getBssid(), cellid);
                                checkResult = true;
                                List<CellInfoData> cellInfos = this.mDbManager.queryCellInfoByBssid(data.getBssid());
                                if (cellInfos.size() != 0) {
                                    data.setCellInfo(cellInfos);
                                }
                            } else {
                                HwHiLog.i(MessageUtil.TAG, false, "addCurrentApInfo info is already there", new Object[0]);
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
            return getApInfo(bssid);
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
    /* access modifiers changed from: public */
    private void setScanInterval(int scanType) {
        int scanInterval;
        if (scanType == 0) {
            scanInterval = SCAN_INTERVAL_SHORT;
        } else if (scanType == 1) {
            scanInterval = 60000;
        } else if (scanType == 2) {
            scanInterval = SCAN_INTERVAL_NORMAL_3;
            resetScanAlwaysEnable();
        } else if (scanType != 3) {
            scanInterval = 60000;
        } else {
            scanInterval = SCAN_INTERVAL_NORMAL_5;
            resetScanAlwaysEnable();
        }
        HwHiLog.i(MessageUtil.TAG, false, "setScanInterval scanInterval = %{public}d", new Object[]{Integer.valueOf(scanInterval)});
        this.mWifiHandler.sendEmptyMessageDelayed(3, (long) scanInterval);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleScanResult(List<ScanResult> lists) {
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
                HwHiLog.d(MessageUtil.TAG, false, "handleScanResult AP in balcklist SSID = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(data.SSID)});
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
                HwHiLog.d(MessageUtil.TAG, false, "handleScanResult hasTargetAp == true, SSID = %{public}s BSSID = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(data.SSID), WifiProCommonUtils.safeDisplayBssid(data.BSSID)});
            } else {
                HwHiLog.i(MessageUtil.TAG, false, "handleScanResult AP RSSI is weak SSID = %{public}s BSSID = %{public}s data.level = %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(data.SSID), WifiProCommonUtils.safeDisplayBssid(data.BSSID), Integer.valueOf(data.level)});
            }
        }
        if (hasApInBlackList) {
            HwHiLog.d(MessageUtil.TAG, false, "Has tartget in blacklist, update record.", new Object[0]);
            resetBlackList(lists, true);
            return true;
        } else if (!hasTargetAp) {
            return false;
        } else {
            HwHiLog.d(MessageUtil.TAG, false, "handleScanResult find target AP!", new Object[0]);
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

    public boolean isMonitorCellId(String cellid) {
        HwHiLog.i(MessageUtil.TAG, false, "isMonitorCellId mInfos.size() = %{public}d", new Object[]{Integer.valueOf(this.mInfos.size())});
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
            return false;
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
                return false;
            }
            APInfoData data = getApInfo(bssid);
            if (data == null) {
                return false;
            }
            return data.isInBlackList();
        }
    }

    public void deleteApInfoByBssid(String bssid) {
        synchronized (this.mLock) {
            if (this.mIsRunning) {
                HwHiLog.d(MessageUtil.TAG, false, "deleteApInfoByBssid mInfos.size = %{public}d", new Object[]{Integer.valueOf(this.mInfos.size())});
                APInfoData data = getApInfo(bssid);
                if (data != null) {
                    this.mDbManager.delAPInfos(bssid);
                    HwHiLog.d(MessageUtil.TAG, false, "deleteApInfo SSID = %{public}s ,BSSID = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(data.getSsid()), WifiProCommonUtils.safeDisplayBssid(bssid)});
                    this.mInfos.remove(data);
                    HwHiLog.i(MessageUtil.TAG, false, "deleteApInfoByBssid mInfos.size() = %{public}d", new Object[]{Integer.valueOf(this.mInfos.size())});
                }
            }
        }
    }

    public void delectApInfoBySsid(String ssid) {
        synchronized (this.mLock) {
            HwHiLog.i(MessageUtil.TAG, false, "getApInfoBySsid mInfos.size = %{public}d", new Object[]{Integer.valueOf(this.mInfos.size())});
            if (this.mIsRunning) {
                List<APInfoData> delList = new ArrayList<>();
                for (APInfoData info : this.mInfos) {
                    if (info.getSsid().equals(ssid) && !isInConfigNetworks(ssid, info.getAuthType())) {
                        HwHiLog.i(MessageUtil.TAG, false, "delectApInfoBySsid  ssid = %{public}s  info.getAuthType() = %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(ssid), Integer.valueOf(info.getAuthType())});
                        this.mDbManager.delAPInfos(info.getBssid());
                        delList.add(info);
                    }
                }
                if (delList.size() > 0) {
                    for (APInfoData delInfo : delList) {
                        this.mInfos.remove(delInfo);
                    }
                }
                HwHiLog.i(MessageUtil.TAG, false, "delectApInfoBySsid mInfos.size()=%{public}d", new Object[]{Integer.valueOf(this.mInfos.size())});
            }
        }
    }

    public void delectApInfoBySsidForPortal(WifiInfo mConnectionInfo) {
        synchronized (this.mLock) {
            HwHiLog.i(MessageUtil.TAG, false, "delectApInfoBySsidForPortal mInfos.size = %{public}d", new Object[]{Integer.valueOf(this.mInfos.size())});
            if (this.mIsRunning) {
                if (mConnectionInfo != null) {
                    if (mConnectionInfo.getBSSID() != null) {
                        List<APInfoData> delList = new ArrayList<>();
                        for (APInfoData info : this.mInfos) {
                            if (info.getSsid().equals(mConnectionInfo.getSSID()) && info.getAuthType() == getAuthType(mConnectionInfo.getNetworkId())) {
                                HwHiLog.i(MessageUtil.TAG, false, "delectApInfoBySsid ssid = %{public}s  info.getAuthType() = %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(info.getSsid()), Integer.valueOf(info.getAuthType())});
                                this.mDbManager.delAPInfos(info.getBssid());
                                delList.add(info);
                            }
                        }
                        if (delList.size() > 0) {
                            for (APInfoData delInfo : delList) {
                                this.mInfos.remove(delInfo);
                            }
                        }
                        HwHiLog.i(MessageUtil.TAG, false, "delectApInfoBySsidForPortal mInfos.size()=%{public}d", new Object[]{Integer.valueOf(this.mInfos.size())});
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInMonitorNearbyAp(List<ScanResult> lists) {
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
        HwHiLog.i(MessageUtil.TAG, false, "handleAutoScanResult enter", new Object[0]);
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            HwHiLog.e(MessageUtil.TAG, false, "handleAutoScanResult configNetworks == null || configNetworks.size()", new Object[0]);
            return false;
        }
        for (ScanResult scanResult : lists) {
            if (scanResult.capabilities != null) {
                String capStr = getCapString(scanResult.capabilities);
                for (WifiConfiguration config : configNetworks) {
                    boolean found = false;
                    String ssid = "\"" + scanResult.SSID + "\"";
                    if (config.SSID != null && config.SSID.equals(ssid) && !config.isTempCreated) {
                        if (!isValid(config)) {
                            found = true;
                        } else if (capStr.equals("NONE") && config.getAuthType() == 0) {
                            found = true;
                        } else if (config.configKey().contains(capStr)) {
                            found = true;
                        }
                        if (found) {
                            HwHiLog.i(MessageUtil.TAG, false, "handleAutoScanResult scanResult.SSID = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(scanResult.SSID)});
                            return true;
                        }
                    }
                }
                continue;
            }
        }
        HwHiLog.i(MessageUtil.TAG, false, "handleAutoScanResult return false", new Object[0]);
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
        if (cap == null) {
            return "NULL";
        }
        if (cap.contains("WEP")) {
            return "WEP";
        }
        if (cap.contains("PSK")) {
            return "PSK";
        }
        if (cap.contains("EAP")) {
            return "EAP";
        }
        return "NONE";
    }

    public void resetBlackList(List<ScanResult> mLists, boolean isAddtoBlack) {
        synchronized (this.mLock) {
            if (mLists != null) {
                if (mLists.size() > 0) {
                    HwHiLog.i(MessageUtil.TAG, false, "processManualClose mLists.size() = %{public}d", new Object[]{Integer.valueOf(mLists.size())});
                    for (ScanResult result : mLists) {
                        APInfoData data = getApInfo(result.BSSID);
                        if (!(data == null || data.isInBlackList() == isAddtoBlack)) {
                            setBlackListBySsid(data.getSsid(), data.getAuthType(), isAddtoBlack);
                        }
                    }
                }
            }
            HwHiLog.w(MessageUtil.TAG, false, "processManualClose mLists = null", new Object[0]);
        }
    }

    public void setBlackListBySsid(String ssid, int authtype, boolean isAddtoBlack) {
        for (APInfoData info : this.mInfos) {
            if (info.getSsid().equals(ssid) && info.getAuthType() == authtype && info.isInBlackList() != isAddtoBlack) {
                info.setBlackListFlag(isAddtoBlack);
                HwHiLog.d(MessageUtil.TAG, false, "setBlackListBySsid info.getBssid() = %{public}s info.getSsid() = %{public}s isAddtoBlack = %{public}s", new Object[]{WifiProCommonUtils.safeDisplayBssid(info.getBssid()), StringUtilEx.safeDisplaySsid(info.getSsid()), String.valueOf(isAddtoBlack)});
                this.mDbManager.updateBssidIsInBlackList(info.getBssid(), isAddtoBlack ? 1 : 0);
            }
        }
    }

    public void resetBlackListByBssid(String bssid, boolean isAddtoBlack) {
        synchronized (this.mLock) {
            if (bssid == null) {
                try {
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                int value = isAddtoBlack ? 1 : 0;
                APInfoData data = getApInfo(bssid);
                if (data != null) {
                    HwHiLog.d(MessageUtil.TAG, false, "resetBlackListByBssid data.getBssid() = %{public}s data.getSsid() = %{public}s isAddtoBlack = %{public}s", new Object[]{WifiProCommonUtils.safeDisplayBssid(data.getBssid()), StringUtilEx.safeDisplaySsid(data.getSsid()), String.valueOf(isAddtoBlack)});
                    this.mDbManager.updateBssidIsInBlackList(data.getBssid(), value);
                    data.setBlackListFlag(isAddtoBlack);
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

    private boolean isInConfigNetworks(String ssid, int authType) {
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            return false;
        }
        for (WifiConfiguration mConfiguration : configNetworks) {
            if (mConfiguration != null && isValid(mConfiguration) && mConfiguration.status != 1 && mConfiguration.SSID != null && mConfiguration.SSID.equals(ssid) && mConfiguration.getAuthType() == authType) {
                return true;
            }
        }
        return false;
    }

    private int getAuthType(int networkId) {
        List<WifiConfiguration> configs = WifiproUtils.getAllConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            return -1;
        }
        for (WifiConfiguration config : configs) {
            if (config != null && isValid(config) && networkId == config.networkId) {
                HwHiLog.d(MessageUtil.TAG, false, "getAuthType networkId= %{public}d config.getAuthType() = %{public}d", new Object[]{Integer.valueOf(networkId), Integer.valueOf(config.getAuthType())});
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
                    HwHiLog.i(MessageUtil.TAG, false, "resetAllBlackList info.ssid() = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(info.getSsid())});
                    this.mDbManager.updateBssidIsInBlackList(info.getBssid(), 0);
                }
            }
        }
    }

    public boolean isHasTargetAp(List<ScanResult> lists) {
        for (ScanResult result : lists) {
            String str = result.BSSID;
            if (isInTargetAp(str, "\"" + result.SSID + "\"") && !isInBlackList(result.BSSID)) {
                HwHiLog.d(MessageUtil.TAG, false, "isHasTargetAp find target AP result.BSSID = %{public}s", new Object[]{WifiProCommonUtils.safeDisplayBssid(result.BSSID)});
                return true;
            }
        }
        return false;
    }

    private void inlineUpdataApCellInfo(APInfoData data, String cellid) {
        if (!this.mIsRunning || data == null) {
            return;
        }
        if (!isCellIdExit(data, cellid)) {
            HwHiLog.i(MessageUtil.TAG, false, "addCurrentApInfo addCellIdInfo", new Object[0]);
            this.mDbManager.addCellInfo(data.getBssid(), cellid);
            List<CellInfoData> cellInfos = this.mDbManager.queryCellInfoByBssid(data.getBssid());
            if (cellInfos.size() != 0) {
                data.setCellInfo(cellInfos);
                return;
            }
            return;
        }
        HwHiLog.i(MessageUtil.TAG, false, "inlineUpdataApCellInfo info is already there", new Object[0]);
    }
}
