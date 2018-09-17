package com.android.server.devicepolicy;

import android.app.admin.IDeviceAdminService;
import android.app.admin.IDeviceAdminService.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.PersistentConnection;
import java.io.PrintWriter;
import java.util.List;

public class DeviceAdminServiceController {
    static final boolean DEBUG = false;
    static final String TAG = "DevicePolicyManager";
    @GuardedBy("mLock")
    private final SparseArray<DevicePolicyServiceConnection> mConnections = new SparseArray();
    private final DevicePolicyConstants mConstants;
    final Context mContext;
    private final Handler mHandler;
    private final Injector mInjector;
    final Object mLock = new Object();
    private final DevicePolicyManagerService mService;

    private class DevicePolicyServiceConnection extends PersistentConnection<IDeviceAdminService> {
        public DevicePolicyServiceConnection(int userId, ComponentName componentName) {
            super(DeviceAdminServiceController.TAG, DeviceAdminServiceController.this.mContext, DeviceAdminServiceController.this.mHandler, userId, componentName, DeviceAdminServiceController.this.mConstants.DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC, DeviceAdminServiceController.this.mConstants.DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE, DeviceAdminServiceController.this.mConstants.DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC);
        }

        protected IDeviceAdminService asInterface(IBinder binder) {
            return Stub.asInterface(binder);
        }
    }

    static void debug(String format, Object... args) {
    }

    public DeviceAdminServiceController(DevicePolicyManagerService service, DevicePolicyConstants constants) {
        this.mService = service;
        this.mInjector = service.mInjector;
        this.mContext = this.mInjector.mContext;
        this.mHandler = new Handler(BackgroundThread.get().getLooper());
        this.mConstants = constants;
    }

    private ServiceInfo findService(String packageName, int userId) {
        Intent intent = new Intent("android.app.action.DEVICE_ADMIN_SERVICE");
        intent.setPackage(packageName);
        try {
            ParceledListSlice<ResolveInfo> pls = this.mInjector.getIPackageManager().queryIntentServices(intent, null, 0, userId);
            if (pls == null) {
                return null;
            }
            List<ResolveInfo> list = pls.getList();
            if (list.size() == 0) {
                return null;
            }
            if (list.size() > 1) {
                Log.e(TAG, "More than one DeviceAdminService's found in package " + packageName + ".  They'll all be ignored.");
                return null;
            }
            ServiceInfo si = ((ResolveInfo) list.get(0)).serviceInfo;
            if ("android.permission.BIND_DEVICE_ADMIN".equals(si.permission)) {
                return si;
            }
            Log.e(TAG, "DeviceAdminService " + si.getComponentName().flattenToShortString() + " must be protected with " + "android.permission.BIND_DEVICE_ADMIN" + ".");
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public void startServiceForOwner(String packageName, int userId, String actionForLog) {
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (this.mLock) {
                ServiceInfo service = findService(packageName, userId);
                if (service == null) {
                    debug("Owner package %s on u%d has no service.", packageName, Integer.valueOf(userId));
                    disconnectServiceOnUserLocked(userId, actionForLog);
                } else {
                    if (((PersistentConnection) this.mConnections.get(userId)) != null) {
                        debug("Disconnecting from existing service connection.", packageName, Integer.valueOf(userId));
                        disconnectServiceOnUserLocked(userId, actionForLog);
                    }
                    debug("Owner package %s on u%d has service %s for %s", packageName, Integer.valueOf(userId), service.getComponentName().flattenToShortString(), actionForLog);
                    DevicePolicyServiceConnection conn = new DevicePolicyServiceConnection(userId, service.getComponentName());
                    this.mConnections.put(userId, conn);
                    conn.bind();
                    this.mInjector.binderRestoreCallingIdentity(token);
                }
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

    private void disconnectServiceOnUserLocked(int userId, String actionForLog) {
        DevicePolicyServiceConnection conn = (DevicePolicyServiceConnection) this.mConnections.get(userId);
        if (conn != null) {
            debug("Stopping service for u%d if already running for %s.", Integer.valueOf(userId), actionForLog);
            conn.unbind();
            this.mConnections.remove(userId);
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mConnections.size() == 0) {
                return;
            }
            pw.println();
            pw.print(prefix);
            pw.println("Owner Services:");
            for (int i = 0; i < this.mConnections.size(); i++) {
                int userId = this.mConnections.keyAt(i);
                pw.print(prefix);
                pw.print("  ");
                pw.print("User: ");
                pw.println(userId);
                ((DevicePolicyServiceConnection) this.mConnections.valueAt(i)).dump(prefix + "    ", pw);
            }
            pw.println();
        }
    }
}
