package ohos.security.dpermissionkit;

import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.security.permission.BundleLabelInfo;

public final class DPermissionKit {
    private static final int FAILTURE_CODE = -1;
    private static final Object INSTANCE_LOCK = new Object();
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY_DPERMISSION, "DPermissionKit");
    private static final int SUB_DOMAIN_SECURITY_DPERMISSION = 218115841;
    private static volatile DPermissionKit sInstance;
    private final DPermissionKitProxy mDPermissionKitProxy = DPermissionKitProxy.getInstance();

    private DPermissionKit() {
    }

    public static DPermissionKit getInstance() {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new DPermissionKit();
                }
            }
        }
        return sInstance;
    }

    public int allocateDuid(String str, int i) {
        try {
            return this.mDPermissionKitProxy.allocateDuid(str, i);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to allocateDuid because RemoteException", new Object[0]);
            return -1;
        }
    }

    public int queryDuid(String str, int i) {
        try {
            return this.mDPermissionKitProxy.queryDuid(str, i);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to queryDuid because RemoteException", new Object[0]);
            return -1;
        }
    }

    public int delDuid(String str, int i) {
        try {
            return this.mDPermissionKitProxy.delDuid(str, i);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to delDuid by deviceId and rUid because RemoteException", new Object[0]);
            return -1;
        }
    }

    public int delDuid(String str) {
        try {
            return this.mDPermissionKitProxy.delDuid(str);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to delDuid by deviceId because RemoteException", new Object[0]);
            return -1;
        }
    }

    public int checkDPermission(int i, String str) {
        try {
            return this.mDPermissionKitProxy.checkDPermission(i, str);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to checkDPermission because RemoteException", new Object[0]);
            return -1;
        }
    }

    public int notifyUidPermissionChanged(int i) {
        try {
            return this.mDPermissionKitProxy.notifyUidPermissionChanged(i);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to notifyUidPermissionChanged because RemoteException", new Object[0]);
            return -1;
        }
    }

    public int notifyAppStatusChanged(int i) {
        try {
            return this.mDPermissionKitProxy.notifyAppStatusChanged(i);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to notifyAppStatusChanged because RemoteException", new Object[0]);
            return -1;
        }
    }

    public Optional<BundleLabelInfo> getBundleLabelInfo(int i) {
        try {
            return this.mDPermissionKitProxy.getBundleLabelInfo(i);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to getBundleLabelInfo because RemoteException", new Object[0]);
            return Optional.empty();
        }
    }
}
