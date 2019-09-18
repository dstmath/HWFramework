package com.android.server.security.hsm;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Slog;
import com.android.server.security.core.IHwSecurityPlugin;
import huawei.android.security.IHwSystemManagerPlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwSystemManagerPlugin extends IHwSystemManagerPlugin.Stub implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            HwSystemManagerPlugin access$100;
            synchronized (HwSystemManagerPlugin.serviceLock) {
                if (HwSystemManagerPlugin.sInstance == null) {
                    HwSystemManagerPlugin unused = HwSystemManagerPlugin.sInstance = new HwSystemManagerPlugin(context);
                }
                access$100 = HwSystemManagerPlugin.sInstance;
            }
            return access$100;
        }

        public String getPluginPermission() {
            return null;
        }
    };
    private static final String PERMISSION = "com.huawei.permission.HWSYSTEMMANAGER_PLUGIN";
    private static final int RET_FAIL = 1;
    private static final int RET_SUCCESS = 0;
    private static final String TAG = "HwSystemManagerPlugin";
    /* access modifiers changed from: private */
    public static volatile HwSystemManagerPlugin sInstance;
    private static final Set<String> sStartComponetBlackList = new HashSet();
    /* access modifiers changed from: private */
    public static final Object serviceLock = new Object();
    private Context mContext;

    private HwSystemManagerPlugin(Context context) {
        this.mContext = context;
    }

    public static HwSystemManagerPlugin getInstance(Context context) {
        HwSystemManagerPlugin hwSystemManagerPlugin;
        synchronized (serviceLock) {
            if (sInstance == null) {
                sInstance = new HwSystemManagerPlugin(context);
            }
            hwSystemManagerPlugin = sInstance;
        }
        return hwSystemManagerPlugin;
    }

    public void onStart() {
        Slog.d(TAG, "HwAddViewChecker - onStart");
    }

    public void onStop() {
        Slog.d(TAG, "HwAddViewChecker - onStop");
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.hsm.HwSystemManagerPlugin, android.os.IBinder] */
    public IBinder asBinder() {
        return this;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0027, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0029, code lost:
        return false;
     */
    public boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        checkPermission(PERMISSION);
        synchronized (sStartComponetBlackList) {
            if (callerPackage == null) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (type != 1) {
                if (type != 3) {
                    if (sStartComponetBlackList.contains(callerPackage) && !callerPackage.equals(calleePackage)) {
                        return true;
                    }
                }
            }
        }
    }

    public int updateAddViewData(Bundle data, int operation) {
        checkPermission(PERMISSION);
        return HwAddViewManager.getInstance(this.mContext).updateAddViewData(data, operation);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        return;
     */
    public void setStartComponetBlackList(List<String> pkgs) {
        checkPermission(PERMISSION);
        synchronized (sStartComponetBlackList) {
            sStartComponetBlackList.clear();
            if (pkgs != null) {
                if (!pkgs.isEmpty()) {
                    sStartComponetBlackList.addAll(pkgs);
                }
            }
        }
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}
