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
import com.android.server.hidata.IHidataCallback;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;

public class HwQoEWiFiOptimization implements IHidataCallback {
    private static HwQoEWiFiOptimization mHwQoEWiFiOptimization = null;
    private Context mContext;
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
        HwQoEUtils.logD("HwQoEService: hwQoELimitedSpeed: " + enable + " mode=" + mode + ",VpnConnected:" + this.mIsVpnConnected);
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
        HwQoEUtils.logD("Vpn Connected,can not limit speed!");
    }

    public synchronized void hwQoEHighPriorityTransmit(int uid, int type, int enable) {
        HwQoEUtils.logD("hwQoEHighPriorityTransmit uid: " + uid + " enable: " + enable + "type:" + type);
    }

    public synchronized void hwQoESetMode(int mode) {
        HwQoEUtils.logD("hwQoESetMode:  mode: " + mode);
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            this.mWifiNative.gameKOGAdjustSpeed(mWifiInfo.getFrequency(), mode);
        }
    }

    public synchronized void setTXPower(int enable) {
        HwQoEUtils.logD("hwQoESetTXPower:  enable: " + enable);
        if (this.mWifiNative != null) {
            this.mWifiNative.setPwrBoost(enable);
        }
    }

    public void onSetPMMode(int mode) {
        HwQoEUtils.logD("onSetPMMode:  mode: " + mode);
        if ((6 == mode || 7 == mode) && !HwQoEService.getInstance().isPermitUpdateWifiPowerMode(mode)) {
            HwQoEUtils.logD("onSetPMMode: Not allow set low power mode:" + mode);
            return;
        }
        hwQoESetMode(mode);
    }

    public void onSetTXPower(int enable) {
        HwQoEUtils.logD("onSetTXPower:  enable: " + enable);
        setTXPower(enable);
    }

    public void onPauseABSHandover() {
        HwQoEUtils.logD("onPauseABSHandover");
        if (HwABSUtils.getABSEnable()) {
            HwABSDetectorService mHwABSDetectorService = HwABSDetectorService.getInstance();
            if (mHwABSDetectorService != null) {
                mHwABSDetectorService.puaseABSHandover();
            }
        }
    }

    public void onRestartABSHandover() {
        HwQoEUtils.logD("onRestartABSHandover");
        if (HwABSUtils.getABSEnable()) {
            HwABSDetectorService mHwABSDetectorService = HwABSDetectorService.getInstance();
            if (mHwABSDetectorService != null) {
                mHwABSDetectorService.restartABSHandover();
            }
        }
    }

    public RssiPacketCountInfo onGetOTAInfo() {
        return HiDataUtilsManager.getInstance(this.mContext).getOTAInfo();
    }

    public void OnSetHiLinkAccGameMode(boolean enable, String appName) {
        this.mHwQoEHilink.handleAccGameStateChanged(enable, appName);
    }
}
