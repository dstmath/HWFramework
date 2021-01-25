package com.huawei.security.dpermission;

import android.content.Context;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.security.dpermission.fetcher.AppInfoFetcher;
import com.huawei.security.dpermission.monitor.DPermissionMonitor;
import com.huawei.security.dpermission.service.DPermissionZ2aProxyService;
import com.huawei.security.dpermission.service.HwDPermissionService;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DPermissionInitializer {
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPERMISSION_LOG_ID, "DPermissionInitializer");
    public static final int DPERMISSION_LOG_ID = 218115841;
    private static final Object INSTANCE_LOCK = new Object();
    private static volatile DPermissionInitializer sInstance;

    private DPermissionInitializer() {
    }

    public static DPermissionInitializer getInstance() {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new DPermissionInitializer();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        HiLog.info(DPERMISSION_LABEL, "Starting init SA and Service begin", new Object[0]);
        if (context == null) {
            HiLog.error(DPERMISSION_LABEL, "DPermissionInitializer init context is null.", new Object[0]);
            return;
        }
        try {
            DPermissionZ2aProxyService.getInstance(context).start();
            DPermissionMonitor.getInstance(context).register();
            ServiceManagerEx.addService("com.huawei.security.dpermission.service.HwDPermissionService", new HwDPermissionService(context));
            AppInfoFetcher.getInstance().init(context);
        } catch (SecurityException unused) {
            HiLog.error(DPERMISSION_LABEL, "Add HwDPermissionService failed, encountered SecurityException.", new Object[0]);
        } catch (Exception unused2) {
            HiLog.error(DPERMISSION_LABEL, "Add HwDPermissionService failed, encountered Exception.", new Object[0]);
        }
    }
}
