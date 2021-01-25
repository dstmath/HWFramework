package ohos.wifi;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public final class WifiEnhancer {
    private static final Object ENHANCER_LOCK = new Object();
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_ENHANCER, "WifiEnhancer");
    private static volatile WifiEnhancer sInstance;
    private final Context mContext;
    private final WifiEnhancerProxy mWifiEnhancerProxy = WifiEnhancerProxy.getInstance();

    private WifiEnhancer(Context context) {
        this.mContext = context;
    }

    public static WifiEnhancer getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ENHANCER_LOCK) {
                if (sInstance == null) {
                    sInstance = new WifiEnhancer(context);
                }
            }
        }
        return sInstance;
    }

    public boolean bindMpNetwork(WifiMpConfig wifiMpConfig) {
        try {
            return this.mWifiEnhancerProxy.bindMpNetwork(wifiMpConfig);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to bindMpNetwork config", new Object[0]);
            return false;
        }
    }

    public boolean unBindMpNetwork(WifiMpConfig wifiMpConfig) {
        try {
            return this.mWifiEnhancerProxy.unBindMpNetwork(wifiMpConfig);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to unBindMpNetwork config", new Object[0]);
            return false;
        }
    }

    public boolean bindMpNetwork() {
        try {
            return this.mWifiEnhancerProxy.bindMpNetwork();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to bindMpNetwork", new Object[0]);
            return false;
        }
    }

    public boolean unBindMpNetwork() {
        try {
            return this.mWifiEnhancerProxy.unBindMpNetwork();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to unBindMpNetwork", new Object[0]);
            return false;
        }
    }

    public boolean isInMpLinkState(int i) {
        try {
            return this.mWifiEnhancerProxy.isInMpLinkState(i);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to isInMpLinkState uid: %{public}d", Integer.valueOf(i));
            return false;
        }
    }

    public boolean isInMpLinkState() {
        try {
            if (this.mContext == null || this.mContext.getAbilityInfo() == null) {
                return this.mWifiEnhancerProxy.isInMpLinkState(-1);
            }
            HiLog.info(LABEL, "isInMpLinkState has non-empty context", new Object[0]);
            return this.mWifiEnhancerProxy.isInMpLinkState(-1);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to isInMpLinkState", new Object[0]);
            return false;
        }
    }

    public boolean connectDc(WifiDeviceConfig wifiDeviceConfig) {
        try {
            return this.mWifiEnhancerProxy.connectDc(wifiDeviceConfig);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to connectDc", new Object[0]);
            return false;
        }
    }

    public boolean isWifiDcActive() {
        try {
            return this.mWifiEnhancerProxy.isWifiDcActive();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to isWifiDcActive", new Object[0]);
            return false;
        }
    }

    public boolean disconnectDc() {
        try {
            return this.mWifiEnhancerProxy.disconnectDc();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to disconnectDc", new Object[0]);
            return false;
        }
    }
}
