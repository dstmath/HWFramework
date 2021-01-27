package com.android.server.wifi.cast;

import android.app.AlarmManager;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.wifi.HwHiLog;
import android.widget.Toast;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.ScanResultMatchInfo;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiConfigManagerUtils;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.cast.P2pSharing.P2pSharingDispatcher;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CastOptGcActioner {
    private static final int GC_CAST_OPTIMIZATION_RETRY_MAX_CNT = 1;
    private static final int GC_CAST_OPTIMIZATION_SCAN_FAIL_MAX_CNT = 5;
    private static final int PERIODIC_SCAN_INTERVAL_MS = 3000;
    private static final int[] PERMANENT_SCAN_FREQUENCYS = {2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472};
    private static final int ROAM_TO_24G_CHANNEL_NETWORK_BASE_SCORE = 40;
    private static final int ROAM_TO_5G_CHANNEL_NETWORK_BASE_SCORE = 80;
    private static final int SIGNAL_LEVEL_HRESHOLD = 3;
    private static final int SIGNAL_LEVEL_STEP_SCORE = 5;
    private static final int SWITCH_TO_24G_CHANNEL_NETWORK_BASE_SCORE = 20;
    private static final int SWITCH_TO_5G_CHANNEL_NETWORK_BASE_SCORE = 60;
    private static final String TAG = "CastOptGcActioner";
    private static CastOptGcActioner sCastOptGcActioner = null;
    private AlarmManager mAlarmManager = null;
    private CastOptMonitor mCastOptMonitor = null;
    private ClientModeImpl mClientModeImpl = null;
    private Context mContext = null;
    private int mGcCastOptRetryCnt;
    private int mGcCastOptScanFailCnt;
    private Handler mHandler;
    private Looper mLooper = null;
    private PeriodicScanAlarmListener mPeriodicScanTimerListener;
    private boolean mPeriodicScanTimerSet;
    private List<Integer> mPermanentScanFrequencys;
    private CastOptScanListener mScanListener = null;
    private WifiScanner.ScanSettings mScanSettings = null;
    private WifiScanner mScanner = null;
    private WifiConfigManager mWifiConfigManager = null;
    private WifiInjector mWifiInjector = null;
    private WifiManager mWifiManager = null;

    static /* synthetic */ int access$208(CastOptGcActioner x0) {
        int i = x0.mGcCastOptScanFailCnt;
        x0.mGcCastOptScanFailCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$510(CastOptGcActioner x0) {
        int i = x0.mGcCastOptRetryCnt;
        x0.mGcCastOptRetryCnt = i - 1;
        return i;
    }

    private CastOptGcActioner(Context context, Looper looper, CastOptMonitor castOptMonitor) {
        this.mGcCastOptRetryCnt = 0;
        this.mGcCastOptScanFailCnt = 0;
        this.mPeriodicScanTimerSet = false;
        this.mPermanentScanFrequencys = new ArrayList();
        this.mContext = context;
        this.mLooper = looper;
        this.mHandler = new Handler(looper);
        this.mCastOptMonitor = castOptMonitor;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mWifiInjector = WifiInjector.getInstance();
        this.mClientModeImpl = this.mWifiInjector.getClientModeImpl();
        this.mWifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        this.mScanner = this.mWifiInjector.getWifiScanner();
        this.mScanSettings = new WifiScanner.ScanSettings();
        WifiScanner.ScanSettings scanSettings = this.mScanSettings;
        scanSettings.type = 2;
        scanSettings.band = 7;
        scanSettings.reportEvents = 1;
        for (int channel : PERMANENT_SCAN_FREQUENCYS) {
            this.mPermanentScanFrequencys.add(Integer.valueOf(channel));
        }
        this.mScanListener = new CastOptScanListener();
        this.mPeriodicScanTimerListener = new PeriodicScanAlarmListener();
    }

    /* access modifiers changed from: private */
    public class CastOptScanListener implements WifiScanner.ScanListener {
        private CastOptScanListener() {
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            CastOptGcActioner.access$208(CastOptGcActioner.this);
            HwHiLog.e(CastOptGcActioner.TAG, false, "scan failure received. reason: %{public}d, scan fail cnt:%{public}d", new Object[]{Integer.valueOf(reason), Integer.valueOf(CastOptGcActioner.this.mGcCastOptScanFailCnt)});
            if (CastOptGcActioner.this.mGcCastOptScanFailCnt >= 5) {
                if (CastOptGcActioner.this.shouldDoGcOptimization()) {
                    CastOptGcActioner.this.gcCastOptAction(new ScanResult[0]);
                }
                CastOptGcActioner.this.mGcCastOptScanFailCnt = 0;
                return;
            }
            if (CastOptGcActioner.this.mGcCastOptRetryCnt > 0) {
                CastOptGcActioner.access$510(CastOptGcActioner.this);
            }
            CastOptGcActioner.this.scheduleNextPeriodicScan();
        }

        public void onResults(WifiScanner.ScanData[] scanDatas) {
            HwHiLog.i(CastOptGcActioner.TAG, false, "get scan result, start gc cast optimization", new Object[0]);
            CastOptGcActioner.this.mGcCastOptScanFailCnt = 0;
            if (scanDatas.length != 1) {
                HwHiLog.e(CastOptGcActioner.TAG, false, "found more than 1 batch of scan results, ignoring", new Object[0]);
                return;
            }
            ScanResult[] scanResults = scanDatas[0].getResults();
            if (CastOptGcActioner.this.shouldDoGcOptimization()) {
                CastOptGcActioner.this.gcCastOptAction(scanResults);
            }
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPeriodChanged(int periodInMs) {
        }
    }

    /* access modifiers changed from: private */
    public class PeriodicScanAlarmListener implements AlarmManager.OnAlarmListener {
        private PeriodicScanAlarmListener() {
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            CastOptGcActioner.this.startScan();
        }
    }

    protected static CastOptGcActioner createCastOptGcActioner(Context context, Looper looper, CastOptMonitor castOptMonitor) {
        if (sCastOptGcActioner == null) {
            sCastOptGcActioner = new CastOptGcActioner(context, looper, castOptMonitor);
        }
        return sCastOptGcActioner;
    }

    protected static CastOptGcActioner getInstance() {
        return sCastOptGcActioner;
    }

    /* access modifiers changed from: protected */
    public void startScan() {
        if (this.mScanner == null || this.mWifiConfigManager == null) {
            HwHiLog.e(TAG, false, "error, startScan param is null", new Object[0]);
        } else if (shouldDoGcOptimization()) {
            HwHiLog.i(TAG, false, "start scan, 2.4G + p2p channel: %{public}d", new Object[]{Integer.valueOf(this.mCastOptMonitor.getP2pFrequency())});
            List<WifiScanner.ScanSettings.HiddenNetwork> hiddenNetworkList = this.mWifiConfigManager.retrieveHiddenNetworkList();
            if (hiddenNetworkList != null) {
                this.mScanSettings.hiddenNetworks = (WifiScanner.ScanSettings.HiddenNetwork[]) hiddenNetworkList.toArray(new WifiScanner.ScanSettings.HiddenNetwork[hiddenNetworkList.size()]);
            }
            WifiScanner.ScanSettings scanSettings = this.mScanSettings;
            scanSettings.band = 0;
            scanSettings.reportEvents = 3;
            scanSettings.numBssidsPerScan = 0;
            scanSettings.channels = getScanfrequencys();
            this.mScanner.startScan(this.mScanSettings, this.mScanListener, new WorkSource((int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM));
        }
    }

    /* access modifiers changed from: protected */
    public WifiScanner.ChannelSpec[] getScanfrequencys() {
        int p2pFreq = this.mCastOptMonitor.getP2pFrequency();
        List<Integer> frequencyList = new ArrayList<>();
        frequencyList.addAll(this.mPermanentScanFrequencys);
        if (p2pFreq != 0) {
            frequencyList.add(Integer.valueOf(p2pFreq));
        }
        WifiScanner.ChannelSpec[] channels = new WifiScanner.ChannelSpec[frequencyList.size()];
        for (int i = 0; i < frequencyList.size(); i++) {
            channels[i] = new WifiScanner.ChannelSpec(frequencyList.get(i).intValue());
        }
        return channels;
    }

    /* access modifiers changed from: protected */
    public void resetGcCastOptCfg() {
        cancelPeriodicScans();
        this.mGcCastOptRetryCnt = 0;
        this.mGcCastOptScanFailCnt = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldDoGcOptimization() {
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (this.mWifiManager == null || castOptManager == null) {
            HwHiLog.e(TAG, false, "error, shouldDoGcOptimization param is null", new Object[0]);
            return false;
        } else if (!castOptManager.isCastOptScenes()) {
            HwHiLog.i(TAG, false, "out of scenes, do not need gc optimization", new Object[0]);
            return false;
        } else if (!this.mCastOptMonitor.isP2pConnected()) {
            HwHiLog.i(TAG, false, "p2p is disconnect, do not need gc optimization", new Object[0]);
            return false;
        } else if (!this.mCastOptMonitor.isStaConnected()) {
            HwHiLog.i(TAG, false, "sta is disconnect, do not need gc optimization", new Object[0]);
            return false;
        } else if (this.mCastOptMonitor.isP2pGroupOwner()) {
            HwHiLog.i(TAG, false, "p2p is GO, do not need gc optimization", new Object[0]);
            return false;
        } else {
            int p2pFreq = this.mCastOptMonitor.getP2pFrequency();
            if (!ScanResult.is5GHz(p2pFreq)) {
                HwHiLog.i(TAG, false, "p2p is not 5G, do not need gc optimization, p2p freq: %{public}d", new Object[]{Integer.valueOf(p2pFreq)});
                return false;
            }
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            if (wifiInfo == null || (!ScanResult.is24GHz(wifiInfo.getFrequency()) && wifiInfo.getFrequency() != p2pFreq)) {
                return true;
            }
            HwHiLog.i(TAG, false, "wifi is not cast mode, do not need gc optimization, p2p freq: %{public}d, sta freq: %{public}d", new Object[]{Integer.valueOf(p2pFreq), Integer.valueOf(wifiInfo.getFrequency())});
            return false;
        }
    }

    private void showToast() {
        final String showInfo = this.mContext.getResources().getString(33685914);
        HwHiLog.i(TAG, false, "showToast + = %{public}s ", new Object[]{showInfo});
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            /* class com.android.server.wifi.cast.CastOptGcActioner.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(CastOptGcActioner.this.mContext, showInfo, 0).show();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void updateWifiConfig(Bundle wifiConfig) {
        if (wifiConfig != null) {
            int securityType = wifiConfig.getInt("securityType");
            if (securityType == 0 || securityType == 2 || securityType == 4) {
                String ssid = CastOptUtils.createQuotedSsid(wifiConfig.getString("ssid"));
                List<WifiConfiguration> configNetworks = this.mWifiConfigManager.getConfiguredNetworks();
                if (configNetworks != null) {
                    for (WifiConfiguration wifiConfiguration : configNetworks) {
                        if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals(ssid) && securityType == ScanResultMatchInfo.getNetworkType(wifiConfiguration)) {
                            HwHiLog.i(TAG, false, "saved network, do nothing", new Object[0]);
                            return;
                        }
                    }
                }
                WifiConfiguration config = new WifiConfiguration();
                config.SSID = ssid;
                if (securityType != 0) {
                    config.preSharedKey = "\"" + wifiConfig.getString("password") + "\"";
                } else {
                    config.allowedKeyManagement.set(0);
                }
                config.noInternetAccess = !wifiConfig.getBoolean("hasInternet");
                this.mWifiManager.save(config, null);
                showToast();
                updateNoInternetAccess(config.SSID, securityType, config.noInternetAccess);
                CastOptChr castOptChr = CastOptChr.getInstance();
                if (castOptChr != null) {
                    castOptChr.handleAddGoWifiConfig();
                    return;
                }
                return;
            }
            HwHiLog.i(TAG, false, "securityType is not open or wpk", new Object[0]);
        }
    }

    private void updateNoInternetAccess(String ssid, int securityType, boolean noInternetAccess) {
        List<WifiConfiguration> configNetworks = this.mWifiConfigManager.getConfiguredNetworks();
        int networkId = -1;
        if (configNetworks != null) {
            Iterator<WifiConfiguration> it = configNetworks.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                WifiConfiguration wifiConfiguration = it.next();
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals(ssid) && securityType == ScanResultMatchInfo.getNetworkType(wifiConfiguration)) {
                    networkId = wifiConfiguration.networkId;
                    break;
                }
            }
        }
        if (networkId != -1) {
            HwHiLog.i(TAG, false, "updateNoInternetAccess networkId is  %{public}d", new Object[]{Integer.valueOf(networkId)});
            WifiConfigManagerUtils wifiConfigManagerUtils = EasyInvokeFactory.getInvokeUtils(WifiConfigManagerUtils.class);
            if (wifiConfigManagerUtils == null) {
                HwHiLog.e(TAG, false, "updateNoInternetAccess wifiConfigManagerUtils is null", new Object[0]);
                return;
            }
            WifiConfiguration wifiConfiguration2 = wifiConfigManagerUtils.getInternalConfiguredNetwork(this.mWifiConfigManager, networkId);
            if (wifiConfiguration2 != null) {
                wifiConfiguration2.noInternetAccess = noInternetAccess;
                return;
            }
            return;
        }
        HwHiLog.e(TAG, false, "updateNoInternetAccess can not find target wificonfig", new Object[0]);
    }

    private boolean isScanResultValid(ScanResult scanResult, WifiInfo wifiInfo, int p2pFreq) {
        if (scanResult == null) {
            return false;
        }
        if (ScanResult.is5GHz(scanResult.frequency) && scanResult.frequency != p2pFreq) {
            return false;
        }
        if (wifiInfo.getBSSID() == null || !wifiInfo.getBSSID().equals(scanResult.BSSID)) {
            return true;
        }
        return false;
    }

    private boolean isSavedNetworkValid(WifiInfo wifiInfo, WifiConfiguration curWifiConfig, WifiConfiguration targetWifiConfig, ScanResult targetScanResult) {
        if (curWifiConfig == null || targetWifiConfig == null || targetScanResult == null) {
            HwHiLog.e(TAG, false, "error, isSavedNetworkValid param is invalid", new Object[0]);
            return false;
        }
        int targetSignalLevel = WifiProCommonUtils.getSignalLevel(targetScanResult.frequency, targetScanResult.level);
        int curSignalLevel = WifiProCommonUtils.getSignalLevel(wifiInfo.getFrequency(), wifiInfo.getRssi());
        HwHiLog.i(TAG, false, "target network info, ssid: %{public}s, freq: %{public}d, signalLevel: %{public}d, portalNetwork: %{public}s, noInternetAccess: %{public}s, ", new Object[]{StringUtilEx.safeDisplaySsid(targetWifiConfig.SSID), Integer.valueOf(targetScanResult.frequency), Integer.valueOf(targetSignalLevel), String.valueOf(targetWifiConfig.portalNetwork), String.valueOf(targetWifiConfig.noInternetAccess)});
        if ((targetSignalLevel < 3 && targetSignalLevel < curSignalLevel) || targetWifiConfig.portalNetwork) {
            return false;
        }
        if (curWifiConfig.noInternetAccess || !targetWifiConfig.noInternetAccess) {
            return true;
        }
        return false;
    }

    private int calculateNeworkScore(WifiConfiguration curWifiConfig, WifiConfiguration targetWifiConfig, int p2pFreq, ScanResult targetScanResult) {
        int networkScore;
        int signalLevel = WifiProCommonUtils.getSignalLevel(targetScanResult.frequency, targetScanResult.level);
        if (ScanResult.is5GHz(targetScanResult.frequency)) {
            if (p2pFreq != targetScanResult.frequency) {
                return 0;
            }
            if (curWifiConfig.networkId == targetWifiConfig.networkId) {
                networkScore = ROAM_TO_5G_CHANNEL_NETWORK_BASE_SCORE;
            } else {
                networkScore = SWITCH_TO_5G_CHANNEL_NETWORK_BASE_SCORE;
            }
        } else if (curWifiConfig.networkId == targetWifiConfig.networkId) {
            networkScore = ROAM_TO_24G_CHANNEL_NETWORK_BASE_SCORE;
        } else {
            networkScore = 20;
        }
        if (networkScore != 0) {
            networkScore += signalLevel * 5;
        }
        HwHiLog.i(TAG, false, "candidate network info, networkId: %{public}d, ssid: %{public}s, bssid: %{public}s, freq: %{public}d, rssi: %{public}d, score: %{public}d", new Object[]{Integer.valueOf(targetWifiConfig.networkId), StringUtilEx.safeDisplaySsid(targetScanResult.SSID), StringUtilEx.safeDisplayBssid(targetScanResult.BSSID), Integer.valueOf(targetScanResult.frequency), Integer.valueOf(targetScanResult.level), Integer.valueOf(networkScore)});
        return networkScore;
    }

    private void cancelPeriodicScans() {
        if (this.mPeriodicScanTimerSet) {
            this.mAlarmManager.cancel(this.mPeriodicScanTimerListener);
            this.mPeriodicScanTimerSet = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleNextPeriodicScan() {
        int i = this.mGcCastOptRetryCnt;
        if (i < 1) {
            this.mGcCastOptRetryCnt = i + 1;
            this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 3000, TAG, this.mPeriodicScanTimerListener, this.mHandler);
            this.mPeriodicScanTimerSet = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void gcCastOptAction(ScanResult[] scanResults) {
        WifiConfiguration targetWifiConfig;
        int networkScore;
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        WifiConfiguration curWifiConfig = this.mClientModeImpl.getCurrentWifiConfiguration();
        if (curWifiConfig == null || wifiInfo == null) {
            HwHiLog.e(TAG, false, "error, castOptimizationAction param is null", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "current network info, ssid: %{public}s, freq: %{public}d, signalLevel: %{public}d, portalNetwork: %{public}s, noInternetAccess: %{public}s, ", new Object[]{StringUtilEx.safeDisplaySsid(wifiInfo.getSSID()), Integer.valueOf(wifiInfo.getFrequency()), Integer.valueOf(WifiProCommonUtils.getSignalLevel(wifiInfo.getFrequency(), wifiInfo.getRssi())), String.valueOf(curWifiConfig.portalNetwork), String.valueOf(curWifiConfig.noInternetAccess)});
        int p2pFreq = this.mCastOptMonitor.getP2pFrequency();
        ScanResult candidateScanResult = null;
        int candidateNetworkScore = 0;
        int candidateNetworkId = -1;
        for (ScanResult scanResult : scanResults) {
            if (isScanResultValid(scanResult, wifiInfo, p2pFreq) && (targetWifiConfig = this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(ScanResultUtil.toScanDetail(scanResult))) != null && isSavedNetworkValid(wifiInfo, curWifiConfig, targetWifiConfig, scanResult) && (networkScore = calculateNeworkScore(curWifiConfig, targetWifiConfig, p2pFreq, scanResult)) > candidateNetworkScore) {
                candidateNetworkScore = networkScore;
                candidateScanResult = scanResult;
                candidateNetworkId = targetWifiConfig.networkId;
            }
        }
        handleCandidateNetwork(curWifiConfig, candidateScanResult, candidateNetworkId);
    }

    private void handleCandidateNetwork(WifiConfiguration curWifiConfig, ScanResult candidateScanResult, int candidateNetworkId) {
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr == null) {
            HwHiLog.e(TAG, false, "handleCandidateNetwork castOptChr is null", new Object[0]);
        } else if (candidateNetworkId == -1 || candidateScanResult == null) {
            tryP2pSharing();
            HwHiLog.i(TAG, false, "no suitable network to switch", new Object[0]);
            scheduleNextPeriodicScan();
        } else if (curWifiConfig.networkId == candidateNetworkId) {
            HwHiLog.i(TAG, false, "roam to netowrk, ssid: %{public}s, freq: %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(candidateScanResult.SSID), Integer.valueOf(candidateScanResult.frequency)});
            this.mClientModeImpl.startRoamToNetwork(candidateNetworkId, candidateScanResult);
            if (ScanResult.is5GHz(candidateScanResult.frequency)) {
                castOptChr.setGcCastOptStaAction(1);
            } else {
                castOptChr.setGcCastOptStaAction(3);
            }
        } else {
            HwHiLog.i(TAG, false, "connect to netowrk, ssid: %{public}s, freq: %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(candidateScanResult.SSID), Integer.valueOf(candidateScanResult.frequency)});
            this.mClientModeImpl.startConnectToUserSelectNetwork(candidateNetworkId, (int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM, candidateScanResult.BSSID);
            if (ScanResult.is5GHz(candidateScanResult.frequency)) {
                castOptChr.setGcCastOptStaAction(2);
            } else {
                castOptChr.setGcCastOptStaAction(4);
            }
        }
    }

    private void tryP2pSharing() {
        if (this.mGcCastOptRetryCnt >= 1) {
            HwHiLog.i(TAG, false, "try p2p sharing", new Object[0]);
            P2pSharingDispatcher.getInstance().start();
        }
    }
}
