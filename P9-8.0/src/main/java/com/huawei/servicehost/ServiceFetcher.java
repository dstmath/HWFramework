package com.huawei.servicehost;

import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.servicehost.IServiceHostClient.Stub;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class ServiceFetcher implements DeathRecipient {
    private static final boolean DEBUG = false;
    private static final String SERVICE_NAME = "com.huawei.servicehost";
    private static final String TAG = "HwServiceHost";
    private static final Object gLock = new Object();
    private static ServiceFetcher gServiceFetcher;
    private final IServiceHostClient mClient = new Stub() {
    };
    private final Object mLock = new Object();
    private IServiceHost mServiceHost = null;

    private ServiceFetcher() {
        getServiceHost();
    }

    public static ServiceFetcher get() {
        ServiceFetcher serviceFetcher;
        synchronized (gLock) {
            if (gServiceFetcher == null) {
                gServiceFetcher = new ServiceFetcher();
            }
            serviceFetcher = gServiceFetcher;
        }
        return serviceFetcher;
    }

    public IBufferShareService getBufferShareService() {
        synchronized (this.mLock) {
            try {
                if (this.mServiceHost != null) {
                    IBufferShareService bufferShareService = this.mServiceHost.getBufferShareService();
                    return bufferShareService;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "failed.");
            }
        }
        return null;
    }

    public IImageProcessService getImageProcessService() {
        synchronized (this.mLock) {
            try {
                if (this.mServiceHost != null) {
                    IImageProcessService imageProcessService = this.mServiceHost.getImageProcessService();
                    return imageProcessService;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "failed.");
            }
        }
        return null;
    }

    public IServiceHost getServiceHost() {
        synchronized (this.mLock) {
            IServiceHost iServiceHost;
            if (this.mServiceHost != null) {
                iServiceHost = this.mServiceHost;
                return iServiceHost;
            }
            Log.i(TAG, "Connecting to camera servicehost.");
            IBinder serviceBinder = ServiceManager.getService(SERVICE_NAME);
            if (serviceBinder == null) {
                Log.e(TAG, "Connect to camera servicehost failed.");
                return null;
            }
            try {
                serviceBinder.linkToDeath(this, 0);
                IServiceHost serviceHost = IServiceHost.Stub.asInterface(serviceBinder);
                if (serviceHost != null) {
                    try {
                        serviceHost.connect(this.mClient);
                        this.mServiceHost = serviceHost;
                        iServiceHost = this.mServiceHost;
                        return iServiceHost;
                    } catch (RemoteException e) {
                        Log.e(TAG, "servicehost connect client failed.");
                        return null;
                    }
                }
                Log.e(TAG, "camera servicehost null.");
                return null;
            } catch (RemoteException e2) {
                Log.e(TAG, "linkToDeath to camera servicehost failed.");
                return null;
            }
        }
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

    @SuppressWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
    public void binderDied() {
        synchronized (gLock) {
            gServiceFetcher = null;
        }
        synchronized (this.mLock) {
            Log.w(TAG, "camera servicehost died.");
            this.mServiceHost = null;
        }
    }
}
