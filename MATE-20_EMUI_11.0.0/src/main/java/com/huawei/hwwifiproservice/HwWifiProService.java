package com.huawei.hwwifiproservice;

import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.telephony.TelephonyManager;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.wifipro.IHwSelfCureService;
import com.android.server.wifi.wifipro.IHwWifiProService;
import com.huawei.hwwifiproservice.HwWifiProService;
import java.util.ArrayList;
import java.util.List;

public class HwWifiProService {
    private static final int MSG_SCAN_GENIE_FAIL_EVENT = 1002;
    private static final int MSG_SCAN_GENIE_OCCUR = 1000;
    private static final int MSG_SCAN_GENIE_SUCC_EVENT = 1001;
    private static final String SCAN_GENIE_EVENT = "scanGenieEvent";
    private static final String SCAN_GENIE_FAIL_EVENT = "scanGenieFailCnt";
    private static final String SCAN_GENIE_OCCUR_EVENT = "scanGenieCnt";
    private static final String SCAN_GENIE_SUCC_EVENT = "scanGenieSuccCnt";
    private static final String TAG = "HwWifiProService";
    private boolean isHwWifiProServiceInitCompleted = false;
    private Context mContext;
    private Handler mEvaluateHandler;
    private HwAutoConnectManager mHwAutoConnectManager = null;
    private HwSelfCureEngine mHwSelfCureEngine;
    private HwUidTcpMonitor mHwUidTcpMonitor;
    private HwWifiConnectivityMonitor mHwWifiConnectivityMonitor;
    private HwWifiProServiceManager mHwWifiProServiceManager = new HwWifiProServiceManager();
    private WifiProStateMachine mWifiProStateMachine;
    private WifiScanGenieController mWifiScanGenieController;
    private WifiproBqeUtils mWifiproBqeUtils;
    private Messenger messenger;

    public HwWifiProService(Context context) {
        HwHiLog.i(TAG, false, "addService HwWifiProService().", new Object[0]);
        this.mContext = context;
        WifiProManagerEx.init(context);
    }

    /* access modifiers changed from: private */
    public final class HwWifiProServiceManager implements IHwWifiProService, IHwSelfCureService {
        private HwWifiProServiceManager() {
        }

        public long getRttDuration(int uid, int type) {
            return HwUidTcpMonitor.getInstance(HwWifiProService.this.mContext).getRttDuration(uid, type);
        }

        public long getRttSegs(int uid, int type) {
            return HwUidTcpMonitor.getInstance(HwWifiProService.this.mContext).getRttSegs(uid, type);
        }

        public void notifyUseFullChannels() {
            if (HwWifiProFeatureControl.sWifiProScanGenieCtrl) {
                WifiScanGenieController.createWifiScanGenieControllerImpl(HwWifiProService.this.mContext).notifyUseFullChannels();
            }
        }

        public List<String> getScanfrequencys() {
            if (!HwWifiProFeatureControl.sWifiProScanGenieCtrl) {
                return null;
            }
            WifiScanGenieController.createWifiScanGenieControllerImpl(HwWifiProService.this.mContext).getScanfrequencys();
            List<String> result = new ArrayList<>();
            List<Integer> frequencylist = WifiScanGenieController.createWifiScanGenieControllerImpl(HwWifiProService.this.mContext).getScanfrequencys();
            if (frequencylist == null || frequencylist.size() <= 0) {
                return null;
            }
            int j = frequencylist.size();
            for (int i = 0; i < j; i++) {
                result.add(String.valueOf(frequencylist.get(i)));
            }
            return result;
        }

        public void handleWiFiDisconnected() {
            if (HwWifiProFeatureControl.sWifiProScanGenieCtrl) {
                WifiScanGenieController.createWifiScanGenieControllerImpl(HwWifiProService.this.mContext).handleWiFiDisconnected();
            }
        }

        public void notifyWifiConnectedBackground() {
            if (HwWifiProFeatureControl.sWifiProScanGenieCtrl) {
                WifiScanGenieController.createWifiScanGenieControllerImpl(HwWifiProService.this.mContext).notifyWifiConnectedBackground();
            }
        }

        public void handleWiFiConnected(WifiConfiguration currentWifiConfig, boolean flag) {
            if (HwWifiProFeatureControl.sWifiProScanGenieCtrl) {
                WifiScanGenieController.createWifiScanGenieControllerImpl(HwWifiProService.this.mContext).handleWiFiConnected(currentWifiConfig, flag);
            }
        }

        public void notifyNetworkRoamingCompleted(String newBssid) {
            if (HwWifiProFeatureControl.sWifiProScanGenieCtrl) {
                WifiScanGenieController.createWifiScanGenieControllerImpl(HwWifiProService.this.mContext).notifyNetworkRoamingCompleted(newBssid);
            }
        }

        public void userHandoverWifi() {
            WifiProStateMachine.getWifiProStateMachineImpl().userHandoverWifi();
        }

        public void setWifiApEvaluateEnabled(boolean enablen) {
            WifiProStateMachine.getWifiProStateMachineImpl().setWifiApEvaluateEnabled(enablen);
        }

        public int getNetwoksHandoverType() {
            return WifiProStateMachine.getWifiProStateMachineImpl().getNetwoksHandoverType();
        }

        public void notifyNetworkUserConnect(boolean flag) {
            WifiProStateMachine.getWifiProStateMachineImpl().notifyNetworkUserConnect(flag);
        }

        public void notifyApkChangeWifiStatus(boolean flag, String packageName) {
            WifiProStateMachine.getWifiProStateMachineImpl().notifyApkChangeWifiStatus(flag, packageName);
        }

        public void notifyWifiDisconnected(Intent intent) {
            WifiProStateMachine.getWifiProStateMachineImpl().notifyWifiDisconnected(intent);
        }

        public boolean isWifiEvaluating() {
            return WifiProStateMachine.isWifiEvaluating();
        }

        public void disconnectePoorWifi() {
            HwWifiConnectivityMonitor.getInstance(HwWifiProService.this.mContext).disconnectePoorWifi();
        }

        public void notifyForegroundAppChanged(String packageName) {
            HwWifiConnectivityMonitor.getInstance(HwWifiProService.this.mContext).notifyForegroundAppChanged(packageName);
        }

        public void notifyWifiMonitorDisconnected() {
            HwWifiConnectivityMonitor.getInstance(HwWifiProService.this.mContext).notifyWifiDisconnected();
        }

        public void notifyWifiRoamingStarted() {
            HwWifiConnectivityMonitor.getInstance(HwWifiProService.this.mContext).notifyWifiRoamingStarted();
        }

        public void notifyWifiConnectivityRoamingCompleted() {
            HwWifiConnectivityMonitor.getInstance(HwWifiProService.this.mContext).notifyWifiRoamingCompleted();
        }

        public boolean isPortalNotifyOn() {
            if (HwWifiProService.this.mHwAutoConnectManager == null) {
                return false;
            }
            return HwAutoConnectManager.getInstance().isPortalNotifyOn();
        }

        public boolean isAutoJoinAllowedSetTargetBssid(WifiConfiguration candidate, String scanResultBssid) {
            if (HwWifiProService.this.mHwAutoConnectManager == null) {
                return false;
            }
            return HwAutoConnectManager.getInstance().isAutoJoinAllowedSetTargetBssid(candidate, scanResultBssid);
        }

        public void releaseBlackListBssid(WifiConfiguration config, boolean flag) {
            if (HwWifiProService.this.mHwAutoConnectManager != null) {
                HwAutoConnectManager.getInstance().releaseBlackListBssid(config, flag);
            }
        }

        public void notifyAutoConnectManagerDisconnected() {
            HwAutoConnectManager.getInstance().notifyNetworkDisconnected();
        }

        public void notifyWifiConnFailedInfo(WifiConfiguration selectedConfig, String bssid, int rssi, int reason) {
            if (HwWifiProService.this.mHwAutoConnectManager != null) {
                HwAutoConnectManager.getInstance().notifyWifiConnFailedInfo(selectedConfig, bssid, rssi, reason);
            }
        }

        public void notifyEnableSameNetworkId(int netId) {
            if (HwWifiProService.this.mHwAutoConnectManager != null) {
                HwAutoConnectManager.getInstance().notifyEnableSameNetworkId(netId);
            }
        }

        public boolean allowAutoJoinDisabledNetworkAgain(WifiConfiguration config) {
            if (HwWifiProService.this.mHwAutoConnectManager == null) {
                return false;
            }
            return HwAutoConnectManager.getInstance().allowAutoJoinDisabledNetworkAgain(config);
        }

        public String getCurrentPackageNameFromWifiPro() {
            if (HwWifiProService.this.mHwAutoConnectManager == null) {
                return "";
            }
            return HwAutoConnectManager.getInstance().getCurrentPackageName();
        }

        public boolean isBssidMatchedBlacklist(String bssid) {
            if (HwWifiProService.this.mHwAutoConnectManager == null) {
                return false;
            }
            return HwAutoConnectManager.getInstance().isBssidMatchedBlacklist(bssid);
        }

        public boolean allowCheckPortalNetwork(String configKey, String bssid) {
            if (HwWifiProService.this.mHwAutoConnectManager == null) {
                return false;
            }
            return HwAutoConnectManager.getInstance().allowCheckPortalNetwork(configKey, bssid);
        }

        public void updatePopUpNetworkRssi(String configKey, int maxRssi) {
            if (HwWifiProService.this.mHwAutoConnectManager != null) {
                HwAutoConnectManager.getInstance().updatePopUpNetworkRssi(configKey, maxRssi);
            }
        }

        public void setWiFiProScanResultList(List<ScanResult> list) {
            HwIntelligenceWiFiManager.setWiFiProScanResultList(list);
        }

        public List<ScanResult> updateScanResultByWifiPro(List<ScanResult> scanResults) {
            if (scanResults != null && scanResults.size() > 0) {
                for (ScanResult tmp : scanResults) {
                    WifiProConfigStore.updateScanDetailByWifiPro(tmp);
                }
            }
            return scanResults;
        }

        public ScanResult updateScanDetailByWifiPro(ScanResult scanResult) {
            WifiProConfigStore.updateScanDetailByWifiPro(scanResult);
            return scanResult;
        }

        public boolean isDualbandScanning() {
            return HwDualBandManager.getInstance().isDualbandScanning();
        }

        public boolean isDhcpFailedBssid(String bssid) {
            if (HwSelfCureEngine.getInstance() == null) {
                return false;
            }
            return HwSelfCureEngine.getInstance().isDhcpFailedBssid(bssid);
        }

        public boolean isDhcpFailedConfigKey(String configKey) {
            if (HwSelfCureEngine.getInstance() == null) {
                return false;
            }
            return HwSelfCureEngine.getInstance().isDhcpFailedConfigKey(configKey);
        }

        public void notifyDhcpResultsInternetOk(String dhcpResults) {
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().notifyDhcpResultsInternetOk(dhcpResults);
            }
        }

        public void notifySelfCureWifiConnectedBackground() {
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().notifyWifiConnectedBackground();
            }
        }

        public void notifySelfCureWifiDisconnected() {
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().notifyWifiDisconnected();
            }
        }

        public void notifySelfCureWifiScanResultsAvailable(boolean flag) {
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().notifyWifiScanResultsAvailable(flag);
            }
        }

        public void notifySelfCureWifiRoamingCompleted(String newBssid) {
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().notifyWifiRoamingCompleted(newBssid);
            }
        }

        public void notifySelfCureIpConfigCompleted() {
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().notifyIpConfigCompleted();
            }
        }

        public boolean notifySelfCureIpConfigLostAndHandle(WifiConfiguration config) {
            if (HwSelfCureEngine.getInstance() == null) {
                return false;
            }
            return HwSelfCureEngine.getInstance().notifyIpConfigLostAndHandle(config);
        }

        public void requestChangeWifiStatus(boolean flag) {
            HwSelfCureEngine.getInstance().requestChangeWifiStatus(flag);
        }

        public void notifySefCureCompleted(int status) {
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().notifySefCureCompleted(status);
            }
        }

        public void sendMessageToHwDualBandStateMachine(int message) {
            if (message == 7) {
                HwDualBandManager.getHwDualBandStateMachine().getStateMachineHandler().sendEmptyMessage(7);
            }
        }

        public void notifyFirstConnectProbeResult(int respCode) {
            HwHiLog.i(HwWifiProService.TAG, false, "notifyFirstConnectProbeResult = %{public}d", new Object[]{Integer.valueOf(respCode)});
            if (WifiProStateMachine.getWifiProStateMachineImpl() != null) {
                WifiProStateMachine.getWifiProStateMachineImpl().notifyWifiProConnect();
            }
            HwWifiProFeatureControl.getInstance().notifyFirstConnectProbeResult(respCode);
        }

        public void notifyTcpStatResult(List<String> list) {
            if (list == null) {
                HwHiLog.e(HwWifiProService.TAG, false, "list is null, return.", new Object[0]);
            } else {
                HwUidTcpMonitor.getInstance(HwWifiProService.this.mContext).parseTcpStatLines(list);
            }
        }

        public Bundle getWifiDisplayInfo(NetworkInfo networkInfo) {
            return WifiProStateMachine.getWifiProStateMachineImpl().getWifiDisplayInfo(networkInfo);
        }

        public void notifyChrEvent(int eventId, String apType, String ssid, int freq) {
            if (apType != null) {
                WifiProChrUploadManager.getInstance(HwWifiProService.this.mContext).post(new Runnable(eventId, apType, ssid, freq) {
                    /* class com.huawei.hwwifiproservice.$$Lambda$HwWifiProService$HwWifiProServiceManager$QUlPfFnW17uPiGhdhMTkfXcSe1w */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ String f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ int f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwWifiProService.HwWifiProServiceManager.this.lambda$notifyChrEvent$0$HwWifiProService$HwWifiProServiceManager(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$notifyChrEvent$0$HwWifiProService$HwWifiProServiceManager(int eventId, String apType, String ssid, int freq) {
            WifiProChrUploadManager.getInstance(HwWifiProService.this.mContext).updateFwkEvent(eventId, apType, ssid, freq);
        }

        public int getNetworkProbeRuslt(boolean isReconfirm) {
            return new HwNetworkPropertyChecker(HwWifiProService.this.mContext, (WifiManager) null, (TelephonyManager) null, true, (Network) null, false).isCaptivePortal(true);
        }

        public void notifyScanGenieEvent(int eventId) {
            WifiProChrUploadManager uploadManager = WifiProChrUploadManager.getInstance(HwWifiProService.this.mContext);
            if (uploadManager != null) {
                if (eventId == HwWifiProService.MSG_SCAN_GENIE_OCCUR) {
                    uploadManager.addChrCntStat(HwWifiProService.SCAN_GENIE_EVENT, HwWifiProService.SCAN_GENIE_OCCUR_EVENT);
                } else if (eventId == HwWifiProService.MSG_SCAN_GENIE_SUCC_EVENT) {
                    uploadManager.addChrCntStat(HwWifiProService.SCAN_GENIE_EVENT, HwWifiProService.SCAN_GENIE_SUCC_EVENT);
                } else if (eventId == HwWifiProService.MSG_SCAN_GENIE_FAIL_EVENT) {
                    uploadManager.addChrCntStat(HwWifiProService.SCAN_GENIE_EVENT, HwWifiProService.SCAN_GENIE_FAIL_EVENT);
                } else {
                    HwHiLog.w(HwWifiProService.TAG, false, "notifyScanGenieEvent not match event", new Object[0]);
                }
            }
        }

        public void updateDualBandSwitchEvent() {
            WifiProChrUploadManager uploadManager = WifiProChrUploadManager.getInstance(HwWifiProService.this.mContext);
            if (uploadManager != null) {
                Bundle dualBandSwitch = new Bundle();
                dualBandSwitch.putInt("index", 2);
                uploadManager.addChrBundleStat("wifiSwitchCntEvent", "wifiSwitchCnt", dualBandSwitch);
            }
        }

        public void uploadDisconnectedEvent(String eventType) {
            WifiProChrUploadManager.uploadDisconnectedEvent(eventType);
        }
    }

    public HwWifiProServiceManager getHwWifiProServiceManager() {
        return this.mHwWifiProServiceManager;
    }

    public void initHwWifiProService() {
        if (this.isHwWifiProServiceInitCompleted) {
            HwHiLog.i(TAG, false, "Service has already completed initialization!", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "Enter initHwWifiProService", new Object[0]);
        WifiProChrUploadManager.getInstance(this.mContext).setup();
        HwHiLog.i(TAG, false, "start create HwWifiConnectivityMonitor", new Object[0]);
        HwWifiConnectivityMonitor.getInstance(this.mContext).setup();
        HwWifiProFeatureControl.getInstance(this.mContext).init();
        HwHiLog.i(TAG, false, "start create WifiScanGenieController", new Object[0]);
        if (HwWifiProFeatureControl.sWifiProScanGenieCtrl) {
            WifiScanGenieController.createWifiScanGenieControllerImpl(this.mContext).handleWiFiDisconnected();
        }
        this.mHwAutoConnectManager = HwAutoConnectManager.getInstance();
        if (this.mHwAutoConnectManager != null) {
            HwAutoConnectManager.getInstance().notifyNetworkDisconnected();
        }
        Bundle bundle = new Bundle();
        if (WifiProStateMachine.getWifiProStateMachineImpl() != null) {
            bundle.putBoolean("isWifiProStateMachineNotNull", true);
        }
        if (HwAutoConnectManager.getInstance() != null) {
            bundle.putBoolean("isHwAutoConnectManagerNotNull", true);
        }
        if (HwDualBandManager.getInstance() != null) {
            bundle.putBoolean("isHwDualBandManagerNotNull", true);
        }
        if (HwSelfCureEngine.getInstance() != null) {
            bundle.putBoolean("isHwSelfCureEngineNotNull", true);
        }
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 0, bundle);
        HwHiLog.i(TAG, false, "initHwWifiProService INTERFACE_NOTIFY_HWWIFIPROSERVICE_ACCOMPLISHED", new Object[0]);
        this.isHwWifiProServiceInitCompleted = true;
    }
}
