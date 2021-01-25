package com.huawei.server.security.privacyability;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.server.security.core.IHwSecurityPlugin;
import huawei.android.security.privacyability.IIDAnonymizationManager;
import java.util.List;

public class IDAnonymizationManagerService extends IIDAnonymizationManager.Stub implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.privacyability.IDAnonymizationManagerService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(IDAnonymizationManagerService.TAG, "Create IDAnonymizationManagerService");
            return new IDAnonymizationManagerService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static final String ID_ANONYMIZATION_MANAGER_PERMISSION = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final boolean IS_ID_ANONYMIZATION_MANAGER = "true".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.id_anonmyzation_manager", "true"));
    private static final String TAG = "IDAnonymizationManagerService";
    private Context mContext;
    private IDAnonymizationBroadcastReceiver mIDAnonymizationBroadcastReceiver;

    private IDAnonymizationManagerService(Context context) {
        this.mContext = context;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.huawei.server.security.privacyability.IDAnonymizationManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        this.mContext.enforceCallingOrSelfPermission(ID_ANONYMIZATION_MANAGER_PERMISSION, "does not hava ID anonymization manager permission");
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        if (this.mIDAnonymizationBroadcastReceiver == null) {
            this.mIDAnonymizationBroadcastReceiver = new IDAnonymizationBroadcastReceiver(this.mContext);
        }
        this.mIDAnonymizationBroadcastReceiver.onStart();
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        IDAnonymizationBroadcastReceiver iDAnonymizationBroadcastReceiver = this.mIDAnonymizationBroadcastReceiver;
        if (iDAnonymizationBroadcastReceiver != null) {
            iDAnonymizationBroadcastReceiver.onStop();
        }
    }

    public static boolean isNeedRegisterService() {
        return IS_ID_ANONYMIZATION_MANAGER;
    }

    private List<ActivityManager.RunningAppProcessInfo> getRunningProcesses() {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager != null) {
            return activityManager.getRunningAppProcesses();
        }
        Log.e(TAG, "get process status, get ams service failed");
        return null;
    }

    private String getAppNameByPid(int pid) {
        List<ActivityManager.RunningAppProcessInfo> processes = getRunningProcesses();
        if (processes == null) {
            Log.e(TAG, "get app name, get running process failed");
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return null;
    }

    private String getCallingPackageName() throws SecurityException {
        String packageName = getAppNameByPid(Binder.getCallingPid());
        if (packageName != null) {
            String[] packageParts = packageName.split(":");
            return packageParts.length > 0 ? packageParts[0] : packageName;
        }
        throw new SecurityException("get calling package name failed");
    }

    private int getCallingUserId() {
        return UserHandleEx.getUserId(Binder.getCallingUid());
    }

    public String getCUID() {
        this.mContext.enforceCallingOrSelfPermission(ID_ANONYMIZATION_MANAGER_PERMISSION, "does not hava ID anonymization manager permission");
        return IDAnonymizationDB.getInstance().getCUID(getCallingUserId(), getCallingPackageName());
    }

    public String getCFID(String containerID, String contentProviderTag) {
        this.mContext.enforceCallingOrSelfPermission(ID_ANONYMIZATION_MANAGER_PERMISSION, "does not hava ID anonymization manager permission");
        return ContainerForwardID.getInstance().generateID(containerID, contentProviderTag);
    }

    public int resetCUID() {
        this.mContext.enforceCallingOrSelfPermission(ID_ANONYMIZATION_MANAGER_PERMISSION, "does not hava ID anonymization manager permission");
        return IDAnonymizationDB.getInstance().removeCUID(getCallingUserId(), getCallingPackageName());
    }
}
