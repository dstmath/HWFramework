package com.android.server.am;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.app.IServiceHooker;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HwServiceHooker {
    private static final int RET_SUCCESS_CODE = 1;
    private static final String TAG = "HwServiceHooker";
    private final Object mLock = new Object();
    private Map<IBinder, IBinder> mServiceConnections;
    private Map<IBinder, String> mServiceHookers;

    public void registerServiceHooker(IBinder hooker, Intent filter) {
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mLock) {
                if (this.mServiceHookers == null) {
                    this.mServiceHookers = new HashMap(1);
                }
                if (filter != null) {
                    String action = filter.getAction();
                    if (action != null) {
                        if (!action.isEmpty()) {
                            this.mServiceHookers.put(hooker, action);
                            return;
                        }
                    }
                    Log.w(TAG, "registerServiceHooker action is empty");
                    return;
                }
                return;
            }
        }
        throw new SecurityException("no permission to registerServiceHooker");
    }

    public void unregisterServiceHooker(IBinder hooker) {
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mLock) {
                if (this.mServiceHookers != null) {
                    this.mServiceHookers.remove(hooker);
                    removeHookerFromConnections(hooker);
                }
            }
            return;
        }
        throw new SecurityException("no permission to unregisterServiceHooker");
    }

    public int bindServiceEx(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String instanceName, String callingPackage, int userId, int defaultResult) {
        synchronized (this.mLock) {
            if (!(this.mServiceHookers == null || connection == null)) {
                if (service != null) {
                    if (!shouldHookPackage(service.getPackage())) {
                        return defaultResult;
                    }
                    String action = service.getAction();
                    if (action == null) {
                        return defaultResult;
                    }
                    for (Map.Entry<IBinder, String> entry : this.mServiceHookers.entrySet()) {
                        if (action.startsWith(entry.getValue())) {
                            service.putExtra("defInvokerUid", Binder.getCallingUid());
                            try {
                                IServiceHooker.Stub.asInterface(entry.getKey()).bindService(caller, token, service, resolvedType, connection, flags, instanceName, callingPackage, userId);
                                Log.i(TAG, "bindServiceEx " + action + " " + connection.asBinder());
                                if (this.mServiceConnections == null) {
                                    this.mServiceConnections = new HashMap(1);
                                }
                                this.mServiceConnections.put(connection.asBinder(), entry.getKey());
                                return 1;
                            } catch (RemoteException e) {
                                Log.e(TAG, "bindServiceEx " + action + " fail.");
                            }
                        }
                    }
                    return defaultResult;
                }
            }
            return defaultResult;
        }
    }

    public int unbindServiceEx(IServiceConnection connection, int defaultResult) {
        synchronized (this.mLock) {
            if (connection != null) {
                if (this.mServiceHookers != null) {
                    if (this.mServiceConnections != null) {
                        Iterator<Map.Entry<IBinder, IBinder>> it = this.mServiceConnections.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<IBinder, IBinder> entry = it.next();
                            if (entry.getKey() == connection.asBinder()) {
                                try {
                                    IServiceHooker.Stub.asInterface(entry.getValue()).unbindService(connection);
                                    Log.i(TAG, "unbindServiceEx " + connection.asBinder());
                                    it.remove();
                                    return 1;
                                } catch (RemoteException e) {
                                    Log.e(TAG, "unbindServiceEx " + connection.asBinder() + " fail.");
                                }
                            }
                        }
                        return defaultResult;
                    }
                }
            }
            return defaultResult;
        }
    }

    private boolean shouldHookPackage(String packageName) {
        return packageName != null && packageName.contains("@");
    }

    private void removeHookerFromConnections(IBinder hooker) {
        Map<IBinder, IBinder> map = this.mServiceConnections;
        if (map != null) {
            Iterator<Map.Entry<IBinder, IBinder>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue() == hooker) {
                    it.remove();
                }
            }
        }
    }
}
