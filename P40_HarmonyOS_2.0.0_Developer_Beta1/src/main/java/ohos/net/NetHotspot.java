package ohos.net;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class NetHotspot {
    private static final String DEFAULT_CALLER = "android";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "NETHOTSPOT");
    private static final int TAG_RX_BYTES = 0;
    private static final int TAG_TX_BYTES = 2;
    private final Context mContext;
    private final NetManagerProxy mNetManagerProxy = NetManagerProxy.getInstance();

    private NetHotspot(Context context) {
        this.mContext = context;
    }

    public static NetHotspot getInstance(Context context) {
        return new NetHotspot(context);
    }

    public boolean startUsbHotspot() {
        String str = DEFAULT_CALLER;
        try {
            if (!(this.mContext == null || this.mContext.getAbilityInfo() == null)) {
                str = this.mContext.getAbilityInfo().getBundleName();
                HiLog.warn(LABEL, "app name: %{public}s", str);
            }
            return this.mNetManagerProxy.enableUsbHotspot(true, str);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to startUsbHotspot: ", new Object[0]);
            return false;
        }
    }

    public boolean stopUsbHotspot() {
        String str = DEFAULT_CALLER;
        try {
            if (!(this.mContext == null || this.mContext.getAbilityInfo() == null)) {
                str = this.mContext.getAbilityInfo().getBundleName();
            }
            return this.mNetManagerProxy.enableUsbHotspot(false, str);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to stopUsbHotspot: ", new Object[0]);
            return false;
        }
    }

    public String[] getNetHotspotAbleIfaces() {
        try {
            return this.mNetManagerProxy.getNetHotspotAbleIfaces();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getNetHotspotAbleIfaces: ", new Object[0]);
            return null;
        }
    }

    public String[] getHotspotIfaces() {
        try {
            return this.mNetManagerProxy.getNetHotspotIfaces();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getHotspotIfaces: ", new Object[0]);
            return null;
        }
    }

    public boolean isHotspotSupported() {
        String str = DEFAULT_CALLER;
        try {
            if (!(this.mContext == null || this.mContext.getAbilityInfo() == null)) {
                str = this.mContext.getAbilityInfo().getBundleName();
                HiLog.warn(LABEL, "app name: %{public}s", str);
            }
            return this.mNetManagerProxy.isHotspotSupported(str);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to isHotspotSupported", new Object[0]);
            return false;
        }
    }

    public static long getTxBytes() {
        try {
            return NetManagerProxy.getInstance().getHotspotStats(2);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getUidTxBytes.", new Object[0]);
            return 0;
        }
    }

    public static long getRxBytes() {
        try {
            return NetManagerProxy.getInstance().getHotspotStats(0);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getUidTxBytes.", new Object[0]);
            return 0;
        }
    }
}
