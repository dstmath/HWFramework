package com.android.server.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.content.pm.IExtServiceProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExtServiceProvider {
    private static final String TAG = "ExtServiceProvider";
    private final Object mLock = new Object();
    private Map<IBinder, String> mServiceProviders;

    public void registerExtServiceProvider(IExtServiceProvider extServiceProvider, Intent filter) {
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mLock) {
                if (this.mServiceProviders == null) {
                    this.mServiceProviders = new HashMap(1);
                }
                if (filter != null) {
                    String action = filter.getAction();
                    if (action != null) {
                        if (!action.isEmpty()) {
                            if (extServiceProvider == null) {
                                Log.w(TAG, "registerExtServiceProvider provider is null");
                                return;
                            } else {
                                this.mServiceProviders.put(extServiceProvider.asBinder(), action);
                                return;
                            }
                        }
                    }
                    Log.w(TAG, "registerExtServiceProvider action is empty");
                    return;
                }
                return;
            }
        }
        throw new SecurityException("no permission to registerExtServiceProvider");
    }

    public void unregisterExtServiceProvider(IExtServiceProvider extServiceProvider) {
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mLock) {
                if (!(this.mServiceProviders == null || extServiceProvider == null)) {
                    this.mServiceProviders.remove(extServiceProvider.asBinder());
                }
            }
            return;
        }
        throw new SecurityException("no permission to unregisterExtServiceProvider");
    }

    public ResolveInfo[] queryExtService(String action, String packageName) {
        synchronized (this.mLock) {
            if (this.mServiceProviders != null) {
                if (action != null) {
                    Log.i(TAG, "queryExtService " + action + " packageName: " + packageName);
                    Iterator<Map.Entry<IBinder, String>> it = this.mServiceProviders.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<IBinder, String> entry = it.next();
                        if (action.startsWith(entry.getValue())) {
                            try {
                                return IExtServiceProvider.Stub.asInterface(entry.getKey()).queryExtService(action, packageName);
                            } catch (RemoteException e) {
                                Log.w(TAG, "queryExtService action: " + action);
                            }
                        }
                    }
                    return null;
                }
            }
            return null;
        }
    }
}
