package com.android.server.wifi.cast;

import android.content.Context;
import android.net.wifi.IWifiCfgCallback;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Looper;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.cast.CastOptDeviceCfg;
import com.android.server.wifi.cast.P2pSharing.P2pSharingDispatcher;
import com.huawei.android.server.wifi.cast.ICastScene;

public class CastOptManager {
    private static final String TAG = "CastOptManager";
    private static CastOptManager sCastOptManager = null;
    private CastOptChr mCastOptChr = null;
    private CastOptDeviceCfg mCastOptDeviceCfg = null;
    private CastOptGcActioner mCastOptGcActioner = null;
    private CastOptGoActioner mCastOptGoActioner = null;
    private CastOptMonitor mCastOptMonitor = null;
    private CastOptScanner mCastOptScanner = null;
    private ICastScene mCastSceneListener;
    private CastType mCastType;
    private Context mContext = null;
    private boolean mIsCastOptScenes = false;
    private boolean mIsCastOptSupported = false;
    private int mTriggerType = -1;

    private CastOptManager(Context context, Looper looper) {
        this.mContext = context;
        if (CastOptUtils.isCastOptSupported()) {
            this.mIsCastOptSupported = true;
        }
        if (this.mIsCastOptSupported) {
            HwHiLog.i(TAG, false, "createCastOptService", new Object[0]);
            if (this.mCastOptDeviceCfg == null) {
                this.mCastOptDeviceCfg = CastOptDeviceCfg.createCastOptDeviceCfg(context);
            }
            if (this.mCastOptMonitor == null) {
                this.mCastOptMonitor = CastOptMonitor.createCastOptMonitor(context, looper);
            }
            if (this.mCastOptGcActioner == null) {
                this.mCastOptGcActioner = CastOptGcActioner.createCastOptGcActioner(context, looper, this.mCastOptMonitor);
            }
            if (this.mCastOptGoActioner == null) {
                this.mCastOptGoActioner = CastOptGoActioner.createCastOptGoActioner(context, looper, this.mCastOptDeviceCfg);
            }
            if (this.mCastOptChr == null) {
                this.mCastOptChr = CastOptChr.createCastOptChr(context, looper, this, this.mCastOptDeviceCfg);
            }
            if (this.mCastOptScanner == null) {
                this.mCastOptScanner = CastOptScanner.createCastOptScanner(context, looper, this.mCastOptMonitor);
            }
        }
    }

    public static CastOptManager createCastOptManager(Context context, Looper looper) {
        if (sCastOptManager == null) {
            sCastOptManager = new CastOptManager(context, looper);
        }
        return sCastOptManager;
    }

    public static CastOptManager getInstance() {
        return sCastOptManager;
    }

    public void setCastOptScenes(boolean isNeedCastOpt) {
        CastType castType;
        if (!this.mIsCastOptSupported) {
            HwHiLog.i(TAG, false, "cast optimization is not supported", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "setCastOptScenes: %{public}s", new Object[]{String.valueOf(isNeedCastOpt)});
        this.mIsCastOptScenes = isNeedCastOpt;
        CastOptMonitor castOptMonitor = this.mCastOptMonitor;
        if (castOptMonitor != null) {
            castOptMonitor.setMccStaP2pQuotaTime(isNeedCastOpt);
            this.mCastOptMonitor.ctrlRoamChannel(isNeedCastOpt);
        }
        ICastScene iCastScene = this.mCastSceneListener;
        if (iCastScene == null) {
            return;
        }
        if (!this.mIsCastOptScenes || (castType = this.mCastType) == null) {
            this.mCastSceneListener.stopCastScene();
            this.mTriggerType = -1;
            return;
        }
        iCastScene.startCastScene(castType.castType);
    }

    public boolean isCastOptScenes() {
        if (this.mIsCastOptSupported) {
            return this.mIsCastOptScenes;
        }
        HwHiLog.i(TAG, false, "cast optimization is not supported", new Object[0]);
        return false;
    }

    public boolean isCastOptWorking() {
        if (!this.mIsCastOptSupported) {
            HwHiLog.i(TAG, false, "cast optimization is not supported", new Object[0]);
            return false;
        }
        int p2pFreq = this.mCastOptMonitor.getP2pFrequency();
        if (!this.mIsCastOptScenes || !ScanResult.is5GHz(p2pFreq)) {
            return false;
        }
        return true;
    }

    public int getP2pFrequency() {
        if (this.mIsCastOptSupported) {
            return this.mCastOptMonitor.getP2pFrequency();
        }
        HwHiLog.i(TAG, false, "cast optimization is not supported", new Object[0]);
        return 0;
    }

    public byte[] getSelfWifiCfgInfo(int cfgType) {
        if (this.mCastOptDeviceCfg == null) {
            HwHiLog.i(TAG, false, "cast optimization is not supported in getSelfWifiCfgInfo", new Object[0]);
            return new byte[0];
        } else if (((WifiManager) this.mContext.getSystemService("wifi")) == null) {
            return new byte[0];
        } else {
            WifiConfiguration wifiConfig = this.mCastOptMonitor.getCurrentWifiConfig();
            if (wifiConfig != null) {
                HwHiLog.i(TAG, false, "noInternetAccess = %{public}d", new Object[]{Integer.valueOf(wifiConfig.noInternetAccess ? 1 : 0)});
                CastOptDeviceCfg.DeviceWifiInfo selfDeviceCfgInfo = this.mCastOptDeviceCfg.getSelfDeviceCfgInfo();
                if (selfDeviceCfgInfo != null) {
                    selfDeviceCfgInfo.setCurrentApHasInternet(!wifiConfig.noInternetAccess);
                    selfDeviceCfgInfo.setApType(wifiConfig.portalNetwork);
                    selfDeviceCfgInfo.setRouteType(wifiConfig.isHiLinkNetwork ? 1 : 0);
                }
            }
            return this.mCastOptDeviceCfg.getSelfDeviceCfg(cfgType);
        }
    }

    public int setPeerWifiCfgInfo(int cfgType, byte[] cfgData) {
        CastOptDeviceCfg castOptDeviceCfg = this.mCastOptDeviceCfg;
        if (castOptDeviceCfg != null) {
            return castOptDeviceCfg.setPeerWifiCfgInfo(cfgType, cfgData);
        }
        HwHiLog.i(TAG, false, "cast optimization is not supported in setPeerWifiCfgInfo", new Object[0]);
        return -1;
    }

    public int registerWifiCfgCallback(IWifiCfgCallback callback) {
        HwHiLog.i(TAG, false, "registerWiFiCfgCallback", new Object[0]);
        if (this.mCastOptDeviceCfg == null) {
            HwHiLog.i(TAG, false, "cast optimization is not supported in registerWifiCfgCallback", new Object[0]);
            return -1;
        }
        triggerToRecognizeCastType(3);
        setCastOptScenes(true);
        this.mCastOptChr.setTriggerType(2);
        return this.mCastOptDeviceCfg.registerWifiCfgCallback(callback);
    }

    public int unregisterWifiCfgCallback() {
        HwHiLog.i(TAG, false, "unregisterWiFiCfgCallback", new Object[0]);
        if (this.mCastOptDeviceCfg == null) {
            HwHiLog.i(TAG, false, "cast optimization is not supported in unregisterWifiCfgCallback", new Object[0]);
            return -1;
        }
        setCastOptScenes(false);
        return this.mCastOptDeviceCfg.unregisterWifiCfgCallback();
    }

    public int getP2pRecommendChannel() {
        CastOptGoActioner castOptGoActioner = this.mCastOptGoActioner;
        if (castOptGoActioner == null) {
            HwHiLog.i(TAG, false, "cast optimization is not supported in getP2pRecommendChannel", new Object[0]);
            return -1;
        }
        int channel = castOptGoActioner.getP2pRecommendChannel();
        CastOptMonitor castOptMonitor = this.mCastOptMonitor;
        if (castOptMonitor != null) {
            castOptMonitor.setGoSwitchChannelEnableWhenDetectRadar(true);
        }
        this.mCastOptChr.setGoCastOptRecommendChannel(channel);
        HwHiLog.i(TAG, false, "getP2pRecommendChannel  = %{public}d", new Object[]{Integer.valueOf(channel)});
        return channel;
    }

    public WifiScanner.ChannelSpec[] getScanfrequencys() {
        if (this.mIsCastOptSupported) {
            return this.mCastOptGcActioner.getScanfrequencys();
        }
        HwHiLog.i(TAG, false, "cast optimization is not supported", new Object[0]);
        return new WifiScanner.ChannelSpec[0];
    }

    public int getStaFrequency() {
        if (this.mIsCastOptSupported) {
            return this.mCastOptMonitor.getStaFrequency();
        }
        HwHiLog.i(TAG, false, "cast optimization is not supported", new Object[0]);
        return 0;
    }

    public boolean isP2pGroupOwner() {
        CastOptMonitor castOptMonitor = this.mCastOptMonitor;
        return castOptMonitor != null && castOptMonitor.isP2pGroupOwner();
    }

    public boolean isInP2pSharing() {
        return P2pSharingDispatcher.getInstance().isInP2pSharing();
    }

    public void triggerToRecognizeCastType(int mode) {
        int triggerType = convertModeToType(mode);
        if (triggerType == -1) {
            this.mCastType = null;
            this.mTriggerType = -1;
        } else if ((triggerType == 2 || triggerType == 1) && this.mTriggerType == 3) {
            HwHiLog.i(TAG, false, "setTriggerType, mTriggerTypeis already set to be REGISTER_CALLBACK", new Object[0]);
        } else {
            this.mCastType = recognizeCastType(triggerType);
            this.mTriggerType = triggerType;
        }
    }

    private int convertModeToType(int mode) {
        if (mode == 3) {
            return 3;
        }
        if (mode == 3001) {
            return 1;
        }
        if (mode == 3003) {
            return 2;
        }
        HwHiLog.i(TAG, false, "type is invalid, can not recognize this cast scene", new Object[0]);
        return -1;
    }

    private CastType recognizeCastType(int triggerType) {
        CastOptDeviceCfg.DeviceWifiInfo selfDeviceCfgInfo = this.mCastOptDeviceCfg.getSelfDeviceCfgInfo();
        CastOptDeviceCfg.DeviceWifiInfo peerDeviceCfgInfo = this.mCastOptDeviceCfg.getPeerDeviceCfgInfo();
        if (triggerType == 3) {
            if (selfDeviceCfgInfo.getDeviceType() == 0) {
                if (peerDeviceCfgInfo.getDeviceType() == 1) {
                    return CastType.PHONE_CAST_PAD;
                }
                return CastType.PHONE_CAST_PC;
            } else if (selfDeviceCfgInfo.getDeviceType() != 1) {
                HwHiLog.i(TAG, false, "registerCallback can not recognize this scene in reg callback", new Object[0]);
                return null;
            } else if (peerDeviceCfgInfo.getDeviceType() == 0) {
                return CastType.PHONE_CAST_PAD;
            } else {
                return CastType.PAD_CAST_PC;
            }
        } else if (triggerType == 1) {
            if (selfDeviceCfgInfo.getDeviceType() == 0) {
                return CastType.PHONE_MIRACAST;
            }
            if (selfDeviceCfgInfo.getDeviceType() == 1) {
                return CastType.PAD_MIRACAST;
            }
            HwHiLog.i(TAG, false, "can not recognize this scene in miracast", new Object[0]);
            return null;
        } else if (triggerType != 2) {
            HwHiLog.i(TAG, false, "can not recognize this scene", new Object[0]);
            return null;
        } else if (selfDeviceCfgInfo.getDeviceType() == 0) {
            return CastType.PHONE_CAST_PLUS;
        } else {
            if (selfDeviceCfgInfo.getDeviceType() == 1) {
                return CastType.PAD_CAST_PLUS;
            }
            HwHiLog.i(TAG, false, "can not recognize this scene in cast+", new Object[0]);
            return null;
        }
    }

    public void setCastSceneListener(ICastScene castSceneListener) {
        this.mCastSceneListener = castSceneListener;
    }

    public enum CastType {
        PHONE_CAST_PLUS(1),
        PHONE_MIRACAST(2),
        PHONE_CAST_PAD(3),
        PHONE_CAST_PC(4),
        PAD_CAST_PLUS(5),
        PAD_MIRACAST(6),
        PAD_CAST_PC(7);
        
        private int castType;

        private CastType(int castType2) {
            this.castType = castType2;
        }

        public int getCastType() {
            return this.castType;
        }
    }
}
