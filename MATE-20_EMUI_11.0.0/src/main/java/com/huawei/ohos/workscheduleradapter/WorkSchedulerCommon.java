package com.huawei.ohos.workscheduleradapter;

import java.util.ArrayList;
import java.util.List;
import ohos.bundle.BundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.workscheduler.WorkInfo;

public final class WorkSchedulerCommon {
    private static final int APP_ILLEGAL_UID = -1;
    private static final int BUNDLE_SERVER_ID = 401;
    private static final Object INSTANCE_LOCK = new Object();
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkSchedulerCommon");
    private static final int PER_USER_RANGE = 100000;
    private static final int SYSTEM_UID = 1000;
    private static volatile BundleManager bundleManagerInstance;

    private WorkSchedulerCommon() {
    }

    public static BundleManager getBundleManager() {
        if (bundleManagerInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (bundleManagerInstance == null) {
                    IRemoteObject sysAbility = SysAbilityManager.getSysAbility(401);
                    if (sysAbility == null) {
                        HiLog.error(LOG_LABEL, "getBundleManager failed and service is null!", new Object[0]);
                        return null;
                    }
                    bundleManagerInstance = new BundleManager(sysAbility);
                }
            }
        }
        return bundleManagerInstance;
    }

    public static int getUserIdFromUid(int i) {
        return i / PER_USER_RANGE;
    }

    public static boolean checkClientPermission(WorkInfo workInfo, int i) {
        if (!workInfo.isWorkInfoValid()) {
            HiLog.error(LOG_LABEL, "checkClientPermission failed, workInfo is invalid", new Object[0]);
            return false;
        } else if (i == getUidFromBundleName(workInfo.getBundleName(), getUserIdFromUid(i))) {
            return true;
        } else {
            return false;
        }
    }

    public static int getUidFromBundleName(String str, int i) {
        BundleManager bundleManager = getBundleManager();
        if (bundleManager == null) {
            HiLog.error(LOG_LABEL, "checkClientPermission failed, Get BundleManager failed!!", new Object[0]);
            return -1;
        }
        try {
            return bundleManager.getUidByBundleName(str, i);
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "checkConPrmission failed, can not bundleManager!", new Object[0]);
            return -1;
        }
    }

    public static List<String> getBundleNameFormUid(int i) {
        ArrayList arrayList = new ArrayList();
        BundleManager bundleManager = getBundleManager();
        if (bundleManager == null) {
            HiLog.error(LOG_LABEL, "getBundleNameFormUid failed, Get BundleManager failed!!", new Object[0]);
            return arrayList;
        }
        try {
            return bundleManager.getBundlesForUid(i);
        } catch (SecurityException | RemoteException unused) {
            HiLog.error(LOG_LABEL, "getBundleNameFormUid failed, can not bundleManager!", new Object[0]);
            return arrayList;
        }
    }
}
