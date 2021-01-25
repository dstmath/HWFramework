package com.android.server.wifi.cast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.ScanResultMatchInfo;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.cast.CastOptDeviceCfg;
import com.android.server.wifi.hwUtil.WifiCommonUtils;

public class CastOptMonitor {
    private static final String TAG = "CastOptMonitor";
    private static CastOptMonitor sCastOptMonitor = null;
    private CastOptBroadcastReceiver mBroadcastReceiver = null;
    private CastOptDeviceCfg mCastOptDeviceCfg = null;
    private CastOptGoActioner mCastOptGoActioner = null;
    private Context mContext = null;
    private boolean mIsP2pConnected = false;
    private boolean mIsStaConnected = false;
    private final Object mLock = new Object();
    private WifiManager mWifiManager = null;
    private WifiP2pGroup mWifiP2pGroup = null;

    private CastOptMonitor(Context context, Looper looper) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mCastOptDeviceCfg = CastOptDeviceCfg.getInstance();
        this.mCastOptGoActioner = CastOptGoActioner.createCastOptGoActioner(context, looper, this.mCastOptDeviceCfg);
        registerBroadcastReceiver();
    }

    /* access modifiers changed from: private */
    public class CastOptBroadcastReceiver extends BroadcastReceiver {
        private CastOptBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwHiLog.e(CastOptMonitor.TAG, false, "error, intent is null", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                CastOptMonitor.this.handleStaStateChanged(intent);
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action) || "android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                CastOptMonitor.this.handleP2pStateChanged(intent);
            } else {
                HwHiLog.i(CastOptMonitor.TAG, false, "unsupport broadcast", new Object[0]);
            }
        }
    }

    protected static CastOptMonitor createCastOptMonitor(Context context, Looper looper) {
        if (sCastOptMonitor == null) {
            sCastOptMonitor = new CastOptMonitor(context, looper);
        }
        return sCastOptMonitor;
    }

    protected static CastOptMonitor getInstance() {
        return sCastOptMonitor;
    }

    /* access modifiers changed from: protected */
    public boolean isStaConnected() {
        return this.mIsStaConnected;
    }

    /* access modifiers changed from: protected */
    public boolean isP2pConnected() {
        return this.mIsP2pConnected;
    }

    /* access modifiers changed from: protected */
    public boolean isP2pGroupOwner() {
        boolean isP2pGroupOwner = false;
        synchronized (this.mLock) {
            if (this.mWifiP2pGroup != null) {
                isP2pGroupOwner = this.mWifiP2pGroup.isGroupOwner();
            }
        }
        return isP2pGroupOwner;
    }

    /* access modifiers changed from: protected */
    public int getP2pFrequency() {
        int p2pFreq = 0;
        synchronized (this.mLock) {
            if (this.mWifiP2pGroup != null) {
                p2pFreq = this.mWifiP2pGroup.getFrequency();
            }
        }
        return p2pFreq;
    }

    /* access modifiers changed from: protected */
    public int getStaFrequency() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null || wifiManager.getConnectionInfo() == null) {
            return 0;
        }
        return this.mWifiManager.getConnectionInfo().getFrequency();
    }

    /* access modifiers changed from: protected */
    public WifiConfiguration getCurrentWifiConfig() {
        WifiInfo wifiInfo;
        WifiConfigManager wifiConfigManager;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null || (wifiConfigManager = WifiInjector.getInstance().getWifiConfigManager()) == null) {
            return null;
        }
        return wifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
    }

    /* access modifiers changed from: protected */
    public void setGoSwitchChannelEnableWhenDetectRadar(boolean isAllowGoSwitchChannel) {
        byte[] buffs = {(byte) (!isAllowGoSwitchChannel ? 1 : 0)};
        WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
        if (wifiNative == null || wifiNative.mHwWifiNativeEx == null) {
            HwHiLog.e(TAG, false, "wifiNative or mHwWifiNativeEx is null", new Object[0]);
            return;
        }
        int ret = wifiNative.mHwWifiNativeEx.sendCmdToDriver("p2p0", 163, buffs);
        if (ret < 0) {
            HwHiLog.i(TAG, false, "set go detect radar failed, ret = %{public}d", new Object[]{Integer.valueOf(ret)});
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setP2pGroupInfo(WifiP2pGroup info) {
        synchronized (this.mLock) {
            this.mWifiP2pGroup = info;
            CastOptChr castOptChr = CastOptChr.getInstance();
            if (castOptChr != null) {
                castOptChr.handleP2pConnected(info);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStaStateChanged(Intent intent) {
        CastOptDeviceCfg castOptDeviceCfg;
        NetworkInfo info = null;
        Object obj = intent.getParcelableExtra("networkInfo");
        if (obj instanceof NetworkInfo) {
            info = (NetworkInfo) obj;
        }
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr != null) {
            castOptChr.handleStaConnectionStateChanged(info);
        }
        if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
            this.mIsStaConnected = false;
            CastOptDeviceCfg castOptDeviceCfg2 = this.mCastOptDeviceCfg;
            if (castOptDeviceCfg2 != null) {
                castOptDeviceCfg2.getSelfDeviceCfgInfo().resetDeviceWifiCfg();
                if (!isP2pGroupOwner()) {
                    this.mCastOptDeviceCfg.doCallback(false);
                }
                handleGoStaChannelChange();
            }
        } else if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            this.mIsStaConnected = true;
            saveWifiInfo();
            if (!isP2pGroupOwner() && (castOptDeviceCfg = this.mCastOptDeviceCfg) != null) {
                castOptDeviceCfg.doCallback(true);
            }
            handleGoStaChannelChange();
        }
    }

    private void handleGoStaChannelChange() {
        if (this.mCastOptGoActioner != null && isP2pGroupOwner()) {
            this.mCastOptGoActioner.handleDeviceChannelChange();
        }
    }

    private void saveWifiInfo() {
        WifiInfo wifiInfo;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null && (wifiInfo = wifiManager.getConnectionInfo()) != null) {
            if (this.mIsStaConnected && this.mIsP2pConnected) {
                HwHiLog.i(TAG, false, "current p2p freq: %{public}d, sta freq: %{public}d", new Object[]{Integer.valueOf(getP2pFrequency()), Integer.valueOf(wifiInfo.getFrequency())});
            }
            CastOptDeviceCfg castOptDeviceCfg = this.mCastOptDeviceCfg;
            if (castOptDeviceCfg != null && castOptDeviceCfg.getSelfDeviceCfgInfo() != null) {
                CastOptDeviceCfg.DeviceWifiInfo selfDeviceCfgInfo = this.mCastOptDeviceCfg.getSelfDeviceCfgInfo();
                selfDeviceCfgInfo.setCurrentApBssid(wifiInfo.getBSSID());
                selfDeviceCfgInfo.setCurrentApSsid(WifiInfo.removeDoubleQuotes(wifiInfo.getSSID()));
                selfDeviceCfgInfo.setStaChannel(WifiCommonUtils.convertFrequencyToChannelNumber(wifiInfo.getFrequency()));
                WifiConfiguration wifiConfig = WifiInjector.getInstance().getWifiConfigManager().getConfiguredNetworkWithoutMasking(wifiInfo.getNetworkId());
                if (wifiConfig != null) {
                    selfDeviceCfgInfo.setApPassword(WifiInfo.removeDoubleQuotes(wifiConfig.preSharedKey));
                    selfDeviceCfgInfo.setApSecurityType(ScanResultMatchInfo.getNetworkType(wifiConfig));
                    selfDeviceCfgInfo.setCurrentApHasInternet(true ^ wifiConfig.noInternetAccess);
                    selfDeviceCfgInfo.setApType(wifiConfig.portalNetwork);
                    selfDeviceCfgInfo.setRouteType(wifiConfig.isHiLinkNetwork ? 1 : 0);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pStateChanged(Intent intent) {
        boolean isCurrentP2pConnected = false;
        String action = intent.getAction();
        if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
            NetworkInfo info = null;
            Object obj = intent.getParcelableExtra("networkInfo");
            if (obj instanceof NetworkInfo) {
                info = (NetworkInfo) obj;
            }
            if (info == null || !info.isConnected()) {
                isCurrentP2pConnected = false;
            } else {
                isCurrentP2pConnected = true;
            }
            CastOptChr castOptChr = CastOptChr.getInstance();
            if (castOptChr != null) {
                castOptChr.handleP2pConnectionStateChanged(info);
            }
        } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
            int p2pState = intent.getIntExtra("extraState", -1);
            if (p2pState == 2) {
                isCurrentP2pConnected = true;
            } else {
                isCurrentP2pConnected = false;
            }
            CastOptChr castOptChr2 = CastOptChr.getInstance();
            if (castOptChr2 != null) {
                castOptChr2.handleHwP2pConnectionStateChanged(p2pState);
            }
        }
        handleP2pStateChange(isCurrentP2pConnected);
    }

    private void handleP2pStateChange(boolean isCurrentP2pConnected) {
        if (!this.mIsP2pConnected && isCurrentP2pConnected) {
            HwHiLog.i(TAG, false, "p2p change from disconnect to connect", new Object[0]);
            WifiP2pManager wifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
            if (wifiP2pManager == null) {
                HwHiLog.e(TAG, false, "error, wifiP2pManager is null", new Object[0]);
                return;
            } else {
                Context context = this.mContext;
                wifiP2pManager.requestGroupInfo(wifiP2pManager.initialize(context, context.getMainLooper(), null), new WifiP2pManager.GroupInfoListener() {
                    /* class com.android.server.wifi.cast.CastOptMonitor.AnonymousClass1 */

                    @Override // android.net.wifi.p2p.WifiP2pManager.GroupInfoListener
                    public void onGroupInfoAvailable(WifiP2pGroup info) {
                        CastOptMonitor.this.setP2pGroupInfo(info);
                        CastOptGcActioner castOptGcActioner = CastOptGcActioner.getInstance();
                        if (castOptGcActioner != null) {
                            castOptGcActioner.startScan();
                        }
                    }
                });
            }
        }
        if (this.mIsP2pConnected && !isCurrentP2pConnected) {
            HwHiLog.i(TAG, false, "p2p change from connect to disconnect", new Object[0]);
            resetCastOptCfg();
        }
        this.mIsP2pConnected = isCurrentP2pConnected;
        if (this.mIsP2pConnected && isP2pGroupOwner()) {
            HwHiLog.i(TAG, false, "p2p change set go switch channel enable", new Object[0]);
            setGoSwitchChannelEnableWhenDetectRadar(true);
        }
    }

    private void resetCastOptCfg() {
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr != null) {
            castOptChr.handleP2pDisconnected();
        }
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (castOptManager != null) {
            castOptManager.setCastOptScenes(false);
        }
        CastOptGcActioner castOptGcActioner = CastOptGcActioner.getInstance();
        if (castOptGcActioner != null) {
            castOptGcActioner.resetGcCastOptCfg();
        }
        CastOptGoActioner castOptGoActioner = this.mCastOptGoActioner;
        if (castOptGoActioner != null) {
            castOptGoActioner.resetGoCastOptCfg();
        }
        synchronized (this.mLock) {
            this.mWifiP2pGroup = null;
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mBroadcastReceiver = new CastOptBroadcastReceiver();
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }
}
