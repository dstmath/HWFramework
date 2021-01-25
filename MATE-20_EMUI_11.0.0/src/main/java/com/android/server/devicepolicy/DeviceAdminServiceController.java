package com.android.server.devicepolicy;

import android.app.admin.DeviceAdminService;
import android.app.admin.IDeviceAdminService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.PersistentConnection;
import com.android.server.appbinding.AppBindingUtils;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;

public class DeviceAdminServiceController {
    static final boolean DEBUG = false;
    static final String TAG = "DevicePolicyManager";
    @GuardedBy({"mLock"})
    private final SparseArray<DevicePolicyServiceConnection> mConnections = new SparseArray<>();
    private final DevicePolicyConstants mConstants;
    final Context mContext;
    private final Handler mHandler;
    private final DevicePolicyManagerService.Injector mInjector;
    final Object mLock = new Object();
    private final DevicePolicyManagerService mService;

    static void debug(String format, Object... args) {
    }

    /* access modifiers changed from: private */
    public class DevicePolicyServiceConnection extends PersistentConnection<IDeviceAdminService> {
        public DevicePolicyServiceConnection(int userId, ComponentName componentName) {
            super(DeviceAdminServiceController.TAG, DeviceAdminServiceController.this.mContext, DeviceAdminServiceController.this.mHandler, userId, componentName, DeviceAdminServiceController.this.mConstants.DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC, DeviceAdminServiceController.this.mConstants.DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE, DeviceAdminServiceController.this.mConstants.DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC, DeviceAdminServiceController.this.mConstants.DAS_DIED_SERVICE_STABLE_CONNECTION_THRESHOLD_SEC);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.am.PersistentConnection
        public int getBindFlags() {
            return DumpState.DUMP_HANDLE;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.am.PersistentConnection
        public IDeviceAdminService asInterface(IBinder binder) {
            return IDeviceAdminService.Stub.asInterface(binder);
        }
    }

    public DeviceAdminServiceController(DevicePolicyManagerService service, DevicePolicyConstants constants) {
        this.mService = service;
        this.mInjector = service.mInjector;
        this.mContext = this.mInjector.mContext;
        this.mHandler = new Handler(BackgroundThread.get().getLooper());
        this.mConstants = constants;
    }

    private ServiceInfo findService(String packageName, int userId) {
        return AppBindingUtils.findService(packageName, userId, "android.app.action.DEVICE_ADMIN_SERVICE", "android.permission.BIND_DEVICE_ADMIN", DeviceAdminService.class, this.mInjector.getIPackageManager(), new StringBuilder());
    }

    public void startServiceForOwner(String packageName, int userId, String actionForLog) {
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (this.mLock) {
                ServiceInfo service = findService(packageName, userId);
                if (service == null) {
                    debug("Owner package %s on u%d has no service.", packageName, Integer.valueOf(userId));
                    disconnectServiceOnUserLocked(userId, actionForLog);
                    return;
                }
                if (this.mConnections.get(userId) != null) {
                    debug("Disconnecting from existing service connection.", packageName, Integer.valueOf(userId));
                    disconnectServiceOnUserLocked(userId, actionForLog);
                }
                debug("Owner package %s on u%d has service %s for %s", packageName, Integer.valueOf(userId), service.getComponentName().flattenToShortString(), actionForLog);
                DevicePolicyServiceConnection conn = new DevicePolicyServiceConnection(userId, service.getComponentName());
                this.mConnections.put(userId, conn);
                conn.bind();
                this.mInjector.binderRestoreCallingIdentity(token);
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    public void stopServiceForOwner(int userId, String actionForLog) {
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (this.mLock) {
                disconnectServiceOnUserLocked(userId, actionForLog);
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    @GuardedBy({"mLock"})
    private void disconnectServiceOnUserLocked(int userId, String actionForLog) {
        DevicePolicyServiceConnection conn = this.mConnections.get(userId);
        if (conn != null) {
            debug("Stopping service for u%d if already running for %s.", Integer.valueOf(userId), actionForLog);
            conn.unbind();
            this.mConnections.remove(userId);
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mConnections.size() != 0) {
                pw.println();
                pw.print(prefix);
                pw.println("Owner Services:");
                for (int i = 0; i < this.mConnections.size(); i++) {
                    int userId = this.mConnections.keyAt(i);
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print("User: ");
                    pw.println(userId);
                    this.mConnections.valueAt(i).dump(prefix + "    ", pw);
                }
                pw.println();
            }
        }
    }
}
