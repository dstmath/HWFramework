package com.android.server.wifi.ABS;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.MSS.HwMssArbitrager;
import com.android.server.wifi.MSS.HwMssUtils;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import java.util.Iterator;
import java.util.List;

public class HwAbsWiFiHandler {
    private static final int ABS_HANDOVER_TIME_OUT = 500;
    private static final int CHECK_LINK_DELAY_TIME = 200;
    private static final String PMF_CAPABILITY_PERFIX = "C:1";
    public static final String SUPPLICANT_BSSID_ANY = "any";
    private Context mContext;
    private Handler mHandler;
    private HwAbsDataBaseManager mHwAbsDataBaseManager;
    private HwMssArbitrager mHwMssArbitrager;
    private HwWifiCHRService mHwWifiChrService;
    private boolean mIsAbsHandover = false;
    private long mIsAbsHandoverTime = 0;
    private int mNowCapability = 2;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private ClientModeImpl mWifiStateMachine;

    public HwAbsWiFiHandler(Context context, Handler handler, ClientModeImpl wifiStateMachine) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWifiStateMachine = wifiStateMachine;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwAbsDataBaseManager = HwAbsDataBaseManager.getInstance(context);
        this.mHwWifiChrService = HwWifiCHRServiceImpl.getInstance();
        this.mHwMssArbitrager = HwMssArbitrager.getInstance(this.mContext);
        this.mIsAbsHandover = false;
    }

    private void handoverToMimo() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            setApCapability(2);
            hwAbsReconnectHandover();
        }
    }

    private void handoverToSiso() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            setApCapability(1);
            hwAbsReconnectHandover();
        }
    }

    public void hwAbsHandover(int capability) {
        if (this.mNowCapability == capability) {
            HwAbsUtils.logD(false, "hwAbsHandover, the same with current capability", new Object[0]);
            return;
        }
        setAbsHandover(true);
        if (capability == 2) {
            handoverToMimo();
        } else {
            handoverToSiso();
        }
    }

    private void hwAbsSoftHandover(int type) {
        setApCapability(type);
        this.mWifiNative.mHwWifiNativeEx.hwABSSoftHandover(type);
        this.mHandler.sendEmptyMessageDelayed(17, 200);
    }

    private void hwAbsReconnectHandover() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                HwAbsUtils.logD(false, "HwABSHandover, WiFi configured networks are invalid", new Object[0]);
                return;
            }
            WifiConfiguration changeConfig = null;
            Iterator<WifiConfiguration> it = configNetworks.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                WifiConfiguration nextConfig = it.next();
                if (wifiInfo.getNetworkId() == nextConfig.networkId) {
                    changeConfig = nextConfig;
                    break;
                }
            }
            if (changeConfig == null) {
                HwAbsUtils.logD(false, "HwABSHandover, WifiConfiguration is null ", new Object[0]);
                return;
            }
            HwWifiCHRService hwWifiCHRService = this.mHwWifiChrService;
            if (hwWifiCHRService != null) {
                hwWifiCHRService.updateAssocByABS();
            }
            HwAbsUtils.logD(false, "hwAbsReconnectHandover", new Object[0]);
            setTargetBssid(wifiInfo.getBSSID());
            String pmfInfo = ScanResultRecords.getDefault().getPmf(wifiInfo.getBSSID());
            if (pmfInfo == null || !pmfInfo.contains(PMF_CAPABILITY_PERFIX) || !HwMssUtils.is1105()) {
                this.mWifiStateMachine.reassociateCommand();
            } else {
                HwAbsUtils.logD(false, "this router support pmf, reassociate will cause disconnect", new Object[0]);
            }
        }
    }

    public void setTargetBssid(String bssid) {
        WifiNative wifiNative = this.mWifiNative;
        wifiNative.setConfiguredNetworkBSSID(wifiNative.getClientInterfaceName(), bssid);
    }

    public void hwAbsHandoverInScreenOff() {
        HwAbsApInfoData hwAbsApInfoData;
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null && (hwAbsApInfoData = this.mHwAbsDataBaseManager.getApInfoByBssid(wifiInfo.getBSSID())) != null) {
            if (hwAbsApInfoData.mSwitchSisoType == 0 || hwAbsApInfoData.mSwitchSisoType == 1) {
                hwAbsSoftHandover(1);
            } else {
                hwAbsHandover(1);
            }
        }
    }

    public void setApCapability(int capability) {
        int wifiState = this.mWifiStateMachine.syncGetWifiState();
        this.mNowCapability = capability;
        HwAbsUtils.logD(false, "setApCapability, capability = %{public}d wifiState =%{public}d", Integer.valueOf(capability), Integer.valueOf(wifiState));
        if (wifiState != 0 && wifiState != 1 && wifiState != 2) {
            this.mWifiNative.mHwWifiNativeEx.hwABSSetCapability(capability);
        }
    }

    public int getCurrentCapability() {
        return this.mNowCapability;
    }

    public boolean isAbsHandover() {
        return this.mIsAbsHandover;
    }

    public void setAbsHandover(boolean isHandover) {
        this.mIsAbsHandover = isHandover;
        if (this.mIsAbsHandover) {
            this.mIsAbsHandoverTime = System.currentTimeMillis();
            setAbsCurrentState(3);
            return;
        }
        this.mIsAbsHandoverTime = 0;
        setAbsCurrentState(this.mNowCapability);
    }

    public boolean isHandoverTimeout() {
        if (this.mIsAbsHandoverTime == 0) {
            return false;
        }
        long handoverTime = System.currentTimeMillis() - this.mIsAbsHandoverTime;
        HwAbsUtils.logD(false, "isHandoverTimeout handoverTime = %{public}s", String.valueOf(handoverTime));
        if (handoverTime > 500) {
            return true;
        }
        return false;
    }

    public void setAbsBlackList(String blackList) {
        int wifiState = this.mWifiStateMachine.syncGetWifiState();
        HwAbsUtils.logD(false, "setApCapability, wifiState = %{public}d", Integer.valueOf(wifiState));
        if (wifiState != 0 && wifiState != 1 && wifiState != 2) {
            this.mWifiNative.mHwWifiNativeEx.hwABSBlackList(blackList);
        }
    }

    public void setAbsCurrentState(int state) {
        HwMssArbitrager.MssState currentState = HwMssArbitrager.MssState.MSSUNKNOWN;
        if (state == 1) {
            currentState = HwMssArbitrager.MssState.ABSMRC;
        } else if (state == 2) {
            currentState = HwMssArbitrager.MssState.ABSMIMO;
        } else if (state == 3) {
            currentState = HwMssArbitrager.MssState.ABSSWITCHING;
        } else {
            HwAbsUtils.logD(false, "setAbsCurrentState,No processing state = %{public}d", Integer.valueOf(state));
        }
        HwAbsUtils.logD(false, "setAbsCurrentState, state = %{public}d", Integer.valueOf(state));
        this.mHwMssArbitrager.setAbsCurrentState(currentState);
    }

    public boolean isNeedHandover() {
        if (this.mHwMssArbitrager.getMssCurrentState() == HwMssArbitrager.MssState.MSSSISO) {
            HwAbsUtils.logD(false, "isNeedHandover, return false", new Object[0]);
            return false;
        }
        HwAbsUtils.logD(false, "isNeedHandover, return true", new Object[0]);
        return true;
    }

    public void hwAbsCheckLinked() {
    }
}
