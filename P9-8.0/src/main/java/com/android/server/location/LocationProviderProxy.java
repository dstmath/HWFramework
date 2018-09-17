package com.android.server.location;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import com.android.internal.location.ILocationProvider;
import com.android.internal.location.ILocationProvider.Stub;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.os.TransferPipe;
import com.android.server.ServiceWatcher;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class LocationProviderProxy implements LocationProviderInterface {
    private static final boolean D = false;
    private static final String TAG = "LocationProviderProxy";
    private final Context mContext;
    private boolean mEnabled = false;
    private Object mLock = new Object();
    private final String mName;
    private Runnable mNewServiceWork = new Runnable() {
        public void run() {
            boolean enabled;
            ProviderRequest request;
            WorkSource source;
            ILocationProvider service;
            ProviderProperties properties = null;
            synchronized (LocationProviderProxy.this.mLock) {
                enabled = LocationProviderProxy.this.mEnabled;
                request = LocationProviderProxy.this.mRequest;
                source = LocationProviderProxy.this.mWorksource;
                service = LocationProviderProxy.this.getService();
            }
            if (service != null) {
                try {
                    properties = service.getProperties();
                    if (properties == null) {
                        Log.e(LocationProviderProxy.TAG, LocationProviderProxy.this.mServiceWatcher.getBestPackageName() + " has invalid locatino provider properties");
                    }
                    if (enabled) {
                        service.enable();
                        if (request != null) {
                            service.setRequest(request, source);
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(LocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                }
                synchronized (LocationProviderProxy.this.mLock) {
                    LocationProviderProxy.this.mProperties = properties;
                }
            }
        }
    };
    private ProviderProperties mProperties;
    private ProviderRequest mRequest = null;
    private final ServiceWatcher mServiceWatcher;
    private WorkSource mWorksource = new WorkSource();

    public static LocationProviderProxy createAndBind(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        LocationProviderProxy proxy = new LocationProviderProxy(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private LocationProviderProxy(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        this.mContext = context;
        this.mName = name;
        this.mServiceWatcher = new ServiceWatcher(this.mContext, "LocationProviderProxy-" + name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, this.mNewServiceWork, handler);
    }

    protected LocationProviderProxy(Context context, String name) {
        this.mContext = context;
        this.mName = name;
        this.mServiceWatcher = null;
    }

    private boolean bind() {
        return this.mServiceWatcher.start();
    }

    private ILocationProvider getService() {
        return Stub.asInterface(this.mServiceWatcher.getBinder());
    }

    public String getConnectedPackageName() {
        return this.mServiceWatcher.getBestPackageName();
    }

    public String getName() {
        return this.mName;
    }

    public ProviderProperties getProperties() {
        ProviderProperties providerProperties;
        synchronized (this.mLock) {
            providerProperties = this.mProperties;
        }
        return providerProperties;
    }

    public void enable() {
        synchronized (this.mLock) {
            this.mEnabled = true;
        }
        ILocationProvider service = getService();
        if (service != null) {
            try {
                service.enable();
            } catch (RemoteException e) {
                Log.w(TAG, e);
            } catch (Exception e2) {
                Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
            }
        }
    }

    public void disable() {
        synchronized (this.mLock) {
            this.mEnabled = false;
        }
        ILocationProvider service = getService();
        if (service != null) {
            try {
                service.disable();
            } catch (RemoteException e) {
                Log.w(TAG, e);
            } catch (Exception e2) {
                Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
            }
        }
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEnabled;
        }
        return z;
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        synchronized (this.mLock) {
            this.mRequest = request;
            this.mWorksource = source;
        }
        ILocationProvider service = getService();
        if (service != null) {
            try {
                service.setRequest(request, source);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            } catch (Exception e2) {
                Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0063 A:{Splitter: B:5:0x005b, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0063, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0064, code:
            r7.println("Failed to dump location provider: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.append("REMOTE SERVICE");
        pw.append(" name=").append(this.mName);
        pw.append(" pkg=").append(this.mServiceWatcher.getBestPackageName());
        pw.append(" version=").append("" + this.mServiceWatcher.getBestVersion());
        pw.append(10);
        ILocationProvider service = getService();
        if (service == null) {
            pw.println("service down (null)");
            return;
        }
        pw.flush();
        try {
            TransferPipe.dumpAsync(service.asBinder(), fd, args);
        } catch (Exception e) {
        }
    }

    public int getStatus(Bundle extras) {
        ILocationProvider service = getService();
        if (service == null) {
            return 1;
        }
        try {
            return service.getStatus(extras);
        } catch (RemoteException e) {
            Log.w(TAG, e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
        }
        return 1;
    }

    public long getStatusUpdateTime() {
        ILocationProvider service = getService();
        if (service == null) {
            return 0;
        }
        try {
            return service.getStatusUpdateTime();
        } catch (RemoteException e) {
            Log.w(TAG, e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
        }
        return 0;
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        ILocationProvider service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.sendExtraCommand(command, extras);
        } catch (RemoteException e) {
            Log.w(TAG, e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcher.getBestPackageName(), e2);
        }
        return false;
    }
}
