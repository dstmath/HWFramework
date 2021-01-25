package com.android.server.wifi.ABS;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.MSS.HwMSSUtils;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import java.util.Iterator;
import java.util.List;

public class HwABSWiFiHandler {
    private static final int ABS_HANDOVER_TIME_OUT = 500;
    private static final String PMF_CAPABILITY_PERFIX = "C:1";
    public static final String SUPPLICANT_BSSID_ANY = "any";
    private Context mContext;
    private Handler mHandler;
    private HwABSDataBaseManager mHwABSDataBaseManager;
    private HwMSSArbitrager mHwMSSArbitrager;
    private HwWifiCHRService mHwWifiCHRService;
    private boolean mIsABSHandover = false;
    private long mIsABSHandoverTime = 0;
    private int mNowCapability = 2;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private ClientModeImpl mWifiStateMachine;

    public HwABSWiFiHandler(Context context, Handler handler, ClientModeImpl wifiStateMachine) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWifiStateMachine = wifiStateMachine;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwABSDataBaseManager = HwABSDataBaseManager.getInstance(context);
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        this.mHwMSSArbitrager = HwMSSArbitrager.getInstance(this.mContext);
        this.mIsABSHandover = false;
    }

    private void handoverToMIMO() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
            setAPCapability(2);
            hwABSReconnectHandover();
        }
    }

    private void handoverToSISO() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
            setAPCapability(1);
            hwABSReconnectHandover();
        }
    }

    public void hwABSHandover(int capability) {
        if (this.mNowCapability == capability) {
            HwABSUtils.logD(false, "hwABSHandover, the same with current capability", new Object[0]);
            return;
        }
        setIsABSHandover(true);
        if (2 == capability) {
            handoverToMIMO();
        } else {
            handoverToSISO();
        }
    }

    private void hwABSSoftHandover(int type) {
        setAPCapability(type);
        this.mWifiNative.mHwWifiNativeEx.hwABSSoftHandover(type);
        this.mHandler.sendEmptyMessageDelayed(17, 200);
    }

    private boolean hwABSReconnectHandover() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null) {
            return false;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            HwABSUtils.logD(false, "HwABSHandover, WiFi configured networks are invalid", new Object[0]);
            return false;
        }
        WifiConfiguration changeConfig = null;
        Iterator<WifiConfiguration> it = configNetworks.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            WifiConfiguration nextConfig = it.next();
            if (mWifiInfo.getNetworkId() == nextConfig.networkId) {
                changeConfig = nextConfig;
                break;
            }
        }
        if (changeConfig == null) {
            HwABSUtils.logD(false, "HwABSHandover, WifiConfiguration is null ", new Object[0]);
            return false;
        }
        HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
        if (hwWifiCHRService != null) {
            hwWifiCHRService.updateAssocByABS();
        }
        HwABSUtils.logD(false, "hwABSReconnectHandover", new Object[0]);
        setTargetBssid(mWifiInfo.getBSSID());
        String pmfInfo = ScanResultRecords.getDefault().getPmf(mWifiInfo.getBSSID());
        if (pmfInfo == null || !pmfInfo.contains(PMF_CAPABILITY_PERFIX) || !HwMSSUtils.is1105()) {
            this.mWifiStateMachine.reassociateCommand();
            return true;
        }
        HwABSUtils.logD(false, "this router support pmf, reassociate will cause disconnect", new Object[0]);
        return true;
    }

    public boolean setTargetBssid(String bssid) {
        WifiNative wifiNative = this.mWifiNative;
        wifiNative.setConfiguredNetworkBSSID(wifiNative.getClientInterfaceName(), bssid);
        return true;
    }

    public void hwABSHandoverInScreenOff() {
        HwABSApInfoData mHwABSApInfoData;
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null && mWifiInfo.getBSSID() != null && (mHwABSApInfoData = this.mHwABSDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID())) != null) {
            if (mHwABSApInfoData.mSwitch_siso_type == 0 || mHwABSApInfoData.mSwitch_siso_type == 1) {
                hwABSSoftHandover(1);
            } else {
                hwABSHandover(1);
            }
        }
    }

    public void setAPCapability(int capability) {
        int wifiState = this.mWifiStateMachine.syncGetWifiState();
        this.mNowCapability = capability;
        HwABSUtils.logD(false, "setAPCapability, capability = %{public}d wifiState =%{public}d", Integer.valueOf(capability), Integer.valueOf(wifiState));
        if (wifiState != 0 && wifiState != 1 && wifiState != 2) {
            this.mWifiNative.mHwWifiNativeEx.hwABSSetCapability(capability);
        }
    }

    public int getCurrentCapability() {
        return this.mNowCapability;
    }

    public void hwABScheckLinked() {
    }

    public boolean getIsABSHandover() {
        return this.mIsABSHandover;
    }

    public void setIsABSHandover(boolean flag) {
        this.mIsABSHandover = flag;
        if (this.mIsABSHandover) {
            this.mIsABSHandoverTime = System.currentTimeMillis();
            setABSCurrentState(3);
            return;
        }
        this.mIsABSHandoverTime = 0;
        setABSCurrentState(this.mNowCapability);
    }

    public boolean isHandoverTimeout() {
        if (this.mIsABSHandoverTime == 0) {
            return false;
        }
        long handoverTime = System.currentTimeMillis() - this.mIsABSHandoverTime;
        HwABSUtils.logD(false, "isHandoverTimeout handoverTime = %{public}s", String.valueOf(handoverTime));
        if (handoverTime > 500) {
            return true;
        }
        return false;
    }

    public void setABSBlackList(String blackList) {
        int wifiState = this.mWifiStateMachine.syncGetWifiState();
        HwABSUtils.logD(false, "setAPCapability, wifiState = %{public}d", Integer.valueOf(wifiState));
        if (wifiState != 0 && wifiState != 1 && wifiState != 2) {
            this.mWifiNative.mHwWifiNativeEx.hwABSBlackList(blackList);
        }
    }

    public void setABSCurrentState(int state) {
        HwMSSArbitrager.MSSState currentState = HwMSSArbitrager.MSSState.MSSUNKNOWN;
        if (state == 1) {
            currentState = HwMSSArbitrager.MSSState.ABSMRC;
        } else if (state == 2) {
            currentState = HwMSSArbitrager.MSSState.ABSMIMO;
        } else if (state == 3) {
            currentState = HwMSSArbitrager.MSSState.ABSSWITCHING;
        }
        HwABSUtils.logD(false, "setABSCurrentState, state = %{public}d", Integer.valueOf(state));
        this.mHwMSSArbitrager.setABSCurrentState(currentState);
    }

    public boolean isNeedHandover() {
        if (this.mHwMSSArbitrager.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSSISO) {
            HwABSUtils.logD(false, "isNeedHandover, return false", new Object[0]);
            return false;
        }
        HwABSUtils.logD(false, "isNeedHandover, return true", new Object[0]);
        return true;
    }
}
