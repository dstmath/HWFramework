package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.text.TextUtils;
import com.android.server.hidata.IHidataCallback;
import com.android.server.hidata.arbitration.HwAppTimeDetail;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.HwWifiNativeEx;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;

public class HwQoEWiFiOptimization implements IHidataCallback {
    private static HwQoEWiFiOptimization mHwQoEWiFiOptimization = null;
    private Context mContext;
    private HwAppTimeDetail mHwAppTimeDetail;
    private HwQoEHilink mHwQoEHilink = null;
    private HwWifiBoost mHwWifiBoost;
    private boolean mIsVpnConnected;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;

    private HwQoEWiFiOptimization(Context context) {
        this.mContext = context;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwWifiBoost = HwWifiBoost.getInstance(this.mContext);
        this.mHwWifiBoost.registWifiBoostCallback(this);
        this.mHwAppTimeDetail = HwAppTimeDetail.createInstance(this.mContext);
        this.mHwAppTimeDetail.registWifiBoostCallback(this);
        this.mHwQoEHilink = HwQoEHilink.getInstance(this.mContext);
    }

    public static synchronized HwQoEWiFiOptimization getInstance(Context context) {
        HwQoEWiFiOptimization hwQoEWiFiOptimization;
        synchronized (HwQoEWiFiOptimization.class) {
            if (mHwQoEWiFiOptimization == null) {
                mHwQoEWiFiOptimization = new HwQoEWiFiOptimization(context);
            }
            hwQoEWiFiOptimization = mHwQoEWiFiOptimization;
        }
        return hwQoEWiFiOptimization;
    }

    public synchronized void updateVNPStateChanged(boolean isVpnConnected) {
        this.mIsVpnConnected = isVpnConnected;
        if (this.mIsVpnConnected) {
            hwQoELimitedSpeed(0, 1);
        }
    }

    public synchronized void hwQoELimitedSpeed(int enable, int mode) {
        HwQoEUtils.logD(false, "HwQoEService: hwQoELimitedSpeed: %{public}d mode=%{public}d, VpnConnected:%{public}s", Integer.valueOf(enable), Integer.valueOf(mode), String.valueOf(this.mIsVpnConnected));
        if (!this.mIsVpnConnected || enable != 1) {
            Bundle args = new Bundle();
            args.putInt("enbale", enable);
            args.putInt("mode", mode);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_NET_MANAGE), System.currentTimeMillis(), args);
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(id);
            return;
        }
        HwQoEUtils.logD(false, "Vpn Connected,can not limit speed!", new Object[0]);
    }

    public synchronized void hwQoEHighPriorityTransmit(int uid, int type, int enable) {
        HwQoEUtils.logD(false, "hwQoEHighPriorityTransmit uid: %{public}d enable: %{public}d type:%{public}d", Integer.valueOf(uid), Integer.valueOf(enable), Integer.valueOf(type));
    }

    public synchronized void hwQoESetMode(int mode) {
        HwQoEUtils.logD(false, "hwQoESetMode: mode: %{public}d", Integer.valueOf(mode));
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            this.mWifiNative.mHwWifiNativeEx.gameKOGAdjustSpeed(mWifiInfo.getFrequency(), mode);
        }
    }

    public int onSetHumanFactor(String path, int windowSize) {
        if (TextUtils.isEmpty(path)) {
            HwQoEUtils.logE(false, "onSetHumanFactor: path is empty", new Object[0]);
            return -1;
        }
        HwQoEUtils.logD(false, "onSetHumanFactor: %{public}s", path);
        HwWifiNativeEx hwWifiNativeEx = HwWifiNativeEx.getInstance();
        if (hwWifiNativeEx != null) {
            return hwWifiNativeEx.sendHumanFactor(path, windowSize);
        }
        HwQoEUtils.logE(false, "hwWifiNativeEx is null", new Object[0]);
        return -1;
    }

    public synchronized void setTXPower(int enable) {
        HwQoEUtils.logD(false, "hwQoESetTXPower: enable: %{public}d", Integer.valueOf(enable));
        if (this.mWifiNative != null) {
            this.mWifiNative.mHwWifiNativeEx.setPwrBoost(enable);
        }
    }

    public void onSetPMMode(int mode) {
        HwQoEUtils.logD(false, "onSetPMMode: mode: %{public}d", Integer.valueOf(mode));
        HwQoEService hwQoeService = HwQoEService.getInstance();
        if ((mode == 6 || mode == 7) && hwQoeService != null && !hwQoeService.isPermitUpdateWifiPowerMode(mode)) {
            HwQoEUtils.logD(false, "onSetPMMode: Not allow set low power mode:%{public}d", Integer.valueOf(mode));
        } else {
            hwQoESetMode(mode);
        }
    }

    public void onSetTXPower(int enable) {
        HwQoEUtils.logD(false, "onSetTXPower: enable: %{public}d", Integer.valueOf(enable));
        setTXPower(enable);
    }

    public void onPauseABSHandover() {
        HwABSDetectorService mHwABSDetectorService;
        HwQoEUtils.logD(false, "onPauseABSHandover", new Object[0]);
        if (HwABSUtils.getABSEnable() && (mHwABSDetectorService = HwABSDetectorService.getInstance()) != null) {
            mHwABSDetectorService.puaseABSHandover();
        }
    }

    public void onRestartABSHandover() {
        HwABSDetectorService mHwABSDetectorService;
        HwQoEUtils.logD(false, "onRestartABSHandover", new Object[0]);
        if (HwABSUtils.getABSEnable() && (mHwABSDetectorService = HwABSDetectorService.getInstance()) != null) {
            mHwABSDetectorService.restartABSHandover();
        }
    }

    public RssiPacketCountInfo onGetOtaInfo() {
        RssiPacketCountInfo info = new RssiPacketCountInfo();
        WifiNative wifiNative = this.mWifiNative;
        WifiNative.TxPacketCounters counters = wifiNative.getTxPacketCounters(wifiNative.getClientInterfaceName());
        if (counters != null) {
            info.txgood = counters.txSucceeded;
            info.txbad = counters.txFailed;
        } else {
            info.txgood = 0;
            info.txbad = 0;
        }
        return info;
    }

    public void OnSetHiLinkAccGameMode(boolean enable, String appName) {
        this.mHwQoEHilink.handleAccGameStateChanged(enable, appName);
    }
}
