package com.huawei.hwwifiproservice;

import android.content.Context;
import android.util.wifi.HwHiLog;
import com.huawei.hwwifiproservice.HwNetworkPropertyChecker;

public class HwWifiProPartImpl implements IHwWifiProPart {
    private static final String TAG = "HwWifiProPartImpl";
    private Context mContext;

    public HwWifiProPartImpl(Context context) {
        HwHiLog.i(TAG, false, "init HwWifiProPartImpl.", new Object[0]);
        this.mContext = context;
    }

    public int getAutoOpenCnt() {
        HwIntelligenceStateMachine hwIntelligence = HwIntelligenceStateMachine.getIntelligenceStateMachine();
        if (hwIntelligence != null) {
            return hwIntelligence.getAutoOpenCnt();
        }
        return 0;
    }

    public void setAutoOpenCnt(int count) {
        HwIntelligenceStateMachine hwIntelligence = HwIntelligenceStateMachine.getIntelligenceStateMachine();
        if (hwIntelligence != null) {
            hwIntelligence.setAutoOpenCnt(count);
        }
    }

    public String getCurrentPackageName() {
        HwAutoConnectManager hwAutoConnectManager = HwAutoConnectManager.getInstance();
        if (hwAutoConnectManager != null) {
            return hwAutoConnectManager.getCurrentPackageName();
        }
        return "";
    }

    public void updateStandardPortalTable(HwNetworkPropertyChecker.StarndardPortalInfo portalInfo) {
        PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
        if (database != null) {
            database.updateStandardPortalTable(portalInfo);
        }
    }

    public void updateDhcpResultsByBssid(String currBssid, String dhcpResults) {
        PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
        if (database != null) {
            database.updateDhcpResultsByBssid(currBssid, dhcpResults);
        }
    }

    public String syncQueryDhcpResultsByBssid(String currentBssid) {
        PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
        if (database != null) {
            return database.syncQueryDhcpResultsByBssid(currentBssid);
        }
        return null;
    }

    public void notifyHttpReachableForWifiPro(boolean httpReachable) {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyHttpReachable(httpReachable);
        }
    }

    public void notifyHttpRedirectedForWifiPro() {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyHttpRedirectedForWifiPro();
        }
    }

    public void notifyRoamingCompletedForWifiPro(String newBssid) {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyRoamingCompleted(newBssid);
        }
    }

    public void notifyRenewDhcpTimeoutForWifiPro() {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyRenewDhcpTimeoutForWifiPro();
        }
    }
}
