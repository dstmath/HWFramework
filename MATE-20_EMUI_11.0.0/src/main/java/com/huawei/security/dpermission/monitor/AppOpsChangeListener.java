package com.huawei.security.dpermission.monitor;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.DPermissionUtils;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.dpermissionkit.DPermissionKit;

public class AppOpsChangeListener implements AppOpsManager.OnOpChangedListener {
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "AppOpsChangeListener");
    private static final Object INSTANCE_LOCK = new Object();
    private static volatile AppOpsChangeListener sInstance;
    private Context mContext;

    private AppOpsChangeListener(Context context) {
        this.mContext = context;
    }

    public static AppOpsChangeListener getInstance(Context context) {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new AppOpsChangeListener(context);
                }
            }
        }
        return sInstance;
    }

    public void register() {
        Context context = this.mContext;
        if (context == null) {
            HiLog.error(DPERMISSION_LABEL, "registerOpsChangeListener context is null", new Object[0]);
            return;
        }
        Object systemService = context.getSystemService("appops");
        AppOpsManager appOpsManager = systemService instanceof AppOpsManager ? (AppOpsManager) systemService : null;
        if (appOpsManager == null) {
            HiLog.error(DPERMISSION_LABEL, "registerOpsChangeListener get appOpsManager fail!", new Object[0]);
            return;
        }
        Set<String> needSyncOpSet = DPermissionUtils.getNeedSyncOpSet();
        if (needSyncOpSet.isEmpty()) {
            HiLog.debug(DPERMISSION_LABEL, "no appops need to listener", new Object[0]);
            return;
        }
        for (String str : needSyncOpSet) {
            String permissionToOp = AppOpsManager.permissionToOp(str);
            if (permissionToOp == null) {
                HiLog.error(DPERMISSION_LABEL, "permission: %{public}s convert to op failed!", new Object[]{str});
            } else {
                appOpsManager.startWatchingMode(permissionToOp, null, this);
            }
        }
    }

    @Override // android.app.AppOpsManager.OnOpChangedListener
    public void onOpChanged(String str, String str2) {
        Context context = this.mContext;
        PackageManager packageManager = context != null ? context.getPackageManager() : null;
        if (packageManager == null) {
            HiLog.error(DPERMISSION_LABEL, "onOpChanged get packageManager failed", new Object[0]);
            return;
        }
        try {
            int packageUidAsUser = packageManager.getPackageUidAsUser(str2, ActivityManagerEx.getCurrentUser());
            HiLog.debug(DPERMISSION_LABEL, "opsChanged: op: %{public}s, pkgName: %{public}s, uid: %{public}d", new Object[]{str, str2, Integer.valueOf(packageUidAsUser)});
            DPermissionKit.getInstance().notifyUidPermissionChanged(packageUidAsUser);
        } catch (PackageManager.NameNotFoundException unused) {
            HiLog.error(DPERMISSION_LABEL, "onOpChanged PackageManager.NameNotFoundException: %{public}s", new Object[]{str2});
        }
    }
}
