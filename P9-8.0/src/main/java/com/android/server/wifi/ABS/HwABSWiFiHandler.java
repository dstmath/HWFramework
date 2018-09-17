package com.android.server.wifi.ABS;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.HwWifiStatStore;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.MSS.HwMSSArbitrager.MSSState;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachine;
import java.util.List;

public class HwABSWiFiHandler {
    private static final int ABS_HANDOVER_TIME_OUT = 500;
    public static final String SUPPLICANT_BSSID_ANY = "any";
    private Context mContext;
    private Handler mHandler;
    private HwABSDataBaseManager mHwABSDataBaseManager;
    private HwMSSArbitrager mHwMSSArbitrager;
    private boolean mIsABSHandover = false;
    private long mIsABSHandoverTime = 0;
    private int mNowCapability = 1;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private HwWifiStatStore mWifiStatStore;
    private WifiStateMachine mWifiStateMachine;

    public HwABSWiFiHandler(Context context, Handler handler, WifiStateMachine wifiStateMachine) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWifiStateMachine = wifiStateMachine;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwABSDataBaseManager = HwABSDataBaseManager.getInstance(context);
        this.mWifiStatStore = HwWifiServiceFactory.getHwWifiStatStore();
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
            HwABSUtils.logD("hwABSHandover, the same with current capability");
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
        this.mWifiNative.hwABSSoftHandover(type);
        this.mHandler.sendEmptyMessageDelayed(17, 200);
    }

    private boolean hwABSReconnectHandover() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null) {
            return false;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            HwABSUtils.logD("HwABSHandover, WiFi configured networks are invalid");
            return false;
        }
        WifiConfiguration changeConfig = null;
        for (WifiConfiguration nextConfig : configNetworks) {
            if (mWifiInfo.getNetworkId() == nextConfig.networkId) {
                changeConfig = nextConfig;
                break;
            }
        }
        if (changeConfig == null) {
            HwABSUtils.logD("HwABSHandover, WifiConfiguration is null ");
            return false;
        }
        if (this.mWifiStatStore != null) {
            this.mWifiStatStore.updateAssocByABS();
        }
        HwABSUtils.logD("hwABSReconnectHandover");
        setTargetBssid(mWifiInfo.getBSSID());
        this.mWifiStateMachine.reassociateCommand();
        return true;
    }

    public boolean setTargetBssid(String bssid) {
        HwABSUtils.logD("setTargetBssid bssid = " + bssid);
        this.mWifiNative.setConfiguredNetworkBSSID(bssid);
        return true;
    }

    public void hwABSHandoverInScreenOff() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
            HwABSApInfoData mHwABSApInfoData = this.mHwABSDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID());
            if (mHwABSApInfoData != null) {
                if (mHwABSApInfoData.mSwitch_siso_type == 0 || mHwABSApInfoData.mSwitch_siso_type == 1) {
                    hwABSSoftHandover(1);
                } else {
                    hwABSHandover(1);
                }
            }
        }
    }

    public void setAPCapability(int capability) {
        int wifiState = this.mWifiStateMachine.syncGetWifiState();
        this.mNowCapability = capability;
        HwABSUtils.logD("setAPCapability, capability = " + capability + " wifiState =" + wifiState);
        if (wifiState != 0 && wifiState != 1 && wifiState != 2) {
            this.mWifiNative.hwABSSetCapability(capability);
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
        HwABSUtils.logD("isHandoverTimeout handoverTime = " + handoverTime);
        if (handoverTime > 500) {
            return true;
        }
        return false;
    }

    public void setABSBlackList(String blackList) {
        int wifiState = this.mWifiStateMachine.syncGetWifiState();
        HwABSUtils.logD("setAPCapability, wifiState =" + wifiState);
        if (wifiState != 0 && wifiState != 1 && wifiState != 2) {
            this.mWifiNative.hwABSBlackList(blackList);
        }
    }

    public void setABSCurrentState(int state) {
        MSSState currentState = MSSState.MSSUNKNOWN;
        if (state == 1) {
            currentState = MSSState.ABSMRC;
        } else if (state == 2) {
            currentState = MSSState.ABSMIMO;
        } else if (state == 3) {
            currentState = MSSState.ABSSWITCHING;
        }
        HwABSUtils.logD("setABSCurrentState, state =" + state);
        this.mHwMSSArbitrager.setABSCurrentState(currentState);
    }

    public boolean isNeedHandover() {
        if (this.mHwMSSArbitrager.getMSSCurrentState() == MSSState.MSSSISO) {
            HwABSUtils.logD("isNeedHandover, return false");
            return false;
        }
        HwABSUtils.logD("isNeedHandover, return true");
        return true;
    }
}
