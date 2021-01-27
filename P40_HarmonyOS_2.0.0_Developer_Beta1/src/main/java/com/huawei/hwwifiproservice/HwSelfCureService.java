package com.huawei.hwwifiproservice;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Messenger;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.wifipro.IHwSelfCureService;
import com.huawei.hwwifiproservice.HwSelfCureService;
import java.util.List;

public class HwSelfCureService {
    private static final String TAG = "HwSelfCureService";
    private Context mContext;
    private HwSelfCureServiceManager mHwSelfCureServiceManager = new HwSelfCureServiceManager();

    public HwSelfCureService(Context context) {
        HwHiLog.i(TAG, false, "start HwSelfCureService.", new Object[0]);
        this.mContext = context;
        WifiProManagerEx.init(context);
        initHwSelfCureService();
    }

    /* access modifiers changed from: private */
    public final class HwSelfCureServiceManager implements IHwSelfCureService {
        private HwSelfCureServiceManager() {
        }

        public List<ScanResult> updateScanResultByWifiPro(List<ScanResult> scanResults) {
            if (scanResults != null && scanResults.size() > 0) {
                for (ScanResult sc : scanResults) {
                    WifiProConfigStore.updateScanDetailByWifiPro(sc);
                }
            }
            return scanResults;
        }

        public ScanResult updateScanDetailByWifiPro(ScanResult scanResult) {
            WifiProConfigStore.updateScanDetailByWifiPro(scanResult);
            return scanResult;
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
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().requestChangeWifiStatus(flag);
            }
        }

        public void notifySefCureCompleted(int status) {
            if (HwSelfCureEngine.getInstance() != null) {
                HwSelfCureEngine.getInstance().notifySefCureCompleted(status);
            }
        }

        public void notifyFirstConnectProbeResult(int respCode) {
            if (HwWifiproLiteStateMachine.getInstance() != null) {
                HwWifiproLiteStateMachine.getInstance().notifyFirstConnectProbeResult(respCode);
            }
        }

        public void notifyChrEvent(int eventId, String apType, String ssid, int freq) {
            if (apType != null) {
                WifiProChrUploadManager.getInstance(HwSelfCureService.this.mContext).post(new Runnable(eventId, apType, ssid, freq) {
                    /* class com.huawei.hwwifiproservice.$$Lambda$HwSelfCureService$HwSelfCureServiceManager$RHOUJV20AytBKKrTn8wBU1nFnJE */
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
                        HwSelfCureService.HwSelfCureServiceManager.this.lambda$notifyChrEvent$0$HwSelfCureService$HwSelfCureServiceManager(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$notifyChrEvent$0$HwSelfCureService$HwSelfCureServiceManager(int eventId, String apType, String ssid, int freq) {
            WifiProChrUploadManager.getInstance(HwSelfCureService.this.mContext).updateFwkEvent(eventId, apType, ssid, freq);
        }

        public int getNetworkProbeRuslt(boolean isReconfirm) {
            return new HwNetworkPropertyChecker(HwSelfCureService.this.mContext, null, null, true, null, false).isCaptivePortal(true);
        }

        public void uploadDisconnectedEvent(String eventType) {
            WifiProChrUploadManager.uploadDisconnectedEvent(eventType);
        }
    }

    public HwSelfCureServiceManager getHwSelfCureServiceManager() {
        return this.mHwSelfCureServiceManager;
    }

    public void initHwSelfCureService() {
        HwHiLog.i(TAG, false, "Start initHwSelfCureService", new Object[0]);
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 78, null);
        if (result == null) {
            HwHiLog.d(TAG, false, "initHwSelfCureService fail, Bundle is null", new Object[0]);
            return;
        }
        HwWifiproLiteStateMachine.getInstance(this.mContext, (Messenger) result.getParcelable("WifiStateMachineMessenger"), null).setup();
        HwSelfCureEngine.getInstance(this.mContext).setup();
        HwHiLog.i(TAG, false, "initHwSelfCureService accomplish", new Object[0]);
    }
}
