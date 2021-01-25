package com.huawei.server.security.hsm;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import com.huawei.android.util.SlogEx;
import com.huawei.server.security.core.IHwSecurityPlugin;
import huawei.android.security.IHwSystemManagerPlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwSystemManagerPlugin extends IHwSystemManagerPlugin.Stub implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.hsm.HwSystemManagerPlugin.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            HwSystemManagerPlugin hwSystemManagerPlugin;
            synchronized (HwSystemManagerPlugin.SERVICE_LOCK) {
                if (HwSystemManagerPlugin.sInstance == null) {
                    HwSystemManagerPlugin unused = HwSystemManagerPlugin.sInstance = new HwSystemManagerPlugin(context);
                }
                hwSystemManagerPlugin = HwSystemManagerPlugin.sInstance;
            }
            return hwSystemManagerPlugin;
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static final String PERMISSION = "com.huawei.permission.HWSYSTEMMANAGER_PLUGIN";
    private static final int RET_FAIL = 1;
    private static final int RET_SUCCESS = 0;
    private static final Object SERVICE_LOCK = new Object();
    private static final Set<String> START_COMPONENT_BLACK_LISTS = new HashSet();
    private static final String TAG = "HwSystemManagerPlugin";
    private static volatile HwSystemManagerPlugin sInstance;
    private Context mContext;

    private HwSystemManagerPlugin(Context context) {
        this.mContext = context;
    }

    public static HwSystemManagerPlugin getInstance(Context context) {
        HwSystemManagerPlugin hwSystemManagerPlugin;
        synchronized (SERVICE_LOCK) {
            if (sInstance == null) {
                sInstance = new HwSystemManagerPlugin(context);
            }
            hwSystemManagerPlugin = sInstance;
        }
        return hwSystemManagerPlugin;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        SlogEx.d(TAG, "HwAddViewChecker - onStart");
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        SlogEx.d(TAG, "HwAddViewChecker - onStop");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.hsm.HwSystemManagerPlugin */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    public boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        checkPermission(PERMISSION);
        synchronized (START_COMPONENT_BLACK_LISTS) {
            if (callerPackage == null) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                if (type != 1) {
                    if (type != 3) {
                        if (!START_COMPONENT_BLACK_LISTS.contains(callerPackage) || callerPackage.equals(calleePackage)) {
                            return false;
                        }
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public int updateAddViewData(Bundle data, int operation) {
        checkPermission(PERMISSION);
        return HwAddViewManager.getInstance(this.mContext).updateAddViewData(data, operation);
    }

    public void setStartComponetBlackList(List<String> pkgs) {
        checkPermission(PERMISSION);
        synchronized (START_COMPONENT_BLACK_LISTS) {
            START_COMPONENT_BLACK_LISTS.clear();
            if (pkgs != null) {
                if (!pkgs.isEmpty()) {
                    START_COMPONENT_BLACK_LISTS.addAll(pkgs);
                }
            }
        }
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}
