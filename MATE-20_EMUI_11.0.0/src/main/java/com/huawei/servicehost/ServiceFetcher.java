package com.huawei.servicehost;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.servicehost.IServiceHost;
import com.huawei.servicehost.IServiceHostClient;

public class ServiceFetcher implements IBinder.DeathRecipient {
    private static final boolean DEBUG = false;
    private static final Object LOCK = new Object();
    private static final String SERVICE_NAME = "com.huawei.servicehost";
    private static final String TAG = "HwServiceHost";
    private static ServiceFetcher serviceFetcher;
    private final IServiceHostClient client = new IServiceHostClient.Stub() {
        /* class com.huawei.servicehost.ServiceFetcher.AnonymousClass1 */
    };
    private final Object lock = new Object();
    private IServiceHost serviceHost = null;

    private ServiceFetcher() {
        getServiceHost();
    }

    public static ServiceFetcher get() {
        ServiceFetcher serviceFetcher2;
        synchronized (LOCK) {
            if (serviceFetcher == null) {
                serviceFetcher = new ServiceFetcher();
            }
            serviceFetcher2 = serviceFetcher;
        }
        return serviceFetcher2;
    }

    public static boolean checkConnected() {
        Log.i(TAG, "Checking servicehost.");
        if (ServiceManager.getService(SERVICE_NAME) == null) {
            Log.i(TAG, "Do not connect to service!");
            return DEBUG;
        }
        Log.i(TAG, "Connected to service!");
        return true;
    }

    public IBufferShareService getBufferShareService() {
        synchronized (this.lock) {
            try {
                if (this.serviceHost == null) {
                    return null;
                }
                return this.serviceHost.getBufferShareService();
            } catch (RemoteException e) {
                Log.e(TAG, "failed.");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public IImageProcessService getImageProcessService() {
        synchronized (this.lock) {
            try {
                if (this.serviceHost == null) {
                    return null;
                }
                return this.serviceHost.getImageProcessService();
            } catch (RemoteException e) {
                Log.e(TAG, "failed.");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private IServiceHost getServiceHost() {
        synchronized (this.lock) {
            if (this.serviceHost != null) {
                return this.serviceHost;
            }
            Log.i(TAG, "Connecting to camera servicehost.");
            IBinder serviceBinder = ServiceManager.getService(SERVICE_NAME);
            if (serviceBinder == null) {
                Log.e(TAG, "Connect to camera servicehost failed.");
                return null;
            }
            try {
                serviceBinder.linkToDeath(this, 0);
                IServiceHost host = IServiceHost.Stub.asInterface(serviceBinder);
                if (host != null) {
                    try {
                        host.connect(this.client);
                        this.serviceHost = host;
                        return this.serviceHost;
                    } catch (RemoteException e) {
                        Log.e(TAG, "servicehost connect client failed.");
                        return null;
                    }
                } else {
                    Log.e(TAG, "camera servicehost null.");
                    return null;
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "linkToDeath to camera servicehost failed.");
                return null;
            }
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (LOCK) {
            serviceFetcher = null;
        }
        synchronized (this.lock) {
            Log.w(TAG, "camera servicehost died.");
            this.serviceHost = null;
        }
    }
}
